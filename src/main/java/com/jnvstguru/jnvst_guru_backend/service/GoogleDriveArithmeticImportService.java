package com.jnvstguru.jnvst_guru_backend.service;

import com.jnvstguru.jnvst_guru_backend.api.dto.ArithmeticImportResponse;
import com.jnvstguru.jnvst_guru_backend.api.dto.GoogleDriveArithmeticImportRequest;
import com.jnvstguru.jnvst_guru_backend.domain.*;
import com.jnvstguru.jnvst_guru_backend.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
@ConditionalOnProperty(prefix = "google.drive", name = "enabled", havingValue = "true")
public class GoogleDriveArithmeticImportService {
    private final GoogleDriveService driveService;
    private final PaperRepository paperRepository;
    private final QuestionRepository questionRepository;
    private final ArithmeticQuestionRepository arithmeticRepository;
    private final QuestionEnglishRepository englishRepository;
    private final PaperQuestionRepository paperQuestionRepository;

    public GoogleDriveArithmeticImportService(
            GoogleDriveService driveService,
            PaperRepository paperRepository,
            QuestionRepository questionRepository,
            ArithmeticQuestionRepository arithmeticRepository,
            QuestionEnglishRepository englishRepository,
            PaperQuestionRepository paperQuestionRepository) {
        this.driveService = driveService;
        this.paperRepository = paperRepository;
        this.questionRepository = questionRepository;
        this.arithmeticRepository = arithmeticRepository;
        this.englishRepository = englishRepository;
        this.paperQuestionRepository = paperQuestionRepository;
    }

    @Transactional
    public ArithmeticImportResponse importFile(GoogleDriveArithmeticImportRequest request) {
        if (request == null || request.fileId() == null || request.fileId().isBlank()
                || request.paperId() == null || request.batchNo() == null || request.batchNo().isBlank()) {
            throw new IllegalArgumentException("fileId, paperId, and batchNo are required");
        }
        PaperEntity paper = paperRepository.findById(request.paperId())
                .orElseThrow(() -> new IllegalArgumentException("Paper was not found"));
        List<RowData> rows;
        try (InputStream input = driveService.downloadFile(request.fileId())) {
            rows = readAndValidate(input);
        } catch (IOException ex) {
            throw new GoogleDriveException("Unable to read the Google Drive file", ex);
        }

        String batchNo = request.batchNo().trim();
        if (batchNo.length() > 60) {
            throw new IllegalArgumentException("batchNo must not exceed 60 characters");
        }
        List<Long> questionIds = new ArrayList<>();
        for (RowData row : rows) {
            String key = batchNo + "-" + row.questionNumber();
            PaperQuestionEntity existingOccurrence = paperQuestionRepository
                    .findByPaper_IdAndBatchQuestionKey(paper.getId(), key).orElse(null);
            QuestionEntity question = questionRepository.findByContentHash(row.contentHash()).orElse(null);
            if (existingOccurrence != null && question != null
                    && !existingOccurrence.getQuestion().getId().equals(question.getId())) {
                throw new IllegalArgumentException("Batch question key already belongs to another question: " + key);
            }
            if (question == null) {
                question = new QuestionEntity();
                question.setContentHash(row.contentHash());
                question.setStatus(QuestionStatus.ACTIVE);
                question = questionRepository.save(question);

                ArithmeticQuestionEntity arithmetic = new ArithmeticQuestionEntity();
                arithmetic.setQuestion(question);
                arithmetic.setQuestionType(row.questionType());
                arithmetic.setCorrectOption(row.correctOption());
                arithmetic.setDifficulty(row.difficulty());
                arithmeticRepository.save(arithmetic);
            }

            QuestionEnglishEntity english = englishRepository.findById(question.getId())
                    .orElseGet(QuestionEnglishEntity::new);
            english.setQuestion(question);
            english.setQuestionText(row.questionText());
            english.setOptionA(row.optionA());
            english.setOptionB(row.optionB());
            english.setOptionC(row.optionC());
            english.setOptionD(row.optionD());
            english.setExplanation(row.explanation());
            englishRepository.save(english);

            if (existingOccurrence == null) {
                PaperQuestionEntity occurrence = new PaperQuestionEntity();
                occurrence.setPaper(paper);
                occurrence.setQuestion(question);
                occurrence.setBatchNo(batchNo);
                occurrence.setQuestionNumber(row.questionNumber());
                occurrence.setBatchQuestionKey(key);
                occurrence.setQuestionType(PaperQuestionType.ARITHMETIC);
                paperQuestionRepository.save(occurrence);
            }
            questionIds.add(question.getId());
        }
        return new ArithmeticImportResponse(rows.size(), questionIds);
    }

    private List<RowData> readAndValidate(InputStream input) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(input)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("Excel file contains no worksheets");
            }
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() < 2) {
                throw new IllegalArgumentException("Excel file contains no data rows");
            }
            Map<String, Integer> headers = headers(sheet.getRow(sheet.getFirstRowNum()));
            List<String> required = List.of("question_number", "question_text", "option_a", "option_b",
                    "option_c", "option_d", "correct_option", "question_type", "question_type_translated",
                    "difficulty", "explanation", "language_code");
            required.forEach(name -> {
                if (!headers.containsKey(name)) throw new IllegalArgumentException("Missing Excel column: " + name);
            });
            Set<Integer> questionNumbers = new HashSet<>();
            List<RowData> rows = new ArrayList<>();
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isBlank(row, headers)) continue;
                int number = integerValue(row, headers.get("question_number"), "question_number");
                if (number < 1) throw new IllegalArgumentException("question_number must be positive");
                if (!questionNumbers.add(number)) {
                    throw new IllegalArgumentException("Duplicate question_number: " + number);
                }
                String language = text(row, headers, "language_code").toUpperCase(Locale.ROOT);
                if (!"EN".equals(language)) throw new IllegalArgumentException("Only language_code=EN is supported");
                String questionText = requiredText(row, headers, "question_text");
                String optionA = requiredText(row, headers, "option_a");
                String optionB = requiredText(row, headers, "option_b");
                String optionC = requiredText(row, headers, "option_c");
                String optionD = requiredText(row, headers, "option_d");
                String correct = requiredText(row, headers, "correct_option").toUpperCase(Locale.ROOT);
                if (!Set.of("A", "B", "C", "D").contains(correct)) throw new IllegalArgumentException("correct_option must be A, B, C, or D");
                ArithmeticQuestionEnums.QuestionType type = enumValue(text(row, headers, "question_type"), ArithmeticQuestionEnums.QuestionType.class, "question_type");
                ArithmeticQuestionEnums.Difficulty difficulty = enumValue(text(row, headers, "difficulty"), ArithmeticQuestionEnums.Difficulty.class, "difficulty");
                String explanation = text(row, headers, "explanation");
                rows.add(new RowData(number, questionText, optionA, optionB, optionC, optionD, correct, type, difficulty, explanation, hash(questionText, optionA, optionB, optionC, optionD)));
            }
            if (rows.isEmpty()) throw new IllegalArgumentException("Excel file contains no usable data rows");
            return rows;
        }
    }

    private Map<String, Integer> headers(Row row) {
        Map<String, Integer> result = new HashMap<>();
        for (Cell cell : row) result.put(cell.toString().trim().toLowerCase(Locale.ROOT), cell.getColumnIndex());
        return result;
    }

    private boolean isBlank(Row row, Map<String, Integer> headers) {
        return headers.values().stream().allMatch(index -> text(row, index).isBlank());
    }

    private String requiredText(Row row, Map<String, Integer> headers, String name) {
        String value = text(row, headers, name);
        if (value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value;
    }

    private String text(Row row, Map<String, Integer> headers, String name) { return text(row, headers.get(name)); }
    private String text(Row row, int index) { return new DataFormatter().formatCellValue(row.getCell(index)).trim(); }
    private int integerValue(Row row, int index, String name) {
        String value = text(row, index);
        try { return Integer.parseInt(value); } catch (NumberFormatException ex) { throw new IllegalArgumentException(name + " must be an integer"); }
    }
    private <E extends Enum<E>> E enumValue(String value, Class<E> type, String name) {
        try { return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("Invalid " + name + ": " + value); }
    }
    private String hash(String... values) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(
                    Arrays.stream(values).map(value -> value.replaceAll("\\s+", " ").trim())
                            .collect(java.util.stream.Collectors.joining("\n"))
                            .getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) { throw new IllegalStateException("SHA-256 is unavailable", ex); }
    }

    private record RowData(int questionNumber, String questionText, String optionA, String optionB, String optionC,
                           String optionD, String correctOption, ArithmeticQuestionEnums.QuestionType questionType,
                           ArithmeticQuestionEnums.Difficulty difficulty, String explanation, String contentHash) {}
}

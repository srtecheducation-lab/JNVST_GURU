package com.jnvstguru.jnvst_guru_backend.service;

import com.jnvstguru.jnvst_guru_backend.api.dto.GoogleDriveLanguageImportRequest;
import com.jnvstguru.jnvst_guru_backend.api.dto.LanguageImportResponse;
import com.jnvstguru.jnvst_guru_backend.domain.LanguagePassageEntity;
import com.jnvstguru.jnvst_guru_backend.domain.LanguageQuestionIndependentEntity;
import com.jnvstguru.jnvst_guru_backend.repository.LanguagePassageRepository;
import com.jnvstguru.jnvst_guru_backend.repository.LanguageQuestionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@ConditionalOnProperty(prefix = "google.drive", name = "enabled", havingValue = "true")
public class GoogleDriveLanguageImportService {
    private static final String FOLDER_MIME = "application/vnd.google-apps.folder";
    private static final Pattern FILE_PATTERN = Pattern.compile("^P(\\d{3,})_(en|bn)\\.csv$");
    private static final List<String> HEADERS = List.of(
            "passage_text", "question_no", "question_text", "option_a",
            "option_b", "option_c", "option_d", "correct_option");

    private final GoogleDriveService drive;
    private final LanguagePassageRepository passageRepository;
    private final LanguageQuestionRepository questionRepository;
    private final TransactionTemplate transactionTemplate;

    public GoogleDriveLanguageImportService(
            GoogleDriveService drive,
            LanguagePassageRepository passageRepository,
            LanguageQuestionRepository questionRepository,
            org.springframework.transaction.PlatformTransactionManager transactionManager) {
        this.drive = drive;
        this.passageRepository = passageRepository;
        this.questionRepository = questionRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public LanguageImportResponse importFolder(GoogleDriveLanguageImportRequest request) {
        if (request == null || request.folderId() == null || request.folderId().isBlank()) {
            throw new IllegalArgumentException("folderId is required");
        }
        GoogleDriveService.DriveFile folder = drive.getFile(request.folderId().trim());
        if (!FOLDER_MIME.equals(folder.mimeType())) {
            throw new IllegalArgumentException("Google Drive folderId must reference a folder");
        }
        String batchNo = folder.name();
        if (batchNo == null || batchNo.isBlank() || batchNo.length() > 60) {
            throw new IllegalArgumentException("Batch folder name must be present and at most 60 characters");
        }

        List<GoogleDriveService.DriveFile> children = drive.listChildren(folder.id());
        Map<String, GoogleDriveService.DriveFile> csvFiles = new TreeMap<>();
        List<String> errors = new ArrayList<>();
        for (GoogleDriveService.DriveFile child : children) {
            Matcher matcher = FILE_PATTERN.matcher(child.name() == null ? "" : child.name());
            if (!matcher.matches()) continue;
            if (csvFiles.putIfAbsent(child.name(), child) != null) {
                errors.add("Duplicate file in batch folder: " + child.name());
            }
        }

        Set<Integer> passageNumbers = new TreeSet<>();
        for (String fileName : csvFiles.keySet()) {
            Matcher matcher = FILE_PATTERN.matcher(fileName);
            if (matcher.matches()) {
                passageNumbers.add(parsePassageNumber(matcher.group(1)));
            }
        }
        List<String> missing = new ArrayList<>();
        for (int passage : passageNumbers) {
            for (String language : List.of("en", "bn")) {
                String expected = String.format("P%03d_%s.csv", passage, language);
                if (!csvFiles.containsKey(expected)) {
                    missing.add(expected);
                }
            }
        }

        int processed = 0;
        int imported = 0;
        int skipped = 0;
        int failed = 0;
        for (GoogleDriveService.DriveFile file : csvFiles.values()) {
            processed++;
            try {
                ParsedFile parsed;
                try (InputStream input = drive.downloadFile(file.id())) {
                    parsed = parseAndValidate(file.name(), input);
                }
                FileResult result = transactionTemplate.execute(status ->
                        importFile(batchNo, parsed));
                if (result == null) throw new IllegalStateException("Import transaction returned no result");
                imported += result.imported();
                skipped += result.skipped();
            } catch (Exception ex) {
                failed++;
                errors.add(file.name() + ": " + message(ex));
            }
        }
        return new LanguageImportResponse(
                batchNo, processed, imported, skipped, failed, List.copyOf(missing), List.copyOf(errors));
    }

    private FileResult importFile(String batchNo, ParsedFile parsed) {
        LanguagePassageEntity passage = passageRepository
                .findByLanguageCodeAndBatchNoAndPassageNumber(parsed.language(), batchNo, parsed.passageNumber())
                .orElseGet(() -> {
                    LanguagePassageEntity created = new LanguagePassageEntity();
                    created.setLanguageCode(parsed.language());
                    created.setBatchNo(batchNo);
                    created.setPassageNumber(parsed.passageNumber());
                    created.setPassageText(parsed.passageText());
                    return passageRepository.save(created);
                });

        int imported = 0;
        int skipped = 0;
        for (CsvQuestion row : parsed.questions()) {
            if (questionRepository.findByLanguageCodeAndBatchNoAndQuestionNumber(
                    parsed.language(), batchNo, row.questionNumber()).isPresent()) {
                skipped++;
                continue;
            }
            LanguageQuestionIndependentEntity question = new LanguageQuestionIndependentEntity();
            question.setLanguageCode(parsed.language());
            question.setBatchNo(batchNo);
            question.setPassage(passage);
            question.setQuestionNumber(row.questionNumber());
            question.setQuestionText(row.questionText());
            question.setOptionA(row.optionA());
            question.setOptionB(row.optionB());
            question.setOptionC(row.optionC());
            question.setOptionD(row.optionD());
            question.setCorrectOption(row.correctOption());
            question.setActive(true);
            questionRepository.save(question);
            imported++;
        }
        return new FileResult(imported, skipped);
    }

    private ParsedFile parseAndValidate(String fileName, InputStream input) throws IOException {
        Matcher fileMatcher = FILE_PATTERN.matcher(fileName);
        if (!fileMatcher.matches()) throw new IllegalArgumentException("Invalid Language CSV filename");
        int passageNumber = parsePassageNumber(fileMatcher.group(1));
        String language = fileMatcher.group(2);
        List<List<String>> rows = parseCsv(input);
        if (rows.isEmpty()) throw new IllegalArgumentException("CSV is empty");
        List<String> headers = rows.get(0).stream().map(this::normalizeHeader).toList();
        if (!headers.equals(HEADERS)) {
            throw new IllegalArgumentException("CSV headers must be exactly: " + String.join(",", HEADERS));
        }
        if (rows.size() != 6) {
            throw new IllegalArgumentException("CSV must contain exactly five question rows");
        }

        String passageText = "";
        Set<Integer> numbers = new HashSet<>();
        List<CsvQuestion> questions = new ArrayList<>();
        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            List<String> row = rows.get(rowIndex);
            if (row.size() != HEADERS.size()) throw new IllegalArgumentException("CSV row " + rowIndex + " must have eight columns");
            String candidatePassage = value(row, 0);
            if (!candidatePassage.isBlank() && passageText.isBlank()) passageText = candidatePassage;
            String questionNo = value(row, 1).toUpperCase(Locale.ROOT);
            Matcher numberMatcher = Pattern.compile("^Q(\\d{3})$").matcher(questionNo);
            if (!numberMatcher.matches()) throw new IllegalArgumentException("Invalid question number at row " + rowIndex);
            int questionNumber = Integer.parseInt(numberMatcher.group(1));
            int expected = (passageNumber - 1) * 5 + (rowIndex - 1) + 1;
            if (questionNumber != expected) throw new IllegalArgumentException(
                    "Expected Q" + String.format("%03d", expected) + " but found " + questionNo);
            if (!numbers.add(questionNumber)) throw new IllegalArgumentException("Duplicate question number: " + questionNo);
            String questionText = value(row, 2);
            if (questionText.isBlank()) throw new IllegalArgumentException("Question text is required for " + questionNo);
            String[] options = {value(row, 3), value(row, 4), value(row, 5), value(row, 6)};
            for (String option : options) if (option.isBlank()) throw new IllegalArgumentException("All options are required for " + questionNo);
            String correct = value(row, 7).toUpperCase(Locale.ROOT);
            if (!Set.of("A", "B", "C", "D").contains(correct)) throw new IllegalArgumentException("correct_option must be A, B, C, or D for " + questionNo);
            questions.add(new CsvQuestion(questionNumber, questionText, options[0], options[1], options[2], options[3], correct));
        }
        if (passageText.isBlank()) throw new IllegalArgumentException("passage_text is required");
        return new ParsedFile(passageNumber, language, passageText, questions);
    }

    private int parsePassageNumber(String value) {
        try {
            int passageNumber = Integer.parseInt(value);
            if (passageNumber <= 0) {
                throw new IllegalArgumentException("Passage number must be positive");
            }
            return passageNumber;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Passage number is outside the supported range", ex);
        }
    }

    private List<List<String>> parseCsv(InputStream input) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            char[] buffer = new char[4096];
            int read;
            while ((read = reader.read(buffer)) != -1) content.append(buffer, 0, read);
        }
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < content.length() && content.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else quoted = !quoted;
            } else if (c == ',' && !quoted) {
                row.add(field.toString());
                field.setLength(0);
            } else if ((c == '\n' || c == '\r') && !quoted) {
                if (c == '\r' && i + 1 < content.length() && content.charAt(i + 1) == '\n') i++;
                row.add(field.toString());
                field.setLength(0);
                if (!(row.size() == 1 && row.get(0).isBlank())) rows.add(row);
                row = new ArrayList<>();
            } else {
                field.append(c);
            }
        }
        if (quoted) throw new IllegalArgumentException("Unclosed quoted CSV field");
        if (field.length() > 0 || !row.isEmpty()) {
            row.add(field.toString());
            rows.add(row);
        }
        if (!rows.isEmpty() && !rows.get(0).isEmpty()) {
            rows.get(0).set(0, rows.get(0).get(0).replace("\uFEFF", ""));
        }
        return rows;
    }

    private String normalizeHeader(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String value(List<String> row, int index) {
        return row.get(index) == null ? "" : row.get(index).trim();
    }

    private String message(Exception ex) {
        return ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
    }

    private record ParsedFile(int passageNumber, String language, String passageText, List<CsvQuestion> questions) {}
    private record CsvQuestion(int questionNumber, String questionText, String optionA, String optionB,
                               String optionC, String optionD, String correctOption) {}
    private record FileResult(int imported, int skipped) {}
}

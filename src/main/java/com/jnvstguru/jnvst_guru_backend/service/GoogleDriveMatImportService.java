package com.jnvstguru.jnvst_guru_backend.service;

import com.jnvstguru.jnvst_guru_backend.api.dto.GoogleDriveMatImportRequest;
import com.jnvstguru.jnvst_guru_backend.api.dto.MatImportResponse;
import com.jnvstguru.jnvst_guru_backend.domain.MatQuestionEntity;
import com.jnvstguru.jnvst_guru_backend.domain.MatTopicEntity;
import com.jnvstguru.jnvst_guru_backend.repository.MatQuestionRepository;
import com.jnvstguru.jnvst_guru_backend.repository.MatTopicRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@ConditionalOnProperty(prefix = "google.drive", name = "enabled", havingValue = "true")
public class GoogleDriveMatImportService {
    private static final List<String> REQUIRED_COLUMNS =
            List.of("question_no", "topic", "batch", "correct_option", "difficulty");
    private final GoogleDriveService drive;
    private final SupabaseStorageService storage;
    private final MatQuestionRepository questions;
    private final MatTopicRepository topics;

    public GoogleDriveMatImportService(GoogleDriveService drive, SupabaseStorageService storage,
                                       MatQuestionRepository questions, MatTopicRepository topics) {
        this.drive = drive;
        this.storage = storage;
        this.questions = questions;
        this.topics = topics;
    }

    @Transactional
    public MatImportResponse importFolder(GoogleDriveMatImportRequest request) {
        String rootId = request == null || request.folderId() == null ? "" : request.folderId().trim();
        if (rootId.isBlank()) throw new IllegalArgumentException("folderId is required");
        Map<String, GoogleDriveService.DriveFile> rootFiles = byName(drive.listChildren(rootId));
        GoogleDriveService.DriveFile csv = rootFiles.get("questions.csv");
        if (csv == null) throw new IllegalArgumentException("Google Drive folder does not contain questions.csv");
        List<Row> rows;
        try (InputStream input = drive.downloadFile(csv.id())) {
            rows = readCsv(input);
        } catch (IOException ex) {
            throw new GoogleDriveException("Unable to read questions.csv", ex);
        }

        int imported = 0;
        int skipped = 0;
        List<MatImportResponse.MatImportFailure> failures = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (int index = 0; index < rows.size(); index++) {
            Row row = rows.get(index);
            try {
                if (!seen.add(row.questionNo())) throw new IllegalArgumentException("duplicate question_no in CSV");
                if (importRow(rootId, rootFiles, row, index + 1)) imported++;
                else skipped++;
            } catch (RuntimeException ex) {
                failures.add(new MatImportResponse.MatImportFailure(row.questionNo(), ex.getMessage()));
            }
        }
        return new MatImportResponse(imported, skipped, failures);
    }

    private boolean importRow(String rootId, Map<String, GoogleDriveService.DriveFile> rootFiles,
                              Row row, int sortOrder) {
        if (!Set.of("A", "B", "C", "D").contains(row.correctOption())) {
            throw new IllegalArgumentException("correct_option must be A, B, C, or D");
        }
        MatTopicEntity topic = topics.findByCode(row.topic())
                .orElseThrow(() -> new IllegalArgumentException("MAT topic not found: " + row.topic()));
        GoogleDriveService.DriveFile folder = rootFiles.get(row.questionNo());
        if (folder == null) throw new IllegalArgumentException("Question folder not found");
        Map<String, GoogleDriveService.DriveFile> files = byName(drive.listChildren(folder.id()));
        String optionAPath = path(rootId, row.questionNo(), "option_a.png");
        if (questions.existsByOptionAImageUrl(optionAPath)) return false;
        Map<String, String> paths = new LinkedHashMap<>();
        for (String name : List.of("option_a.png", "option_b.png", "option_c.png", "option_d.png")) {
            GoogleDriveService.DriveFile file = files.get(name);
            if (file == null) throw new IllegalArgumentException(name + " is required");
            paths.put(name, upload(file, path(rootId, row.questionNo(), name)));
        }
        GoogleDriveService.DriveFile questionImage = files.get("question.png");
        String questionPath = questionImage == null ? null
                : upload(questionImage, path(rootId, row.questionNo(), "question.png"));
        MatQuestionEntity entity = new MatQuestionEntity();
        entity.setTopic(topic);
        entity.setQuestionImageUrl(questionPath);
        entity.setOptionAImageUrl(paths.get("option_a.png"));
        entity.setOptionBImageUrl(paths.get("option_b.png"));
        entity.setOptionCImageUrl(paths.get("option_c.png"));
        entity.setOptionDImageUrl(paths.get("option_d.png"));
        entity.setCorrectOption(row.correctOption());
        entity.setDifficulty(row.difficulty().isBlank() ? null : row.difficulty());
        entity.setActive(true);
        entity.setSortOrder(sortOrder);
        questions.save(entity);
        return true;
    }

    private String upload(GoogleDriveService.DriveFile file, String path) {
        try (InputStream input = drive.downloadFile(file.id())) {
            return storage.upload(path, input, "image/png");
        } catch (IOException ex) {
            throw new GoogleDriveException("Unable to read image: " + file.name(), ex);
        }
    }

    private Map<String, GoogleDriveService.DriveFile> byName(List<GoogleDriveService.DriveFile> files) {
        Map<String, GoogleDriveService.DriveFile> result = new HashMap<>();
        for (GoogleDriveService.DriveFile file : files) result.putIfAbsent(file.name(), file);
        return result;
    }

    private String path(String rootId, String questionNo, String name) {
        return "mat/" + safe(rootId) + "/" + safe(questionNo) + "/" + name;
    }

    private String safe(String value) {
        if (!value.matches("[A-Za-z0-9._-]+")) throw new IllegalArgumentException("Invalid question_no or folderId");
        return value;
    }

    private List<Row> readCsv(InputStream input) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) throw new IllegalArgumentException("questions.csv is empty");
            List<String> header = parseLine(headerLine);
            Map<String, Integer> columns = new HashMap<>();
            for (int i = 0; i < header.size(); i++) columns.put(header.get(i).trim().toLowerCase(Locale.ROOT), i);
            for (String required : REQUIRED_COLUMNS) if (!columns.containsKey(required)) throw new IllegalArgumentException("Missing CSV column: " + required);
            List<Row> result = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> values = parseLine(line);
                String questionNo = value(values, columns, "question_no");
                if (questionNo.isBlank()) throw new IllegalArgumentException("question_no is required");
                result.add(new Row(questionNo, value(values, columns, "topic"), value(values, columns, "batch"),
                        value(values, columns, "correct_option").toUpperCase(Locale.ROOT),
                        value(values, columns, "difficulty")));
            }
            if (result.isEmpty()) throw new IllegalArgumentException("questions.csv contains no data rows");
            return result;
        }
    }

    private String value(List<String> values, Map<String, Integer> columns, String name) {
        int index = columns.get(name);
        return index < values.size() ? values.get(index).trim() : "";
    }

    private List<String> parseLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') { value.append('"'); i++; }
                else quoted = !quoted;
            } else if (c == ',' && !quoted) { result.add(value.toString()); value.setLength(0); }
            else value.append(c);
        }
        if (quoted) throw new IllegalArgumentException("Unclosed CSV quote");
        result.add(value.toString());
        return result;
    }

    private record Row(String questionNo, String topic, String batch, String correctOption, String difficulty) {}
}

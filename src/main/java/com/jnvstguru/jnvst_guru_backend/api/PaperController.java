package com.jnvstguru.jnvst_guru_backend.api;

import com.jnvstguru.jnvst_guru_backend.api.dto.PaperQuestionResponse;
import com.jnvstguru.jnvst_guru_backend.api.dto.PaperResponse;
import com.jnvstguru.jnvst_guru_backend.domain.PaperEntity;
import com.jnvstguru.jnvst_guru_backend.domain.PaperQuestionEntity;
import com.jnvstguru.jnvst_guru_backend.service.PaperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/papers")
public class PaperController {
    private final PaperService paperService;

    public PaperController(PaperService paperService) {
        this.paperService = paperService;
    }

    @GetMapping
    public List<PaperResponse> getPapers() {
        return paperService.getPapers().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{paperId}")
    public ResponseEntity<PaperResponse> getPaper(@PathVariable Long paperId) {
        return ResponseEntity.ok(toResponse(paperService.getPaperById(paperId)));
    }

    @GetMapping("/{paperId}/questions")
    public List<PaperQuestionResponse> getPaperQuestions(@PathVariable Long paperId) {
        return paperService.getPaperQuestions(paperId).stream().map(this::toQuestionResponse).toList();
    }

    private PaperResponse toResponse(PaperEntity paper) {
        return new PaperResponse(paper.getId(), paper.getCode(), paper.getName(), paper.getExamYear(),
                paper.getStatus(), paper.getCreatedAt(), paper.getUpdatedAt());
    }

    private PaperQuestionResponse toQuestionResponse(PaperQuestionEntity item) {
        return new PaperQuestionResponse(item.getId(), item.getPaper().getId(), item.getQuestion().getId(),
                item.getBatchNo(), item.getQuestionNumber(), item.getQuestionType(),
                item.getCreatedAt(), item.getUpdatedAt());
    }
}

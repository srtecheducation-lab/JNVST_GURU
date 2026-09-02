package com.jnvstguru.jnvst_guru_backend.service;

import com.jnvstguru.jnvst_guru_backend.domain.PaperEntity;
import com.jnvstguru.jnvst_guru_backend.domain.PaperQuestionEntity;
import com.jnvstguru.jnvst_guru_backend.repository.PaperQuestionRepository;
import com.jnvstguru.jnvst_guru_backend.repository.PaperRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaperService {
    private final PaperRepository paperRepository;
    private final PaperQuestionRepository paperQuestionRepository;

    public PaperService(PaperRepository paperRepository, PaperQuestionRepository paperQuestionRepository) {
        this.paperRepository = paperRepository;
        this.paperQuestionRepository = paperQuestionRepository;
    }

    @Transactional(readOnly = true)
    public PaperEntity getPaperById(Long paperId) {
        return paperRepository.findById(paperId)
                .orElseThrow(() -> new PaperNotFoundException(paperId));
    }

    @Transactional(readOnly = true)
    public List<PaperEntity> getPapers() {
        return paperRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long paperId) {
        return paperRepository.existsById(paperId);
    }

    @Transactional(readOnly = true)
    public List<PaperQuestionEntity> getPaperQuestions(Long paperId) {
        getPaperById(paperId);
        return paperQuestionRepository.findByPaper_Id(paperId);
    }
}

package com.jnvstguru.jnvst_guru_backend.repository;

import com.jnvstguru.jnvst_guru_backend.domain.PaperQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaperQuestionRepository extends JpaRepository<PaperQuestionEntity, Long> {
    List<PaperQuestionEntity> findByPaper_Id(Long paperId);
    java.util.Optional<PaperQuestionEntity> findByPaper_IdAndBatchQuestionKey(Long paperId, String batchQuestionKey);
}

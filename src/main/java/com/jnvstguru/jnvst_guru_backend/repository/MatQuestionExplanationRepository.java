package com.jnvstguru.jnvst_guru_backend.repository;

import com.jnvstguru.jnvst_guru_backend.domain.MatQuestionExplanationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MatQuestionExplanationRepository extends JpaRepository<MatQuestionExplanationEntity, Long> {
    List<MatQuestionExplanationEntity> findByMatQuestionIdInAndLanguageCode(
            Collection<Long> matQuestionIds, String languageCode);
}

package com.jnvstguru.jnvst_guru_backend.repository;

import com.jnvstguru.jnvst_guru_backend.domain.LanguageQuestionIndependentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.Collection;

@Repository
public interface LanguageQuestionRepository extends JpaRepository<LanguageQuestionIndependentEntity, Long> {
    Page<LanguageQuestionIndependentEntity> findByLanguageCodeAndBatchNoAndActiveTrueOrderByQuestionNumberAscIdAsc(
            String languageCode, String batchNo, Pageable pageable);

    long countByLanguageCodeAndBatchNoAndActiveTrue(String languageCode, String batchNo);

    long countByLanguageCodeAndActiveTrue(String languageCode);

    List<LanguageQuestionIndependentEntity> findByLanguageCodeAndBatchNoAndActiveTrueOrderByQuestionNumberAscIdAsc(
            String languageCode, String batchNo);

    Page<LanguageQuestionIndependentEntity> findByLanguageCodeAndActiveTrueOrderByBatchNoAscQuestionNumberAscIdAsc(
            String languageCode, Pageable pageable);

    Optional<LanguageQuestionIndependentEntity> findByLanguageCodeAndBatchNoAndQuestionNumber(
            String languageCode, String batchNo, Integer questionNumber);

    List<LanguageQuestionIndependentEntity> findByPassageIdInAndActiveTrueOrderByQuestionNumberAscIdAsc(
            Collection<Long> passageIds);
}

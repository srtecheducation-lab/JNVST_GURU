package com.jnvstguru.jnvst_guru_backend.repository;

import com.jnvstguru.jnvst_guru_backend.domain.MatQuestionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatQuestionRepository extends JpaRepository<MatQuestionEntity, Long> {
    boolean existsByOptionAImageUrl(String optionAImageUrl);
    Page<MatQuestionEntity> findByActiveTrueOrderBySortOrderAscIdAsc(Pageable pageable);
    Page<MatQuestionEntity> findByTopicIdAndActiveTrueOrderBySortOrderAscIdAsc(Long topicId, Pageable pageable);
    Page<MatQuestionEntity> findByOrderBySortOrderAscIdAsc(Pageable pageable);
    Page<MatQuestionEntity> findByTopicIdOrderBySortOrderAscIdAsc(Long topicId, Pageable pageable);
}

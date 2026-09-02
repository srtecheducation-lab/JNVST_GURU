package com.jnvstguru.jnvst_guru_backend.repository;

import com.jnvstguru.jnvst_guru_backend.domain.LanguageQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LanguageQuestionRepository extends JpaRepository<LanguageQuestionEntity, Long> {
}

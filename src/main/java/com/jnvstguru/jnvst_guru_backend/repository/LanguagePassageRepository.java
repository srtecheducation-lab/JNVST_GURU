package com.jnvstguru.jnvst_guru_backend.repository;

import com.jnvstguru.jnvst_guru_backend.domain.LanguagePassageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface LanguagePassageRepository extends JpaRepository<LanguagePassageEntity, Long> {
    @Query("""
            select p from LanguagePassageEntity p
            where p.languageCode = :languageCode
              and exists (
                  select q.id from LanguageQuestionIndependentEntity q
                  where q.passage = p and q.active = true
              )
            order by p.passageNumber asc, p.id asc
            """)
    Page<LanguagePassageEntity> findActivePassagesByLanguage(
            String languageCode, Pageable pageable);

    List<LanguagePassageEntity> findByLanguageCodeAndBatchNoOrderByPassageNumberAscIdAsc(
            String languageCode, String batchNo);

    Optional<LanguagePassageEntity> findByLanguageCodeAndBatchNoAndPassageNumber(
            String languageCode, String batchNo, Integer passageNumber);
}

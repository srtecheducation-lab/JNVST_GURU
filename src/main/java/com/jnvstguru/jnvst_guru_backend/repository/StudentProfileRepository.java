package com.jnvstguru.jnvst_guru_backend.repository;

import com.jnvstguru.jnvst_guru_backend.domain.StudentProfileEntity;
import com.jnvstguru.jnvst_guru_backend.domain.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfileEntity, Long> {
    @EntityGraph(attributePaths = {"state", "district", "examSession"})
    Optional<StudentProfileEntity> findByUser(UserEntity user);

    @Query("""
            select p.preferredLanguage
            from StudentProfileEntity p
            where p.user.authUserId = :authUserId
            """)
    Optional<String> findPreferredLanguageByAuthUserId(@Param("authUserId") UUID authUserId);
}

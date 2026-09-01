package com.jnvstguru.jnvst_guru_backend.repository;

import com.jnvstguru.jnvst_guru_backend.domain.StudentProfileEntity;
import com.jnvstguru.jnvst_guru_backend.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfileEntity, Long> {
    Optional<StudentProfileEntity> findByUser(UserEntity user);
}

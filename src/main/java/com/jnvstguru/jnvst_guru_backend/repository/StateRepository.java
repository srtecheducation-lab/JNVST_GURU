package com.jnvstguru.jnvst_guru_backend.repository;

import com.jnvstguru.jnvst_guru_backend.domain.StateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StateRepository extends JpaRepository<StateEntity, Long> {
    List<StateEntity> findByStatusOrderByNameAsc(String status);

    Optional<StateEntity> findByIdAndStatus(Long id, String status);
}

package com.jnvstguru.jnvst_guru_backend.repository;

import com.jnvstguru.jnvst_guru_backend.domain.DistrictEntity;
import com.jnvstguru.jnvst_guru_backend.domain.StateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistrictRepository extends JpaRepository<DistrictEntity, Long> {
    List<DistrictEntity> findByStateAndStatusOrderByNameAsc(StateEntity state, String status);

    Optional<DistrictEntity> findByIdAndStatus(Long id, String status);
}

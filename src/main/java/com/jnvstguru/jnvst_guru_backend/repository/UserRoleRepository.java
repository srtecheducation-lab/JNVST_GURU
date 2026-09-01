package com.jnvstguru.jnvst_guru_backend.repository;

import com.jnvstguru.jnvst_guru_backend.domain.UserEntity;
import com.jnvstguru.jnvst_guru_backend.domain.UserRoleEntity;
import com.jnvstguru.jnvst_guru_backend.domain.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UserRoleId> {
    @Query("select ur from UserRoleEntity ur join fetch ur.role r where ur.user = :user order by r.code")
    List<UserRoleEntity> findByUserOrderByRoleCode(@Param("user") UserEntity user);
}

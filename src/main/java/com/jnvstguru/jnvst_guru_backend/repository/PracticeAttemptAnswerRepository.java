package com.jnvstguru.jnvst_guru_backend.repository;

import com.jnvstguru.jnvst_guru_backend.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PracticeAttemptAnswerRepository extends JpaRepository<PracticeAttemptAnswerEntity, Long> {
    List<PracticeAttemptAnswerEntity> findByAttemptOrderByQuestionId(PracticeAttemptEntity attempt);
    List<PracticeAttemptAnswerEntity> findByAttemptOrderById(PracticeAttemptEntity attempt);
}

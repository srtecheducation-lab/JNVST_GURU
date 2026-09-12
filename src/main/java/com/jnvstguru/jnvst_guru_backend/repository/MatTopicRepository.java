package com.jnvstguru.jnvst_guru_backend.repository;

import com.jnvstguru.jnvst_guru_backend.domain.MatTopicEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MatTopicRepository extends JpaRepository<MatTopicEntity, Long> {
    @Query(value = """
        select t.id as id, t.code as code, t.sort_order as sortOrder,
               coalesce(p.name, e.name) as name,
               coalesce(p.description, e.description) as description
        from application.mat_topics t
        left join application.mat_topic_translations p
          on p.topic_id = t.id and p.language_code = :language
        left join application.mat_topic_translations e
          on e.topic_id = t.id and e.language_code = 'en'
        where t.is_active = true
        order by t.sort_order asc, t.id asc
        """, countQuery = "select count(*) from application.mat_topics where is_active = true", nativeQuery = true)
    Page<StudentMatTopicProjection> findActiveStudentTopics(@Param("language") String language, Pageable pageable);

    interface StudentMatTopicProjection {
        Long getId();
        String getCode();
        Integer getSortOrder();
        String getName();
        String getDescription();
    }
}

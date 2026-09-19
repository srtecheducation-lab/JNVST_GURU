package com.jnvstguru.jnvst_guru_backend.service;

import com.jnvstguru.jnvst_guru_backend.api.dto.MatTopicResponse;
import com.jnvstguru.jnvst_guru_backend.repository.MatQuestionRepository;
import com.jnvstguru.jnvst_guru_backend.repository.MatTopicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatQuestionServiceTest {
    private static final UUID AUTH_USER_ID = UUID.randomUUID();

    @Mock
    private MatQuestionRepository questions;
    @Mock
    private MatTopicRepository topics;
    @Mock
    private ApplicationUserService applicationUserService;
    @InjectMocks
    private MatQuestionService service;

    @Test
    void englishStudentReceivesEnglishTopics() {
        assertTopicsForLanguage("en", "en", "English");
    }

    @Test
    void bengaliStudentReceivesBengaliTopics() {
        assertTopicsForLanguage("bn", "bn", "Bengali");
    }

    @Test
    void nullLanguageFallsBackToEnglish() {
        assertTopicsForLanguage(null, "en", "English");
    }

    @Test
    void blankLanguageFallsBackToEnglish() {
        assertTopicsForLanguage("  ", "en", "English");
    }

    @Test
    void unsupportedLanguageFallsBackToEnglish() {
        assertTopicsForLanguage("hi", "en", "English");
    }

    @Test
    void topicOrderingRemainsUnchanged() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(applicationUserService.getPreferredLanguage(AUTH_USER_ID)).thenReturn("bn");
        when(topics.findActiveStudentTopics("bn", pageable)).thenReturn(page(
                projection(12L, "B", 2, "Second"),
                projection(7L, "A", 1, "First")));

        Page<MatTopicResponse> result = service.studentTopics(AUTH_USER_ID, pageable);

        assertEquals(List.of(12L, 7L), result.getContent().stream().map(MatTopicResponse::id).toList());
        assertEquals(List.of(2, 1), result.getContent().stream().map(MatTopicResponse::sortOrder).toList());
        verify(topics).findActiveStudentTopics("bn", pageable);
    }

    private void assertTopicsForLanguage(String preferredLanguage, String expectedLanguage, String text) {
        PageRequest pageable = PageRequest.of(0, 20);
        when(applicationUserService.getPreferredLanguage(AUTH_USER_ID)).thenReturn(preferredLanguage);
        when(topics.findActiveStudentTopics(expectedLanguage, pageable)).thenReturn(page(
                projection(7L, "A", 1, text)));

        Page<MatTopicResponse> result = service.studentTopics(AUTH_USER_ID, pageable);

        assertEquals(text, result.getContent().get(0).name());
        verify(topics).findActiveStudentTopics(expectedLanguage, pageable);
    }

    private Page<MatTopicRepository.StudentMatTopicProjection> page(
            MatTopicRepository.StudentMatTopicProjection... projections) {
        return new PageImpl<>(List.of(projections));
    }

    private MatTopicRepository.StudentMatTopicProjection projection(
            Long id, String code, Integer sortOrder, String name) {
        return new MatTopicRepository.StudentMatTopicProjection() {
            @Override
            public Long getId() {
                return id;
            }

            @Override
            public String getCode() {
                return code;
            }

            @Override
            public Integer getSortOrder() {
                return sortOrder;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDescription() {
                return name + " description";
            }
        };
    }
}

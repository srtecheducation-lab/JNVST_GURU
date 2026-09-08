package com.jnvstguru.jnvst_guru_backend.service;

import com.jnvstguru.jnvst_guru_backend.api.dto.StudentArithmeticQuestionResponse;
import com.jnvstguru.jnvst_guru_backend.domain.ArithmeticQuestionEnums;
import com.jnvstguru.jnvst_guru_backend.repository.ArithmeticQuestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArithmeticQuestionServiceTest {
    @Mock
    private ArithmeticQuestionRepository arithmeticQuestionRepository;

    @InjectMocks
    private ArithmeticQuestionService arithmeticQuestionService;

    @Test
    void defaultsToEnglishContent() {
        PageRequest pageable = pageRequest();
        when(arithmeticQuestionRepository.findActiveStudentQuestions(
                ArithmeticQuestionEnums.QuestionType.FRACTION,
                ArithmeticQuestionEnums.Difficulty.EASY,
                pageable)).thenReturn(page(7L, 36L, "English 7", "English 36"));

        Page<StudentArithmeticQuestionResponse> result = arithmeticQuestionService.getStudentQuestions(
                null, ArithmeticQuestionEnums.QuestionType.FRACTION,
                ArithmeticQuestionEnums.Difficulty.EASY, pageable);

        assertEquals(List.of(7L, 36L), ids(result));
        assertEquals("English 7", result.getContent().get(0).questionText());
        verify(arithmeticQuestionRepository).findActiveStudentQuestions(
                ArithmeticQuestionEnums.QuestionType.FRACTION,
                ArithmeticQuestionEnums.Difficulty.EASY, pageable);
    }

    @Test
    void selectsBengaliContentWithoutChangingPageIdsOrOrder() {
        PageRequest pageable = pageRequest();
        when(arithmeticQuestionRepository.findActiveStudentQuestionsInBengali(
                ArithmeticQuestionEnums.QuestionType.FRACTION,
                ArithmeticQuestionEnums.Difficulty.EASY,
                pageable)).thenReturn(page(7L, 36L, "Bengali 7", "Bengali 36"));

        Page<StudentArithmeticQuestionResponse> result = arithmeticQuestionService.getStudentQuestions(
                ArithmeticQuestionEnums.Language.BENGALI,
                ArithmeticQuestionEnums.QuestionType.FRACTION,
                ArithmeticQuestionEnums.Difficulty.EASY, pageable);

        assertEquals(List.of(7L, 36L), ids(result));
        assertEquals("Bengali 7", result.getContent().get(0).questionText());
        assertEquals("Bengali 36", result.getContent().get(1).questionText());
        verify(arithmeticQuestionRepository).findActiveStudentQuestionsInBengali(
                ArithmeticQuestionEnums.QuestionType.FRACTION,
                ArithmeticQuestionEnums.Difficulty.EASY, pageable);
    }

    private PageRequest pageRequest() {
        return PageRequest.of(0, 20, Sort.by("questionId"));
    }

    private List<Long> ids(Page<StudentArithmeticQuestionResponse> page) {
        return page.getContent().stream().map(StudentArithmeticQuestionResponse::id).toList();
    }

    private Page<ArithmeticQuestionRepository.StudentArithmeticQuestionProjection> page(
            Long firstId, Long secondId, String firstText, String secondText) {
        return new PageImpl<>(List.of(projection(firstId, firstText), projection(secondId, secondText)));
    }

    private ArithmeticQuestionRepository.StudentArithmeticQuestionProjection projection(Long id, String text) {
        return new ArithmeticQuestionRepository.StudentArithmeticQuestionProjection() {
            @Override
            public Long getQuestionId() {
                return id;
            }

            @Override
            public ArithmeticQuestionEnums.QuestionType getQuestionType() {
                return ArithmeticQuestionEnums.QuestionType.FRACTION;
            }

            @Override
            public String getQuestionText() {
                return text;
            }

            @Override
            public String getOptionA() {
                return "A";
            }

            @Override
            public String getOptionB() {
                return "B";
            }

            @Override
            public String getOptionC() {
                return "C";
            }

            @Override
            public String getOptionD() {
                return "D";
            }

            @Override
            public ArithmeticQuestionEnums.Difficulty getDifficulty() {
                return ArithmeticQuestionEnums.Difficulty.EASY;
            }
        };
    }
}

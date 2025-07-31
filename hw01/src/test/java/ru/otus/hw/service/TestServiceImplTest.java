package ru.otus.hw.service;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Test service testing")
@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    private IOService ioService;

    @Mock
    private QuestionDao questionDao;

    @InjectMocks
    private TestServiceImpl testService;

    @DisplayName("should correctly display questions with answer options")
    @Test
    void shouldCorrectlyDisplayQuestionsWithAnswers() {
        // Given
        List<Answer> answers1 = List.of(
                new Answer("Answer 1", true),
                new Answer("Answer 2", false)
        );
        List<Answer> answers2 = List.of(
                new Answer("Answer A", false),
                new Answer("Answer B", true),
                new Answer("Answer C", false)
        );
        List<Question> questions = List.of(
                new Question("Question 1?", answers1),
                new Question("Question 2?", answers2)
        );

        when(questionDao.findAll()).thenReturn(questions);

        // When
        testService.executeTest();

        // Then
        verify(ioService, times(3)).printLine("");
        verify(ioService).printFormattedLine("Please answer the questions below%n");

        verify(ioService).printFormattedLine("Question %d: %s", 1, "Question 1?");
        verify(ioService).printFormattedLine("  %d. %s", 1, "Answer 1");
        verify(ioService).printFormattedLine("  %d. %s", 2, "Answer 2");

        verify(ioService).printFormattedLine("Question %d: %s", 2, "Question 2?");
        verify(ioService).printFormattedLine("  %d. %s", 1, "Answer A");
        verify(ioService).printFormattedLine("  %d. %s", 2, "Answer B");
        verify(ioService).printFormattedLine("  %d. %s", 3, "Answer C");

        verifyNoMoreInteractions(ioService, questionDao);
    }
} 
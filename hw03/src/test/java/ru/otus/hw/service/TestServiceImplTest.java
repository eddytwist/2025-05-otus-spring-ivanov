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
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Тест для TestServiceImpl")
@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    private LocalizedIOService ioService;

    @Mock
    private QuestionDao questionDao;

    @InjectMocks
    private TestServiceImpl testService;

    @DisplayName("Корректно выполняет тест для студента")
    @Test
    void shouldExecuteTestForStudent() {
        // given
        Student student = new Student("John", "Doe");

        List<Answer> answers1 = List.of(
                new Answer("Correct answer", true),
                new Answer("Wrong answer", false)
        );
        List<Answer> answers2 = List.of(
                new Answer("Wrong answer", false),
                new Answer("Correct answer", true)
        );

        List<Question> questions = List.of(
                new Question("Question 1?", answers1),
                new Question("Question 2?", answers2)
        );

        when(questionDao.findAll()).thenReturn(questions);
        mockGetMessage();
        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1) // первый вопрос - правильный ответ (индекс 1)
                .thenReturn(2); // второй вопрос - правильный ответ (индекс 2)

        // when
        TestResult result = testService.executeTestFor(student);

        // then
        assertThat(result.getStudent()).isEqualTo(student);
        assertThat(result.getAnsweredQuestions()).hasSize(2);
        assertThat(result.getRightAnswersCount()).isEqualTo(2);

        verify(ioService, times(1)).printLineLocalized("TestService.answer.the.questions");
        verify(ioService, times(5)).printLine(anyString()); // 1 пустая строка в начале + 2 пустые строки перед вопросами + 2 текста вопросов
        verify(ioService, times(4)).printFormattedLine(anyString(), anyInt(), anyString()); // варианты ответов
        verify(ioService, times(2)).readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()); // 2 вопроса
        verify(questionDao, times(1)).findAll();
        verifyNoMoreInteractions(ioService);
        verifyNoMoreInteractions(questionDao);
    }

    @DisplayName("Засчитывает неправильные ответы")
    @Test
    void shouldCountWrongAnswers() {
        // given
        Student student = new Student("Jane", "Smith");

        List<Answer> answers = List.of(
                new Answer("Correct answer", true),
                new Answer("Wrong answer", false)
        );

        List<Question> questions = List.of(
                new Question("Question?", answers)
        );

        when(questionDao.findAll()).thenReturn(questions);
        mockGetMessage();
        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(2); // выбираем неправильный ответ (индекс 2)

        // when
        TestResult result = testService.executeTestFor(student);

        // then
        assertThat(result.getRightAnswersCount()).isEqualTo(0);
    }

    private void mockGetMessage() {
        when(ioService.getMessage(anyString(), anyInt())).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            int size = invocation.getArgument(1);
            if (code.equals("TestService.enter.answer.number")) {
                return "Please enter answer number (1-" + size + "):";
            } else if (code.equals("TestService.invalid.answer.number")) {
                return "Invalid answer number. Please enter number from 1 to " + size;
            }
            return "";
        });
    }
}

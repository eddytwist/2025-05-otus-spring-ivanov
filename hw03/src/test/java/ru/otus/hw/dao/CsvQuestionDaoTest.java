package ru.otus.hw.dao;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Тест для CsvQuestionDao")
@ExtendWith(MockitoExtension.class)
class CsvQuestionDaoTest {

    @Mock
    private TestFileNameProvider fileNameProvider;

    @InjectMocks
    private CsvQuestionDao dao;

    @DisplayName("Корректно парсит вопросы из CSV файла")
    @Test
    void shouldParseQuestionsFromCsvFile() {
        when(fileNameProvider.getTestFileName()).thenReturn("questions.csv");

        List<Question> questions = dao.findAll();

        assertThat(questions).isNotEmpty();
        assertThat(questions).hasSize(3);

        Question firstQuestion = questions.get(0);
        assertThat(firstQuestion.text()).contains("Is there life on Mars?");
        assertThat(firstQuestion.answers()).hasSize(3);
        assertThat(firstQuestion.answers()).extracting(Answer::isCorrect)
                .containsExactly(true, false, false);

        verify(fileNameProvider, times(1)).getTestFileName();
        verifyNoMoreInteractions(fileNameProvider);
    }

    @DisplayName("Выбрасывает исключение при отсутствии файла")
    @Test
    void shouldThrowExceptionWhenFileNotFound() {
        when(fileNameProvider.getTestFileName()).thenReturn("nonexistent.csv");

        assertThatThrownBy(dao::findAll)
                .isInstanceOf(QuestionReadException.class)
                .hasMessageContaining("Failed to read questions from file:");
    }
}

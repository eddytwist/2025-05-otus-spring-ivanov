package ru.otus.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Тест для CsvQuestionDao")
class CsvQuestionDaoTest {

    @Mock
    private TestFileNameProvider fileNameProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("Корректно парсит вопросы из CSV файла")
    @Test
    void shouldParseQuestionsFromCsvFile() {
        when(fileNameProvider.getTestFileName()).thenReturn("questions.csv");
        
        CsvQuestionDao dao = new CsvQuestionDao(fileNameProvider);
        
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
        
        CsvQuestionDao dao = new CsvQuestionDao(fileNameProvider);
        
        assertThatThrownBy(dao::findAll)
                .isInstanceOf(QuestionReadException.class)
                .hasMessageContaining("Failed to read questions from file:");
    }
}

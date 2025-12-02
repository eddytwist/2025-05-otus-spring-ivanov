package ru.otus.hw.dao;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тест для CsvQuestionDao")
@SpringBootTest
@ActiveProfiles("test")
class CsvQuestionDaoTest {

    @Autowired
    private QuestionDao dao;

    @DisplayName("Корректно парсит вопросы из CSV файла")
    @Test
    void shouldParseQuestionsFromCsvFile() {
        // When questions are loaded from configured CSV file (questions.csv for en-US
        // locale in test profile)
        List<Question> questions = dao.findAll();

        // Then questions are parsed correctly
        assertThat(questions).isNotEmpty();
        assertThat(questions).hasSize(3);

        Question firstQuestion = questions.get(0);
        assertThat(firstQuestion.text()).contains("Is there life on Mars?");
        assertThat(firstQuestion.answers()).hasSize(3);
        assertThat(firstQuestion.answers()).extracting(Answer::isCorrect)
                .containsExactly(true, false, false);
    }
}

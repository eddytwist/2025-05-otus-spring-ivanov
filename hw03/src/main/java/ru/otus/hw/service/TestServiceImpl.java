package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final LocalizedIOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printLineLocalized("TestService.answer.the.questions");

        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (var question: questions) {
            var isAnswerValid = askQuestion(question);
            testResult.applyAnswer(question, isAnswerValid);
        }
        return testResult;
    }

    // нужно также локализовать ответы
    private boolean askQuestion(ru.otus.hw.domain.Question question) {
        ioService.printLine("");
        ioService.printLine(question.text());
        var answers = question.answers();
        for (int i = 0; i < answers.size(); i++) {
            ioService.printFormattedLine("%d. %s", i + 1, answers.get(i).text());
        }

        int userAnswer = ioService.readIntForRangeWithPrompt(1, answers.size(),
                "Please enter answer number (1-" + answers.size() + "):",
                "Invalid answer number. Please enter number from 1 to " + answers.size());

        return answers.get(userAnswer - 1).isCorrect();
    }

}

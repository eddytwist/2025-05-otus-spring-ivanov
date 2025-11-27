package ru.otus.hw.shell;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;
import ru.otus.hw.service.ResultService;
import ru.otus.hw.service.StudentService;
import ru.otus.hw.service.TestService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Тест для TestCommands")
@SpringBootTest
@ActiveProfiles("test")
class TestCommandsTest {

    @MockitoBean
    private TestService testService;

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private ResultService resultService;

    @Autowired
    private TestCommands testCommands;

    @DisplayName("Корректно выполняет команду start-test")
    @Test
    void shouldExecuteStartTestCommand() {
        // given
        Student student = new Student("John", "Doe");
        TestResult testResult = new TestResult(student);

        when(studentService.determineCurrentStudent()).thenReturn(student);
        when(testService.executeTestFor(student)).thenReturn(testResult);

        // when
        testCommands.startTest();

        // then
        verify(studentService, times(1)).determineCurrentStudent();
        verify(testService, times(1)).executeTestFor(student);
        verify(resultService, times(1)).showResult(testResult);
    }
}

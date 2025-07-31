package ru.otus.hw.dao;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Question;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {
    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        try (InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(fileNameProvider.getTestFileName())) {

            if (inputStream == null) {
                throw new QuestionReadException("Test file not found: " + fileNameProvider.getTestFileName());
            }

            CsvToBean<QuestionDto> csvToBean = new CsvToBeanBuilder<QuestionDto>(new InputStreamReader(inputStream))
                    .withType(QuestionDto.class)
                    .withSeparator(';')
                    .withSkipLines(1)
                    .build();

            return csvToBean.parse()
                    .stream()
                    .map(QuestionDto::toDomainObject)
                    .toList();
        } catch (Exception e) {
            throw new QuestionReadException("Failed to read questions from file: "
                    + fileNameProvider.getTestFileName(), e);
        }
    }
}

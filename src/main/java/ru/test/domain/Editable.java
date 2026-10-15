package ru.test.domain;

import ru.test.domain.exception.CsvParserException;

import java.util.Map;

public interface Editable {
    Map<String, String> getEditableFields();

    void update(Map<String, String> fields) throws CsvParserException;
}

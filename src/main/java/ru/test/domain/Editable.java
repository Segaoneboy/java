package ru.test.domain;

import java.util.Map;

public interface Editable {
    Map<String, String> getEditableFields();

    void update(Map<String, String> fields);
}

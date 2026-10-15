package ru.test.domain;

import ru.test.domain.datastructure.Connections;
import ru.test.domain.exception.CsvParserException;
import ru.test.domain.exception.ErrorCode;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserProfile extends Profile implements Editable {
    public UserProfile() {
        super();
    }

    public UserProfile(int id, String name, String city, int birthYear, Connections[] friendshipList) {
        super(id, name, city, birthYear, friendshipList);
    }

    @Override
    public Map<String, String> getEditableFields() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Имя", getName());
        map.put("Город", getCity());
        map.put("Год рождения", String.valueOf(getBirthYear()));
        return map;
    }

    @Override
    public void update(Map<String, String> fields) throws CsvParserException {
        setName(fields.get("Имя"));
        setCity(fields.get("Город"));
        try {
            setBirthYear(Integer.parseInt(fields.get("Год рождения")));
        } catch (NumberFormatException e) {
            throw new CsvParserException(ErrorCode.BAD_NUMBER, "Год рождения должен быть числом");
        }
    }
}
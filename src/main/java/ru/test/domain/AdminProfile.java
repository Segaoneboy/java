package ru.test.domain;

import ru.test.domain.datastructure.Connections;
import ru.test.domain.exception.CsvParserException;
import ru.test.domain.exception.ErrorCode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminProfile extends Profile implements Editable {
    private List<String> groups = new ArrayList<>();

    public AdminProfile() {
        super();
    }

    public AdminProfile(int id, String name, String city, int birthYear, Connections[] friendshipList, List<String> groups) {
        super(id, name, city, birthYear, friendshipList);
        this.groups = (groups != null) ? groups : new ArrayList<>();
    }

    public List<String> getGroups() { return groups; }
    public void setGroups(List<String> groups) { this.groups = groups; }

    @Override
    public Map<String, String> getEditableFields() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Имя", getName());
        map.put("Город", getCity());
        map.put("Год рождения", String.valueOf(getBirthYear()));
        map.put("Группы", String.join(", ", groups));
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

        List<String> newGroups = new ArrayList<>();
        String rawGroups = fields.get("Группы");
        if (rawGroups != null && !rawGroups.isBlank()) {
            for (String g : rawGroups.split(",")) {
                newGroups.add(g.trim());
            }
        }
        this.groups = newGroups;
    }
}
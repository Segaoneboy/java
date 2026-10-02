package ru.test.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
public class AdminProfile extends Profile implements Editable {
    private List<String> groups = new ArrayList<String>();

    public AdminProfile(){
        super();
    }
    public AdminProfile(int id, String name, String city, int birthYear, List<Connections> friendshipList, List<String> groups){
        super(id, name, city, birthYear, friendshipList);
        this.groups = (groups != null) ? groups : new ArrayList<>();
    }

    public List<String> getGroups(){
        return groups;
    }
    public void setGroups(List<String> groups){
        this.groups = groups;
    }

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
    public void update(Map<String, String> fields) {
        setName(fields.get("Имя"));
        setCity(fields.get("Город"));

        try {
            setBirthYear(Integer.parseInt(fields.get("Год рождения")));
        } catch (NumberFormatException ignored) {}

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

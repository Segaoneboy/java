package ru.test.data;

import ru.test.domain.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class CsvLoader {
    private static final String DELIMITER = ";";

    public Profile parseLineToProfile(String line){
        String[] tokens = line.split(DELIMITER, -1);
        if(tokens.length < 5){
            return null;
        }
        try{
            int id = Integer.parseInt(tokens[0].trim());
            String type = tokens[1].trim().toUpperCase();
            String name = tokens[2].trim();
            String city = tokens[3].trim();
            int birthYear = Integer.parseInt(tokens[4].trim());
            String groups = tokens.length > 5 ? tokens[5].trim() : "";

            List<Connections> friendships = new ArrayList<>();

            List<String> groupList = new ArrayList<>();
            if(!groups.isEmpty()){
                for(String g: groups.split(",")){
                    groupList.add(g.trim());
                }
            }

            return switch (type){
                case "ADMIN"-> new AdminProfile(id,name,city,birthYear,friendships,groupList);
                case "DELETED"-> new DeletedProfile(id,name,city,birthYear,friendships);
                default -> new Profile(id,name,city,birthYear,friendships);
            };
        } catch (NumberFormatException e){
            System.err.println("Ошибка формата числе в строке:" + line);
            return null;
        }
    }

    public List<Profile> load(File file){
        List<Profile> profiles = new ArrayList<>();

        if (file == null || !file.exists()){
            return profiles;
        }
       try (BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line;
            boolean isHeader = true;

            while((line = reader.readLine()) != null){
                if(isHeader){
                    isHeader=false;
                    continue;
                }
                if(line.trim().isEmpty()){
                    continue;
                }
                Profile profile = parseLineToProfile(line);
                if(profile != null) {
                    profiles.add(profile);
                }
            }
        } catch(IOException e){
           e.printStackTrace();
       }
       return profiles;
    }
}

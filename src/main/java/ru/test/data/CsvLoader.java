package ru.test.data;

import ru.test.domain.*;
import ru.test.domain.exception.CsvParserException;
import ru.test.domain.exception.ErrorCode;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class CsvLoader {
    private static final String DELIMITER = ";";
    private static final String HEADER = "id;type;name;city;birthyear;groups";

    public Profile parseLineToProfile(String line, int lineNumber) throws CsvParserException{
        String[] tokens = line.split(DELIMITER, -1);
        if(tokens.length < 5){
            throw new CsvParserException(
                    ErrorCode.WRONG_FIELD_COUNT,
                    lineNumber,
                    "Ожидалось минимум 5 полей, получено: " + tokens.length
            );
        }
        int id;
        int birthYear;

        try{
            id = Integer.parseInt(tokens[0].trim());
            birthYear = Integer.parseInt(tokens[4].trim());
        } catch(NumberFormatException e){
            throw new CsvParserException(
                    ErrorCode.BAD_NUMBER,
                    lineNumber,
                    "Некорректный формат числа в ID или годе рождения: " + e.getMessage()
            );
        }

        if (id < 0) {
            throw new CsvParserException(
                    ErrorCode.BAD_NUMBER,
                    lineNumber,
                    "ID не может быть отрицательным: " + id
            );
        }

        if (birthYear < 1900 || birthYear > 2026) {
            throw new CsvParserException(
                    ErrorCode.BAD_NUMBER,
                    lineNumber,
                    "Некорректный год рождения: " + birthYear
            );
        }


        String type = tokens[1].trim().toUpperCase();
        String name = tokens[2].trim();
        String city = tokens[3].trim();
        String groups = tokens.length > 5 ? tokens[5].trim() : "";

        List<Connections> friendships = new ArrayList<>();

        List<String> groupList = new ArrayList<>();
        if(!groups.isEmpty()){
            for(String g: groups.split(",")){
                if(!g.isBlank()){
                    groupList.add(g.trim());
                }
            }
        }

        return switch (type){
            case "ADMIN"-> new AdminProfile(id,name,city,birthYear,friendships,groupList);
            case "DELETED"-> new DeletedProfile(id,name,city,birthYear,friendships);
            default -> new Profile(id,name,city,birthYear,friendships);
        };

    }

    private void validateHeader(String headerLine, int lineNumber) throws CsvParserException{
        String normalizedHeader = headerLine.replace("\uFEFF", "").trim().toLowerCase();
        if(!normalizedHeader.startsWith(HEADER)){
            throw new CsvParserException(
                    ErrorCode.INVALID_HEADER,
                    lineNumber,
                    "Заголовок CSV не соответствует формату. Ожидалось: " + HEADER
            );
        }
    }

    public List<Profile> load(File file) throws CsvParserException {
        int lineNumber = 0;

        List<Profile> profiles = new ArrayList<>();

        if (file == null || !file.exists()){
            throw new CsvParserException(
                    ErrorCode.FILE_NOT_FOUND,
                    "Указанный файл не существует или путь равен null"
            );
        }
       try (BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line;
            boolean isHeader = true;

            while((line = reader.readLine()) != null){
                lineNumber++;
                if(isHeader){
                    isHeader=false;
                    validateHeader(line, lineNumber);
                    continue;
                }
                if(line.trim().isEmpty()){
                    continue;
                }
                try{
                    Profile profile = parseLineToProfile(line, lineNumber);
                    if(profile != null) {
                        profiles.add(profile);
                    }
                } catch(CsvParserException e){
                    System.err.printf("[Пропуск строки %d] Код: %s, Описание: %s%n",e.getLineNumber(), e.getErrorCode(), e.getMessage());
                }

            }
        } catch(IOException e){
           throw new CsvParserException(
                   ErrorCode.FILE_READ_ERROR,
                   "Ошибка при чтении файла: " + e.getMessage()
           );
       }

       return profiles;
    }
}

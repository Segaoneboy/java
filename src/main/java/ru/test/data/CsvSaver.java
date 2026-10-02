package ru.test.data;

import ru.test.domain.*;
import ru.test.domain.Profile;
import ru.test.domain.exception.CsvParserException;
import ru.test.domain.exception.ErrorCode;

import java.io.*;
import java.util.List;

public class CsvSaver {
    private static final String HEADER = "id;type;name;city;birthyear;groups";

    public void save(File file, List<Profile> profiles) throws CsvParserException{
        if(file == null){
            throw new CsvParserException(ErrorCode.FILE_NOT_FOUND, "Файл не указан");
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))){
            writer.write(HEADER);
            writer.newLine();

            for(Profile p : profiles){
                String type = "USER";
                String groups = "";

                if(p instanceof AdminProfile admin){
                    type="ADMIN";
                    groups = String.join(",", admin.getGroups());
                } else if (p instanceof DeletedProfile){
                    type = "DELETED";
                }
                String line = String.format("%d;%s;%s;%s;%d;%s",
                        p.getId(),
                        type,
                        p.getName(),
                        p.getCity(),
                        p.getBirthYear(),
                        groups
                );

                writer.write(line);
                writer.newLine();
            }
        }
        catch(IOException e){
            throw new CsvParserException(ErrorCode.FILE_READ_ERROR, "Ошибка записи файла: "+ e.getMessage());
        }
    }
}

package ru.test.data.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import ru.test.data.serialization.adapter.ProfileTypeAdapter;
import ru.test.data.serialization.dto.GraphDto;
import ru.test.domain.Profile;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class JsonGraphSerializer implements Serializer<GraphDto> {

    private final Gson gson;

    public JsonGraphSerializer() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Profile.class, new ProfileTypeAdapter())
                .create();
    }

    @Override
    public String getFormatName() {
        return "JSON (*.json)";
    }

    @Override
    public String getExtension() {
        return "json";
    }

    @Override
    public void save(GraphDto data, File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("Целевой файл не может быть null");
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        }
    }

    @Override
    public GraphDto load(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException("Файл не существует или путь равен null");
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            GraphDto snapshot = gson.fromJson(reader, GraphDto.class);
            if (snapshot == null) {
                throw new IOException("Файл пуст или содержит некорректный JSON");
            }
            if (snapshot.getSchemaVersion() != BinaryGraphSerializer.CURRENT_SCHEMA_VERSION) {
                throw new IOException("Неподдерживаемая версия схемы JSON: " + snapshot.getSchemaVersion());
            }
            return snapshot;
        } catch (JsonParseException e) {
            throw new IOException("Ошибка парсинга JSON: " + e.getMessage(), e);
        }
    }
}
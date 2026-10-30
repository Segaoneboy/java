package ru.test.benchmark.benchmark;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.test.data.serialization.BinaryGraphSerializer;
import ru.test.data.serialization.JsonGraphSerializer;
import ru.test.data.serialization.dto.GraphDto;
import ru.test.domain.AdminProfile;
import ru.test.domain.Profile;
import ru.test.domain.UserProfile;
import ru.test.domain.datastructure.Connections;
import ru.test.domain.datastructure.Graph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SerializationBenchmark {

    private static final int RECORDS_COUNT = 10_000;
    private static final int ITERATIONS = 5;

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Замеры времени и объёма файлов для отчёта (10^4 записей, 5 прогонов)")
    void runBenchmark() throws IOException {
        System.out.println("Генерация графа на " + RECORDS_COUNT + " вершин...");
        Graph graph = new Graph(RECORDS_COUNT);
        List<Profile> profileList = new ArrayList<>(RECORDS_COUNT);

        for (int i = 0; i < RECORDS_COUNT; i++) {
            Profile p;
            if (i % 10 == 0) {
                p = new AdminProfile(i, "Admin_" + i, "Город_" + (i % 50), 1985 + (i % 25),
                        new Connections[0], List.of("Root", "Group_" + (i % 5)));
            } else {
                p = new UserProfile(i, "User_" + i, "Город_" + (i % 50), 1990 + (i % 20),
                        new Connections[0]);
            }
            profileList.add(p);
            graph.addVertex(p);

            if (i > 0) {
                graph.addEdge(i - 1, i, (i % 10) + 1);
            }
        }

        GraphDto snapshot = graph.exportSnapshot(profileList);

        JsonGraphSerializer jsonSerializer = new JsonGraphSerializer();
        BinaryGraphSerializer binarySerializer = new BinaryGraphSerializer();

        File jsonFile = tempDir.resolve("benchmark_data.json").toFile();
        File binaryFile = tempDir.resolve("benchmark_data.bin").toFile();

        jsonSerializer.save(snapshot, jsonFile);
        jsonSerializer.load(jsonFile);
        binarySerializer.save(snapshot, binaryFile);
        binarySerializer.load(binaryFile);

        long jsonTotalSave = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            jsonSerializer.save(snapshot, jsonFile);
            jsonTotalSave += (System.nanoTime() - start);
        }
        long jsonAvgSaveMs = (jsonTotalSave / ITERATIONS) / 1_000_000;

        long jsonTotalLoad = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            jsonSerializer.load(jsonFile);
            jsonTotalLoad += (System.nanoTime() - start);
        }
        long jsonAvgLoadMs = (jsonTotalLoad / ITERATIONS) / 1_000_000;

        long binTotalSave = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            binarySerializer.save(snapshot, binaryFile);
            binTotalSave += (System.nanoTime() - start);
        }
        long binAvgSaveMs = (binTotalSave / ITERATIONS) / 1_000_000;

        long binTotalLoad = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            binarySerializer.load(binaryFile);
            binTotalLoad += (System.nanoTime() - start);
        }
        long binAvgLoadMs = (binTotalLoad / ITERATIONS) / 1_000_000;

        long jsonSizeBytes = jsonFile.length();
        long binSizeBytes = binaryFile.length();

        System.out.println("\n================ СВОДНАЯ ТАБЛИЦА ДЛЯ ОТЧЕТА ================");
        System.out.printf("%-15s | %-15s | %-15s | %-15s%n", "Формат", "Размер (КБ)", "Сохранение (мс)", "Загрузка (мс)");
        System.out.println("------------------------------------------------------------------");
        System.out.printf("%-15s | %-15.2f | %-15d | %-15d%n", "JSON (Gson)", jsonSizeBytes / 1024.0, jsonAvgSaveMs, jsonAvgLoadMs);
        System.out.printf("%-15s | %-15.2f | %-15d | %-15d%n", "Binary (Sega)", binSizeBytes / 1024.0, binAvgSaveMs, binAvgLoadMs);
        System.out.println("==================================================================\n");
    }
}
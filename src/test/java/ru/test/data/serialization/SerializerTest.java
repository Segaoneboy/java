package ru.test.data.serialization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.test.data.serialization.dto.EdgeDto;
import ru.test.data.serialization.dto.GraphDto;
import ru.test.domain.AdminProfile;
import ru.test.domain.DeletedProfile;
import ru.test.domain.Profile;
import ru.test.domain.UserProfile;
import ru.test.domain.datastructure.BFSResult;
import ru.test.domain.datastructure.Connections;
import ru.test.domain.datastructure.Graph;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SerializerTest {

    @TempDir
    Path tempDir;

    private JsonGraphSerializer jsonSerializer;
    private BinaryGraphSerializer binarySerializer;
    private Graph graph;
    private List<Profile> profiles;

    @BeforeEach
    void setUp() {
        jsonSerializer = new JsonGraphSerializer();
        binarySerializer = new BinaryGraphSerializer();
        graph = new Graph(16);
        profiles = new ArrayList<>();

        Profile user = new UserProfile(1, "Иван", "Москва", 1995, new Connections[0]);
        Profile admin = new AdminProfile(2, "Алиса", "СПб", 1990, new Connections[0], List.of("Root", "Admin"));
        Profile deleted = new DeletedProfile(3, "Бот", "Казань", 2000, new Connections[0]);

        profiles.addAll(List.of(user, admin, deleted));
        profiles.forEach(graph::addVertex);

        graph.addEdge(1, 2, 7);
        graph.addEdge(2, 3, 3);
    }

    @Test
    @DisplayName("1. Позитивный: Полный round-trip JSON с проверкой топологии графа через BFS")
    void testJsonRoundTrip() throws IOException {
        File file = tempDir.resolve("graph.json").toFile();
        GraphDto snapshot = graph.exportSnapshot(profiles);

        jsonSerializer.save(snapshot, file);
        assertThat(file).exists().isNotEmpty();

        GraphDto loaded = jsonSerializer.load(file);
        assertThat(loaded.getSchemaVersion()).isEqualTo(1);
        assertThat(loaded.getProfiles()).hasSize(3);
        assertThat(loaded.getEdges()).hasSize(2);

        Graph restoredGraph = new Graph(16);
        restoredGraph.restoreFromSnapshot(loaded);

        BFSResult bfs = restoredGraph.findShortestPath(1, 3);
        assertThat(bfs.isFound()).isTrue();
        assertThat(bfs.getPath()).containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("2. Позитивный: Полный round-trip Binary с проверкой топологии графа через BFS")
    void testBinaryRoundTrip() throws IOException {
        File file = tempDir.resolve("graph.bin").toFile();
        GraphDto snapshot = graph.exportSnapshot(profiles);

        binarySerializer.save(snapshot, file);
        assertThat(file).exists().isNotEmpty();

        GraphDto loaded = binarySerializer.load(file);
        assertThat(loaded.getSchemaVersion()).isEqualTo(1);
        assertThat(loaded.getProfiles()).hasSize(3);
        assertThat(loaded.getEdges()).hasSize(2);

        Graph restoredGraph = new Graph(16);
        restoredGraph.restoreFromSnapshot(loaded);

        BFSResult bfs = restoredGraph.findShortestPath(1, 3);
        assertThat(bfs.isFound()).isTrue();
        assertThat(bfs.getPath()).containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("3. Позитивный: Сохранение полиморфизма сущностей в JSON и Binary")
    void testPolymorphismPreserved() throws IOException {
        File fJson = tempDir.resolve("poly.json").toFile();
        File fBin = tempDir.resolve("poly.bin").toFile();

        GraphDto dto = graph.exportSnapshot(profiles);
        jsonSerializer.save(dto, fJson);
        binarySerializer.save(dto, fBin);

        GraphDto loadedJson = jsonSerializer.load(fJson);
        GraphDto loadedBin = binarySerializer.load(fBin);

        assertThat(loadedJson.getProfiles().get(0)).isInstanceOf(UserProfile.class);
        assertThat(loadedJson.getProfiles().get(1)).isInstanceOf(AdminProfile.class);
        assertThat(loadedJson.getProfiles().get(2)).isInstanceOf(DeletedProfile.class);
        assertThat(((AdminProfile) loadedJson.getProfiles().get(1)).getGroups()).containsExactly("Root", "Admin");

        assertThat(loadedBin.getProfiles().get(0)).isInstanceOf(UserProfile.class);
        assertThat(loadedBin.getProfiles().get(1)).isInstanceOf(AdminProfile.class);
        assertThat(loadedBin.getProfiles().get(2)).isInstanceOf(DeletedProfile.class);
        assertThat(((AdminProfile) loadedBin.getProfiles().get(1)).getGroups()).containsExactly("Root", "Admin");
    }

    @Test
    @DisplayName("4. Позитивный (граничный): Сериализация абсолютно пустого графа")
    void testEmptyGraphRoundTrip() throws IOException {
        File fJson = tempDir.resolve("empty.json").toFile();
        File fBin = tempDir.resolve("empty.bin").toFile();

        Graph emptyGraph = new Graph(16);
        GraphDto emptyDto = emptyGraph.exportSnapshot(List.of());

        jsonSerializer.save(emptyDto, fJson);
        binarySerializer.save(emptyDto, fBin);

        GraphDto loadedJson = jsonSerializer.load(fJson);
        GraphDto loadedBin = binarySerializer.load(fBin);

        assertThat(loadedJson.getProfiles()).isEmpty();
        assertThat(loadedJson.getEdges()).isEmpty();
        assertThat(loadedBin.getProfiles()).isEmpty();
        assertThat(loadedBin.getEdges()).isEmpty();
    }

    @Test
    @DisplayName("5. Негативный: Чтение поврежденного синтаксически JSON файла")
    void testCorruptedJsonThrowsException() throws IOException {
        File corruptFile = tempDir.resolve("corrupted.json").toFile();
        Files.writeString(corruptFile.toPath(), "{ \"schema_version\": 1, \"profiles\": [ broken json ... ");

        assertThatThrownBy(() -> jsonSerializer.load(corruptFile))
                .isInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("6. Негативный: Чтение бинарного файла с неверной сигнатурой (0x53656761)")
    void testInvalidBinaryMagicHeaderThrowsException() throws IOException {
        File fakeFile = tempDir.resolve("fake.bin").toFile();
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(fakeFile))) {
            dos.writeInt(0xDEADBEEF);
            dos.writeInt(1);
        }

        assertThatThrownBy(() -> binarySerializer.load(fakeFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Неверная сигнатура бинарного файла: 0xDEADBEEF");
    }

    @Test
    @DisplayName("7. Негативный: Неподдерживаемая версия схемы (файл из будущей версии)")
    void testFutureSchemaVersionThrowsException() throws IOException {
        File futureBin = tempDir.resolve("future.bin").toFile();
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(futureBin))) {
            dos.writeInt(BinaryGraphSerializer.MAGIC_HEADER);
            dos.writeInt(999);
        }

        assertThatThrownBy(() -> binarySerializer.load(futureBin))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Неподдерживаемая версия схемы: 999");
    }

    @Test
    @DisplayName("8. Негативный: Попытка чтения несуществующего файла")
    void testFileNotFoundThrowsException() {
        File nonExistent = tempDir.resolve("not_found.bin").toFile();

        assertThatThrownBy(() -> binarySerializer.load(nonExistent))
                .isInstanceOf(IOException.class);
        assertThatThrownBy(() -> jsonSerializer.load(nonExistent))
                .isInstanceOf(IOException.class);
    }
}
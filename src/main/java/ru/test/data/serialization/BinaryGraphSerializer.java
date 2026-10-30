package ru.test.data.serialization;

import ru.test.data.serialization.dto.EdgeDto;
import ru.test.data.serialization.dto.GraphDto;
import ru.test.domain.AdminProfile;
import ru.test.domain.DeletedProfile;
import ru.test.domain.Profile;
import ru.test.domain.UserProfile;
import ru.test.domain.datastructure.Connections;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BinaryGraphSerializer implements Serializer<GraphDto> {

    public static final int MAGIC_HEADER = 0x53656761;
    public static final int CURRENT_SCHEMA_VERSION = 1;

    private static final byte TYPE_USER = 1;
    private static final byte TYPE_ADMIN = 2;
    private static final byte TYPE_DELETED = 3;

    @Override
    public String getFormatName() {
        return "Custom Binary (*.bin)";
    }

    @Override
    public String getExtension() {
        return "bin";
    }

    @Override
    public void save(GraphDto data, File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("Целевой файл не может быть null");
        }

        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            dos.writeInt(MAGIC_HEADER);
            dos.writeInt(CURRENT_SCHEMA_VERSION);

            List<Profile> profiles = data.getProfiles() != null ? data.getProfiles() : List.of();
            dos.writeInt(profiles.size());

            for (Profile p : profiles) {
                if (p instanceof AdminProfile admin) {
                    dos.writeByte(TYPE_ADMIN);
                    writeBaseProfile(dos, p);
                    List<String> groups = admin.getGroups() != null ? admin.getGroups() : List.of();
                    dos.writeInt(groups.size());
                    for (String g : groups) {
                        dos.writeUTF(g != null ? g : "");
                    }
                } else if (p instanceof DeletedProfile) {
                    dos.writeByte(TYPE_DELETED);
                    writeBaseProfile(dos, p);
                } else {
                    dos.writeByte(TYPE_USER);
                    writeBaseProfile(dos, p);
                }
            }

            List<EdgeDto> edges = data.getEdges() != null ? data.getEdges() : List.of();
            dos.writeInt(edges.size());
            for (EdgeDto edge : edges) {
                dos.writeInt(edge.getFromId());
                dos.writeInt(edge.getFriendId());
                dos.writeInt(edge.getStrength());
            }
        }
    }

    private void writeBaseProfile(DataOutputStream dos, Profile p) throws IOException {
        dos.writeInt(p.getId());
        dos.writeUTF(p.getName() != null ? p.getName() : "");
        dos.writeUTF(p.getCity() != null ? p.getCity() : "");
        dos.writeInt(p.getBirthYear());
    }

    @Override
    public GraphDto load(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException("Файл не существует или путь равен null");
        }

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            int magic = dis.readInt();
            if (magic != MAGIC_HEADER) {
                throw new IOException(String.format("Неверная сигнатура бинарного файла: 0x%08X (ожидалось 0x%08X)", magic, MAGIC_HEADER));
            }

            int version = dis.readInt();
            if (version != CURRENT_SCHEMA_VERSION) {
                throw new IOException("Неподдерживаемая версия схемы: " + version);
            }

            int profileCount = dis.readInt();
            if (profileCount < 0) {
                throw new IOException("Повреждённые данные: отрицательное число профилей");
            }

            List<Profile> profiles = new ArrayList<>(profileCount);
            Connections[] dummyConnections = new Connections[0];

            for (int i = 0; i < profileCount; i++) {
                byte type = dis.readByte();
                int id = dis.readInt();
                String name = dis.readUTF();
                String city = dis.readUTF();
                int birthYear = dis.readInt();

                switch (type) {
                    case TYPE_USER -> profiles.add(new UserProfile(id, name, city, birthYear, dummyConnections));
                    case TYPE_DELETED -> profiles.add(new DeletedProfile(id, name, city, birthYear, dummyConnections));
                    case TYPE_ADMIN -> {
                        int groupCount = dis.readInt();
                        List<String> groups = new ArrayList<>(groupCount);
                        for (int g = 0; g < groupCount; g++) {
                            groups.add(dis.readUTF());
                        }
                        profiles.add(new AdminProfile(id, name, city, birthYear, dummyConnections, groups));
                    }
                    default -> throw new IOException("Неизвестный тип профиля в бинарном файле: " + type);
                }
            }

            int edgeCount = dis.readInt();
            if (edgeCount < 0) {
                throw new IOException("Повреждённые данные: отрицательное число рёбер");
            }

            List<EdgeDto> edges = new ArrayList<>(edgeCount);
            for (int i = 0; i < edgeCount; i++) {
                int from = dis.readInt();
                int to = dis.readInt();
                int strength = dis.readInt();
                edges.add(new EdgeDto(from, to, strength));
            }

            return new GraphDto(version, profiles, edges);
        }
    }
}
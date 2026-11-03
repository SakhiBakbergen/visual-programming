package kz.atu.lab10;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class NoteStorage {

    private final ObjectMapper mapper = new ObjectMapper();

    public void save(Path path, List<Note> notes) throws IOException {
        Path target = path.toAbsolutePath();
        Path tmp = Files.createTempFile(target.getParent(), "movies-", ".tmp");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), notes);
            try {
                Files.move(tmp, target,
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    public List<Note> load(Path path) throws IOException {
        List<Note> list = mapper.readValue(path.toFile(), new TypeReference<List<Note>>() {});
        if (list == null) {
            throw new IOException("Файл не содержит списка");
        }
        for (Note n : list) {
            if (n == null || n.getTitle() == null || n.getTitle().isBlank()) {
                throw new IOException("Некорректная запись: нет названия");
            }
            if (n.getBody() == null) {
                n.setBody("");
            }
            if (n.getStatus() == null || !Note.STATUSES.contains(n.getStatus())) {
                n.setStatus(Note.STATUSES.get(0));
            }
        }
        return list;
    }
}
package kz.atu.lab10;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class NotesController {

    private static final String ALL = "Все статусы";
    private static final String DEFAULT_FILE = "movies.json";

    @FXML private ListView<Note> listNotes;
    @FXML private TextField txtTitle;
    @FXML private TextArea txtBody;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbFilter;
    @FXML private Label lblStatus;
    @FXML private Label lblCount;

    private final ObservableList<Note> notes = FXCollections.observableArrayList();
    private FilteredList<Note> filtered;
    private final NoteStorage storage = new NoteStorage();

    private Path currentFile;
    private boolean dirty = false;

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList(Note.STATUSES));
        cbStatus.getSelectionModel().selectFirst();

        cbFilter.getItems().add(ALL);
        cbFilter.getItems().addAll(Note.STATUSES);
        cbFilter.getSelectionModel().selectFirst();

        filtered = new FilteredList<>(notes, n -> true);
        listNotes.setItems(filtered);
        listNotes.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Note n, boolean empty) {
                super.updateItem(n, empty);
                getStyleClass().removeAll("status-want", "status-watching", "status-done");
                if (empty || n == null) {
                    setText(null);
                    return;
                }
                setText(n.getTitle() + "\n" + n.getStatus());
                switch (n.getStatus()) {
                    case "Смотрю" -> getStyleClass().add("status-watching");
                    case "Просмотрено" -> getStyleClass().add("status-done");
                    default -> getStyleClass().add("status-want");
                }
            }
        });

        listNotes.getSelectionModel().selectedItemProperty().addListener((obs, old, note) -> {
            if (note != null) {
                txtTitle.setText(note.getTitle());
                txtBody.setText(note.getBody());
                cbStatus.setValue(note.getStatus());
            }
        });

        txtSearch.textProperty().addListener((obs, o, n) -> applyFilter());
        cbFilter.valueProperty().addListener((obs, o, n) -> applyFilter());
        filtered.addListener((ListChangeListener<Note>) c -> updateCount());

        loadStartupData();
        updateCount();
    }

    // ---------- Загрузка при запуске ----------

    private void loadStartupData() {
        Path p = Path.of(DEFAULT_FILE);
        if (Files.exists(p)) {
            try {
                notes.setAll(storage.load(p));
                currentFile = p.toAbsolutePath();
                status("Загружено из " + DEFAULT_FILE + ": " + notes.size());
                return;
            } catch (IOException ex) {
                status("Файл " + DEFAULT_FILE + " повреждён, загружены демоданные");
            }
        } else {
            status("Загружены демонстрационные данные (" + DemoData.movies().size() + " фильмов)");
        }
        notes.setAll(DemoData.movies());
    }

    // ---------- Автосохранение в movies.json ----------

    private void autoSave() {
        Path p = Path.of(DEFAULT_FILE);
        try {
            storage.save(p, List.copyOf(notes));
            currentFile = p.toAbsolutePath();
            dirty = false;
        } catch (IOException ex) {
            dirty = true;
            error("Не удалось сохранить файл " + DEFAULT_FILE + ": " + ex.getMessage());
        }
    }

    // ---------- Фильтрация ----------

    private void applyFilter() {
        String q = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        String st = cbFilter.getValue();
        filtered.setPredicate(n ->
                n.getTitle().toLowerCase().contains(q)
                        && (st == null || ALL.equals(st) || st.equals(n.getStatus())));
    }

    private void updateCount() {
        lblCount.setText("Показано: " + filtered.size() + " из " + notes.size());
    }

    // ---------- Добавление / изменение / удаление ----------

    @FXML
    private void onAdd() {
        String title = txtTitle.getText().trim();
        String body = txtBody.getText().trim();
        if (title.isBlank() || body.isBlank()) {
            error("Введите название и описание фильма.");
            return;
        }
        Note note = new Note(title, body, cbStatus.getValue());
        notes.add(note);
        updateCount();
        listNotes.getSelectionModel().select(note);
        status("Добавлен фильм: " + title);
        autoSave();
    }

    @FXML
    private void onEdit() {
        Note selected = listNotes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            error("Выберите фильм для изменения.");
            return;
        }
        String title = txtTitle.getText().trim();
        String body = txtBody.getText().trim();
        if (title.isBlank() || body.isBlank()) {
            error("Название и описание не должны быть пустыми.");
            return;
        }
        selected.setTitle(title);
        selected.setBody(body);
        selected.setStatus(cbStatus.getValue());

        int idx = notes.indexOf(selected);
        notes.set(idx, selected);
        listNotes.getSelectionModel().select(selected);
        status("Фильм изменён: " + title);
        autoSave();
    }

    @FXML
    private void onDelete() {
        Note selected = listNotes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            error("Выберите фильм для удаления.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить фильм «" + selected.getTitle() + "»?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            notes.remove(selected);
            clearForm();
            updateCount();
            status("Фильм удалён");
            autoSave();
        }
    }

    @FXML
    private void onResetFilter() {
        txtSearch.clear();
        cbFilter.getSelectionModel().selectFirst();
        status("Поиск и фильтр сброшены");
    }

    @FXML
    private void onClear() {
        listNotes.getSelectionModel().clearSelection();
        clearForm();
        status("Форма очищена");
    }

    private void clearForm() {
        txtTitle.clear();
        txtBody.clear();
        cbStatus.getSelectionModel().selectFirst();
    }

    // ---------- Ручное сохранение/открытие (кнопки сейчас убраны из интерфейса) ----------

    @FXML
    private void onSave() {
        FileChooser chooser = chooser();
        chooser.setTitle("Сохранить фильмы");
        chooser.setInitialFileName(currentFile != null ? currentFile.getFileName().toString() : DEFAULT_FILE);
        File file = chooser.showSaveDialog(listNotes.getScene().getWindow());
        if (file == null) return;

        Path path = file.toPath();
        if (!path.getFileName().toString().toLowerCase().endsWith(".json")) {
            path = path.resolveSibling(path.getFileName() + ".json");
        }
        try {
            storage.save(path, List.copyOf(notes));
            currentFile = path.toAbsolutePath();
            dirty = false;
            status("Сохранено: " + path.getFileName());
        } catch (IOException ex) {
            error("Не удалось сохранить файл: " + ex.getMessage());
        }
    }

    @FXML
    private void onOpen() {
        if (!confirmDiscard()) return;

        FileChooser chooser = chooser();
        chooser.setTitle("Открыть фильмы");
        File file = chooser.showOpenDialog(listNotes.getScene().getWindow());
        if (file == null) return;

        try {
            List<Note> loaded = storage.load(file.toPath());
            notes.setAll(loaded);
            currentFile = file.toPath().toAbsolutePath();
            dirty = false;
            clearForm();
            updateCount();
            status("Загружено фильмов: " + notes.size());
        } catch (IOException ex) {
            error("Не удалось прочитать JSON: файл повреждён или имеет неверный формат.\nТекущие данные сохранены без изменений.");
        }
    }

    public boolean confirmDiscard() {
        if (!dirty) return true;
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Есть несохранённые изменения. Продолжить без сохранения?",
                ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private FileChooser chooser() {
        FileChooser c = new FileChooser();
        c.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON (*.json)", "*.json"));
        return c;
    }

    // ---------- Сообщения ----------

    private void status(String text) {
        lblStatus.setText(text);
    }

    private void error(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
        status("Ошибка");
    }
}
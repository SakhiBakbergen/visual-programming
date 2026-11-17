package kz.atu.lab12.controller;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import kz.atu.lab12.model.Book;
import kz.atu.lab12.repository.BookRepository;

import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BookController {

    private static final Logger LOG = Logger.getLogger(BookController.class.getName());

    private static final String ALL_AUTHORS = "Все авторы";
    private static final int SHELF_LIMIT = 12;
    private static final int NEW_YEARS = 10;       // «Новинки» — книги последних 10 лет
    private static final int CLASSIC_BEFORE = 1990; // «Классика» — книги до 1990 года
    private static final String[] COVER_COLORS = {
            "#e8833a", "#d1495b", "#3d7ea6", "#4f9d69", "#8a6fa8", "#2f3e5c", "#c99a2e", "#5b8c85"
    };

    @FXML private TableView<Book> tableBooks;
    @FXML private TableColumn<Book, Number> colId;
    @FXML private TableColumn<Book, String> colTitle;
    @FXML private TableColumn<Book, String> colAuthor;
    @FXML private TableColumn<Book, Number> colYear;
    @FXML private TextField txtTitle;
    @FXML private TextField txtAuthor;
    @FXML private TextField txtYear;
    @FXML private TextField txtSearch;
    @FXML private Label lblStatus;
    @FXML private Label lblCount;
    @FXML private Label lblAuthorsCount;
    @FXML private ToggleGroup chipGroup;
    @FXML private ToggleButton chipAll;
    @FXML private ToggleButton chipNew;
    @FXML private ToggleButton chipClassic;
    @FXML private ListView<AuthorStat> listAuthors;
    @FXML private HBox shelf;

    private final BookRepository repository = new BookRepository();
    private final ObservableList<Book> items = FXCollections.observableArrayList();
    private FilteredList<Book> filtered;

    private String selectedAuthor;          // null — все авторы
    private boolean updatingAuthors;        // защита от рекурсии при перестроении списка

    /** Введённые пользователем и уже проверенные значения. */
    private record BookInput(String title, String author, int year) {}

    /** Строка списка «Топ авторов». */
    private record AuthorStat(String name, long count) {}

    @FXML
    private void initialize() {
        colId.setCellValueFactory(d -> new ReadOnlyIntegerWrapper(d.getValue().id()));
        colTitle.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().title()));
        colAuthor.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().author()));
        colYear.setCellValueFactory(d -> new ReadOnlyIntegerWrapper(d.getValue().year()));

        // items -> фильтр (поиск, чипы, автор) -> сортировка по заголовкам -> таблица
        filtered = new FilteredList<>(items, b -> true);
        SortedList<Book> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tableBooks.comparatorProperty());
        tableBooks.setItems(sorted);

        txtSearch.textProperty().addListener((obs, oldV, newV) -> applyFilter());

        chipGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                if (oldT != null) oldT.setSelected(true);   // один чип всегда выбран
            } else {
                applyFilter();
            }
        });

        listAuthors.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(AuthorStat a, boolean empty) {
                super.updateItem(a, empty);
                if (empty || a == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label name = new Label(a.name());
                name.getStyleClass().add("author-name");
                Label count = new Label(String.valueOf(a.count()));
                count.getStyleClass().add("author-count");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox row = new HBox(8, name, spacer, count);
                row.setAlignment(Pos.CENTER_LEFT);
                setText(null);
                setGraphic(row);
            }
        });
        listAuthors.getSelectionModel().selectedItemProperty().addListener((obs, oldV, a) -> {
            if (updatingAuthors || a == null) return;
            selectedAuthor = ALL_AUTHORS.equals(a.name()) ? null : a.name();
            applyFilter();
        });

        // выбор строки таблицы -> поля формы
        tableBooks.getSelectionModel().selectedItemProperty().addListener((obs, oldV, b) -> {
            if (b != null) {
                txtTitle.setText(b.title());
                txtAuthor.setText(b.author());
                txtYear.setText(String.valueOf(b.year()));
            }
        });

        try {
            repository.initializeDatabase();
            refresh();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Не удалось открыть базу данных", e);
            showError("Не удалось открыть базу данных");
        }
    }

    // ---------------------------------------------------------------- данные

    private void refresh() throws SQLException {
        items.setAll(repository.findAll());
        rebuildAuthors();
        buildShelf();
        applyFilter();
        lblStatus.setText("Записей: " + items.size());
    }

    // ---------------------------------------------------------------- фильтры

    private boolean matches(Book b) {
        String q = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        if (!q.isEmpty()
                && !b.title().toLowerCase().contains(q)
                && !b.author().toLowerCase().contains(q)) {
            return false;
        }
        if (selectedAuthor != null && !b.author().equals(selectedAuthor)) {
            return false;
        }
        Toggle chip = chipGroup.getSelectedToggle();
        if (chip == chipNew) {
            return b.year() >= Year.now().getValue() - NEW_YEARS;
        }
        if (chip == chipClassic) {
            return b.year() < CLASSIC_BEFORE;
        }
        return true;
    }

    private void applyFilter() {
        filtered.setPredicate(this::matches);
        lblCount.setText(booksWord(filtered.size()));
    }

    private void resetFilters() {
        selectedAuthor = null;
        txtSearch.clear();
        chipAll.setSelected(true);
        updatingAuthors = true;
        listAuthors.getSelectionModel().select(0);
        updatingAuthors = false;
        applyFilter();
    }

    // ---------------------------------------------------------------- «Топ авторов»

    private void rebuildAuthors() {
        Map<String, Long> counts = items.stream()
                .collect(Collectors.groupingBy(Book::author, Collectors.counting()));
        List<AuthorStat> authors = new ArrayList<>();
        counts.forEach((name, n) -> authors.add(new AuthorStat(name, n)));
        authors.sort((a, b) -> {
            int c = Long.compare(b.count(), a.count());
            return c != 0 ? c : a.name().compareToIgnoreCase(b.name());
        });
        List<AuthorStat> rows = new ArrayList<>();
        rows.add(new AuthorStat(ALL_AUTHORS, items.size()));
        rows.addAll(authors);

        updatingAuthors = true;
        listAuthors.getItems().setAll(rows);
        int idx = 0;
        if (selectedAuthor != null) {
            idx = -1;
            for (int i = 0; i < rows.size(); i++) {
                if (rows.get(i).name().equals(selectedAuthor)) idx = i;
            }
            if (idx < 0) {          // выбранного автора больше нет в каталоге
                selectedAuthor = null;
                idx = 0;
            }
        }
        listAuthors.getSelectionModel().select(idx);
        updatingAuthors = false;
        lblAuthorsCount.setText(authorsWord(authors.size()));
    }

    // ---------------------------------------------------------------- полка «Недавно добавленные»

    private void buildShelf() {
        shelf.getChildren().clear();
        if (items.isEmpty()) {
            Label empty = new Label("Здесь появятся добавленные книги");
            empty.getStyleClass().add("placeholder-label");
            shelf.getChildren().add(empty);
            return;
        }
        items.stream()
                .sorted((a, b) -> Integer.compare(b.id(), a.id()))
                .limit(SHELF_LIMIT)
                .forEach(b -> shelf.getChildren().add(createCard(b)));
    }

    private VBox createCard(Book b) {
        Label coverTitle = new Label(b.title());
        coverTitle.setWrapText(true);
        coverTitle.setTextAlignment(TextAlignment.CENTER);
        coverTitle.setMaxWidth(88);
        coverTitle.setMaxHeight(100);
        coverTitle.getStyleClass().add("cover-title");

        Label coverYear = new Label(String.valueOf(b.year()));
        coverYear.getStyleClass().add("cover-year");

        StackPane cover = new StackPane(coverTitle, coverYear);
        StackPane.setAlignment(coverYear, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(coverYear, new Insets(0, 8, 6, 0));
        cover.setPrefSize(104, 140);
        cover.setMinSize(104, 140);
        cover.setMaxSize(104, 140);
        cover.getStyleClass().add("book-cover");
        String color = COVER_COLORS[Math.floorMod(b.title().hashCode(), COVER_COLORS.length)];
        cover.setStyle("-fx-background-color: " + color + ";");

        Label title = new Label(b.title());
        title.setWrapText(true);
        title.setPrefWidth(104);
        title.setMaxWidth(104);
        title.setMinHeight(36);
        title.setPrefHeight(36);
        title.setMaxHeight(36);
        title.setAlignment(Pos.TOP_LEFT);
        title.getStyleClass().add("book-title");

        Label author = new Label(b.author());
        author.setPrefWidth(104);
        author.setMaxWidth(104);
        author.getStyleClass().add("book-author");

        VBox card = new VBox(6, cover, title, author);
        card.setPrefWidth(104);
        card.setMinWidth(104);
        card.getStyleClass().add("book-card");
        Tooltip.install(card, new Tooltip(b.title() + " — " + b.author() + ", " + b.year()));
        card.setOnMouseClicked(e -> {
            resetFilters();
            tableBooks.getSelectionModel().select(b);
            tableBooks.scrollTo(b);
        });
        return card;
    }

    // ---------------------------------------------------------------- валидация

    /** Проверяет поля формы. При ошибке бросает IllegalArgumentException с понятным текстом. */
    private BookInput readInput() {
        String title = txtTitle.getText() == null ? "" : txtTitle.getText().trim();
        String author = txtAuthor.getText() == null ? "" : txtAuthor.getText().trim();
        if (title.isBlank() || author.isBlank()) {
            throw new IllegalArgumentException("Заполните название и автора");
        }
        int year;
        try {
            year = Integer.parseInt(txtYear.getText() == null ? "" : txtYear.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Год должен быть целым числом");
        }
        int maxYear = Year.now().getValue();
        if (year < 1 || year > maxYear) {
            throw new IllegalArgumentException("Год должен быть от 1 до " + maxYear);
        }
        return new BookInput(title, author, year);
    }

    // ---------------------------------------------------------------- CRUD

    @FXML
    private void onAdd() {
        try {
            BookInput in = readInput();
            repository.insert(in.title(), in.author(), in.year());
            refresh();
            resetFilters();      // чтобы новая книга точно была видна в таблице
            clearFields();
            lblStatus.setText("Книга добавлена. Записей: " + items.size());
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Ошибка INSERT", e);
            showError("Ошибка сохранения в БД");
        }
    }

    @FXML
    private void onUpdate() {
        Book selected = tableBooks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите книгу в таблице");
            return;
        }
        try {
            BookInput in = readInput();
            int rows = repository.update(selected.id(), in.title(), in.author(), in.year());
            refresh();
            lblStatus.setText(rows > 0 ? "Изменения сохранены" : "Запись не найдена");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Ошибка UPDATE", e);
            showError("Ошибка сохранения в БД");
        }
    }

    @FXML
    private void onDelete() {
        Book selected = tableBooks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите книгу в таблице");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить книгу «" + selected.title() + "»?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText(null);
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            lblStatus.setText("Удаление отменено");   // данные остаются
            return;
        }
        try {
            int rows = repository.delete(selected.id());
            refresh();
            clearFields();
            lblStatus.setText(rows > 0 ? "Книга удалена. Записей: " + items.size() : "Запись не найдена");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Ошибка DELETE", e);
            showError("Ошибка удаления из БД");
        }
    }

    @FXML
    private void onRefresh() {
        try {
            refresh();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Ошибка SELECT", e);
            showError("Не удалось прочитать данные из БД");
        }
    }

    // ---------------------------------------------------------------- вспомогательное

    private void clearFields() {
        txtTitle.clear();
        txtAuthor.clear();
        txtYear.clear();
    }

    private void showError(String message) {
        lblStatus.setText(message);
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private static String booksWord(int n) {
        int m100 = n % 100, m10 = n % 10;
        String w = (m100 >= 11 && m100 <= 14) ? "книг"
                : m10 == 1 ? "книга"
                : (m10 >= 2 && m10 <= 4) ? "книги" : "книг";
        return n + " " + w;
    }

    private static String authorsWord(int n) {
        int m100 = n % 100, m10 = n % 10;
        String w = (m100 >= 11 && m100 <= 14) ? "авторов"
                : m10 == 1 ? "автор"
                : (m10 >= 2 && m10 <= 4) ? "автора" : "авторов";
        return n + " " + w;
    }
}

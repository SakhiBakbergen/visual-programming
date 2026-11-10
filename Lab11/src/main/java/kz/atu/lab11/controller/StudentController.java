package kz.atu.lab11.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import kz.atu.lab11.model.Student;
import kz.atu.lab11.service.StudentService;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class StudentController {
    @FXML private TableView<Student> tableStudents;
    @FXML private TableColumn<Student, String> colName, colGroup;
    @FXML private TableColumn<Student, Number> colGpa;

    @FXML private TextField txtName, txtGroup, txtGpa, txtSearch;
    @FXML private Label lblErrName, lblErrGroup, lblErrGpa, lblCount;
    @FXML private Label lblStatTotal, lblStatAvgGpa, lblStatMaxGpa, lblStatGroupsCount;
    @FXML private ComboBox<String> cmbGroupFilter;
    @FXML private Button btnUpdate, btnDelete;

    private final StudentService service = new StudentService();
    private FilteredList<Student> filtered;

    @FXML
    private void initialize() {
        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        colGroup.setCellValueFactory(c -> c.getValue().groupProperty());
        colGpa.setCellValueFactory(c -> c.getValue().gpaProperty());

        filtered = new FilteredList<>(service.getStudents(), s -> true);
        SortedList<Student> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tableStudents.comparatorProperty());
        tableStudents.setItems(sorted);

        updateGroupFilterItems();
        cmbGroupFilter.setValue("Все");

        txtSearch.textProperty().addListener((o, a, b) -> applyFilter());
        cmbGroupFilter.valueProperty().addListener((o, a, b) -> applyFilter());

        service.getStudents().addListener((javafx.collections.ListChangeListener.Change<? extends Student> c) -> {
            updateGroupFilterItems();
            updateCount();
        });

        tableStudents.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = (newVal != null);
            btnUpdate.setDisable(!hasSelection);
            btnDelete.setDisable(!hasSelection);

            if (newVal != null) {
                txtName.setText(newVal.getName());
                txtGroup.setText(newVal.getGroup());
                txtGpa.setText(String.valueOf(newVal.getGpa()));
                clearErrors();
            }
        });

        // Прячем ошибки при первоначальном открытии окна
        clearErrors();
        updateCount();
    }

    @FXML
    private void onAdd() {
        if (!validateFields()) return;
        try {
            service.addStudent(txtName.getText(), txtGroup.getText(), Double.parseDouble(txtGpa.getText().replace(",", ".")));
            applyFilter();
            clearFields();
        } catch (IllegalArgumentException e) {
            showErrorDialog(e.getMessage());
        }
    }

    @FXML
    private void onUpdate() {
        Student s = tableStudents.getSelectionModel().getSelectedItem();
        if (s == null || !validateFields()) return;
        try {
            service.updateStudent(s, txtName.getText(), txtGroup.getText(), Double.parseDouble(txtGpa.getText().replace(",", ".")));
            applyFilter();
        } catch (IllegalArgumentException e) {
            showErrorDialog(e.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        Student s = tableStudents.getSelectionModel().getSelectedItem();
        if (s == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удаление записи");
        confirm.setContentText("Вы действительно хотите удалить студента: " + s.getName() + "?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            service.deleteStudent(s);
            applyFilter();
            clearFields();
        }
    }

    @FXML
    private void onClear() {
        clearFields();
    }

    @FXML
    private void onExportCsv() {
        try (PrintWriter pw = new PrintWriter("students_export.csv", "UTF-8")) {
            pw.println("ФИО;Группа;GPA");
            for (Student s : filtered) {
                pw.println(s.getName() + ";" + s.getGroup() + ";" + s.getGpa());
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Данные сохранены в файл students_export.csv", ButtonType.OK);
            alert.setHeaderText(null);
            alert.setTitle("Экспорт завершён");
            alert.showAndWait();
        } catch (Exception e) {
            showErrorDialog("Не удалось экспортировать файл: " + e.getMessage());
        }
    }

    @FXML
    private void onAbout() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("О программе");
        info.setHeaderText("Каталог студентов v1.0");
        info.setContentText("Лабораторная работа №11\nДисциплина: Визуальное программирование\nАрхитектура: MVC + JavaFX Properties");
        info.showAndWait();
    }

    private boolean validateFields() {
        clearErrors();
        boolean valid = true;

        if (txtName.getText().isBlank()) {
            showFieldError(lblErrName, "ФИО не может быть пустым");
            valid = false;
        }
        if (txtGroup.getText().isBlank()) {
            showFieldError(lblErrGroup, "Укажите группу");
            valid = false;
        }
        try {
            double gpa = Double.parseDouble(txtGpa.getText().replace(",", "."));
            if (gpa < 0.0 || gpa > 4.0) {
                showFieldError(lblErrGpa, "GPA должен быть от 0.0 до 4.0");
                valid = false;
            }
        } catch (NumberFormatException e) {
            showFieldError(lblErrGpa, "Введите корректное число");
            valid = false;
        }
        return valid;
    }

    private void showFieldError(Label lbl, String text) {
        lbl.setText(text);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void clearErrors() {
        lblErrName.setVisible(false); lblErrName.setManaged(false);
        lblErrGroup.setVisible(false); lblErrGroup.setManaged(false);
        lblErrGpa.setVisible(false); lblErrGpa.setManaged(false);
    }

    private void applyFilter() {
        String q = txtSearch.getText().trim().toLowerCase();
        String group = cmbGroupFilter.getValue();
        filtered.setPredicate(s -> s.getName().toLowerCase().contains(q)
                && (group == null || group.equals("Все") || s.getGroup().equals(group)));
        updateCount();
    }

    private void updateGroupFilterItems() {
        String current = cmbGroupFilter.getValue();
        Set<String> groups = new HashSet<>();
        groups.add("Все");
        for (Student s : service.getStudents()) groups.add(s.getGroup());
        cmbGroupFilter.setItems(FXCollections.observableArrayList(groups));
        cmbGroupFilter.setValue(groups.contains(current) ? current : "Все");
    }

    private void updateCount() {
        lblCount.setText("Найдено записей: " + filtered.size() + " из " + service.getStudents().size());
        lblStatTotal.setText("Всего студентов: " + service.getStudents().size());
        lblStatAvgGpa.setText(String.format("Средний GPA: %.2f", service.getAverageGpa()));
        lblStatMaxGpa.setText(String.format("Лучший GPA: %.2f", service.getMaxGpa()));
        lblStatGroupsCount.setText("Всего групп: " + service.getGroupsCount());
    }

    private void clearFields() {
        txtName.clear();
        txtGroup.clear();
        txtGpa.clear();
        tableStudents.getSelectionModel().clearSelection();
        clearErrors();
    }

    private void showErrorDialog(String m) {
        Alert a = new Alert(Alert.AlertType.ERROR, m, ButtonType.OK);
        a.setTitle("Ошибка");
        a.setHeaderText(null);
        a.showAndWait();
    }
}
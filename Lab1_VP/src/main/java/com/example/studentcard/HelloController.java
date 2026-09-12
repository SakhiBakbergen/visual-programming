package com.example.studentcard;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class HelloController {
    @FXML private TextField txtName;
    @FXML private TextField txtSurname;
    @FXML private TextField txtAge;
    @FXML private TextField txtSpeciality;
    @FXML private ComboBox<String> cmbCourse;

    @FXML private ComboBox<String> cmbCity;
    @FXML private RadioButton rbFullTime;
    @FXML private RadioButton rbDistance;
    @FXML private CheckBox chkDormitory;

    @FXML private Label lblResult;

    @FXML
    public void initialize() {
        cmbCourse.getItems().addAll("1 курс", "2 курс", "3 курс", "4 курс");
        cmbCourse.getSelectionModel().selectFirst();

        cmbCity.getItems().addAll("Алматы", "Астана", "Шымкент", "Караганда", "Актобе");
        cmbCity.getSelectionModel().selectFirst();

        rbFullTime.setSelected(true);
    }

    @FXML
    private void onCreateClick() {
        String name = txtName.getText().trim();
        String surname = txtSurname.getText().trim();
        String age = txtAge.getText().trim();
        String speciality = txtSpeciality.getText().trim();
        String course = cmbCourse.getValue();

        String city = cmbCity.getValue();
        String studyMode = rbFullTime.isSelected() ? "Очная" : "Дистанционная";
        boolean hasDormitory = chkDormitory.isSelected();

        if (name.isBlank() || surname.isBlank() || age.isBlank() || speciality.isBlank()) {
            showError("Заполните все текстовые поля.");
            return;
        }

        int studentAge;
        try {
            studentAge = Integer.parseInt(age);
        } catch (NumberFormatException e) {
            showError("Возраст должен быть числом.");
            return;
        }

        if (studentAge < 16 || studentAge > 100) {
            showError("Введите корректный возраст (от 16 до 100).");
            return;
        }

        lblResult.setText(String.format(
                "Студент: %s %s\nВозраст: %d\nСпециальность: %s\nКурс: %s\nГород: %s\nФорма обучения: %s\nОбщежитие: %s",
                surname, name, studentAge, speciality, course, city, studyMode, (hasDormitory ? "Да" : "Нет")
        ));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onClearClick() {
        txtName.clear();
        txtSurname.clear();
        txtAge.clear();
        txtSpeciality.clear();
        cmbCourse.getSelectionModel().selectFirst();
        cmbCity.getSelectionModel().selectFirst();
        rbFullTime.setSelected(true);
        chkDormitory.setSelected(false);
        lblResult.setText("");
        txtName.requestFocus();
    }

    @FXML
    private void onExitClick() {
        Platform.exit();
    }
}

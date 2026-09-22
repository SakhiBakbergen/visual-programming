package kz.atu.lab4.lab4;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class HelloController {
    @FXML private TextField txtFullName;
    @FXML private TextField txtGroup;
    @FXML private ComboBox<String> cmbCourse;
    @FXML private TableView<Student> tblStudents;
    @FXML private Label lblStatus;

    @FXML
    public void initialize() {
        cmbCourse.getItems().addAll("1 курс", "2 курс", "3 курс", "4 курс");
        cmbCourse.getSelectionModel().selectFirst();
    }

    @FXML
    private void onSaveClick() {
        String name = txtFullName.getText().trim();
        String group = txtGroup.getText().trim();
        String course = cmbCourse.getValue();

        if (name.isBlank() || group.isBlank()) {
            lblStatus.setText("Статус: заполните ФИО и группу");
            return;
        }

        lblStatus.setText("Статус: данные сохранены (" + name + ", " + group + ", " + course + ")");
    }

    @FXML
    private void onClearClick() {
        txtFullName.clear();
        txtGroup.clear();
        cmbCourse.getSelectionModel().selectFirst();
        lblStatus.setText("Статус: форма очищена");
        txtFullName.requestFocus();
    }
}
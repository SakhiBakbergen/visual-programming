package kz.atu.lab09;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class HelloController {
    @FXML private TextField txtName, txtEmail, txtAge, txtBirthDate;
    @FXML private ComboBox<String> cmbFormat;
    @FXML private CheckBox chkConsent;
    @FXML private Label lblNameError, lblEmailError, lblAgeError, lblBirthDateError, lblFormatError, lblConsentError;

    @FXML private TableView<Participant> tableParticipants;
    @FXML private TableColumn<Participant, String> colName, colEmail, colFormat;
    @FXML private TableColumn<Participant, Integer> colAge;
    @FXML private TableColumn<Participant, LocalDate> colBirthDate;

    private final ObservableList<Participant> participants = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cmbFormat.getItems().addAll("Очный", "Заочный", "Гибридный");

        colName.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getName()));
        colEmail.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getEmail()));
        colAge.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getAge()).asObject());
        colBirthDate.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getBirthDate()));
        colFormat.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getFormat()));

        tableParticipants.setItems(participants);
    }

    @FXML
    private void onRegister() {
        ValidationResult result = RegistrationValidator.validate(
                txtName.getText(), txtEmail.getText(), txtAge.getText(),
                cmbFormat.getValue(), chkConsent.isSelected(), txtBirthDate.getText()
        );

        showErrors(result);
        if (!result.isValid()) return;

        try {
            participants.add(new Participant(
                    txtName.getText().trim(), txtEmail.getText().trim(),
                    Integer.parseInt(txtAge.getText().trim()), cmbFormat.getValue(),
                    LocalDate.parse(txtBirthDate.getText().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            ));
            clearForm();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка добавления").showAndWait();
        }
    }

    @FXML
    private void clearForm() {
        txtName.clear(); txtEmail.clear(); txtAge.clear(); txtBirthDate.clear();
        cmbFormat.setValue(null); chkConsent.setSelected(false);
        showErrors(new ValidationResult(Map.of()));
    }

    private void showErrors(ValidationResult r) {
        lblNameError.setText(r.error("name"));
        lblEmailError.setText(r.error("email"));
        lblAgeError.setText(r.error("age"));
        lblBirthDateError.setText(r.error("birthDate"));
        lblFormatError.setText(r.error("format"));
        lblConsentError.setText(r.error("consent"));
    }
}
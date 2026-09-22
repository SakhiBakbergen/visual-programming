module kz.atu.lab4.lab4 {
    requires javafx.controls;
    requires javafx.fxml;


    opens kz.atu.lab4.lab4 to javafx.fxml;
    exports kz.atu.lab4.lab4;
}
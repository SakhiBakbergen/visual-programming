package kz.atu.lab12;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/** Точка входа: загружает FXML-разметку и показывает главное окно. */
public class BookApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(BookApplication.class.getResource("book-view.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 900);
        stage.setTitle("Каталог книг");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(760);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

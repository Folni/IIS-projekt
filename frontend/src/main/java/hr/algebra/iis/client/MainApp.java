package hr.algebra.iis.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * KORAK 6: JavaFX desktop aplikacija.
 * Ulazna točka - prikazuje login prozor.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/hr/algebra/iis/client/view/LoginView.fxml"));
        BorderPane root = loader.load();

        Scene scene = new Scene(root, 450, 350);
        scene.getStylesheets().add(
            getClass().getResource("/hr/algebra/iis/client/view/style.css").toExternalForm());

        primaryStage.setTitle("IIS Projekt - WooCommerce Products");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

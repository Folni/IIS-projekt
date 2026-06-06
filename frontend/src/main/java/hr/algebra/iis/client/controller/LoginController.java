package hr.algebra.iis.client.controller;

import hr.algebra.iis.client.service.ApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * KORAK 6: Login kontroler.
 * Dvije uloge: READ_ONLY (viewer) i FULL_ACCESS (admin).
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        // Enter tipka pokreće login
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Unesite korisničko ime i lozinku.");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Prijava...");
        errorLabel.setText("");

        // Login u pozadinskoj niti da ne blokira UI
        new Thread(() -> {
            try {
                boolean success = ApiClient.login(username, password);
                Platform.runLater(() -> {
                    if (success) {
                        openMainWindow();
                    } else {
                        showError("Neispravan username ili lozinka.");
                        loginButton.setDisable(false);
                        loginButton.setText("Prijava");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Greška pri spajanju na server: " + e.getMessage());
                    loginButton.setDisable(false);
                    loginButton.setText("Prijava");
                });
            }
        }).start();
    }

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/hr/algebra/iis/client/view/MainView.fxml"));
            BorderPane root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            scene.getStylesheets().add(
                getClass().getResource("/hr/algebra/iis/client/view/style.css")
                    .toExternalForm());

            stage.setTitle("IIS Projekt - WooCommerce Products [" +
                ApiClient.getUsername() + " - " + ApiClient.getUserRole() + "]");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Greška pri otvaranju glavnog prozora: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}

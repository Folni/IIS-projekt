package hr.algebra.iis.client.controller;

import hr.algebra.iis.client.model.ProductModel;
import hr.algebra.iis.client.model.WeatherModel;
import hr.algebra.iis.client.service.ApiClient;
import hr.algebra.iis.client.service.GrpcClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * KORAK 6: Glavni kontroler aplikacije.
 * Upravljanje svim tabovima i funkcionalnostima.
 */
public class MainController {

    // =================== Proizvodi tab ===================
    @FXML private TableView<ProductModel> productsTable;
    @FXML private TableColumn<ProductModel, Long> idCol;
    @FXML private TableColumn<ProductModel, String> nameCol;
    @FXML private TableColumn<ProductModel, String> priceCol;
    @FXML private TableColumn<ProductModel, String> typeCol;
    @FXML private TableColumn<ProductModel, String> statusCol;
    @FXML private TableColumn<ProductModel, Integer> stockCol;
    @FXML private TableColumn<ProductModel, String> skuCol;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Label statusLabel;

    // =================== XML/JSON Validacija tab ===================
    @FXML private TextArea xmlInput;
    @FXML private TextArea jsonInput;
    @FXML private TextArea validationResult;

    // =================== SOAP tab ===================
    @FXML private TextField soapSearchField;
    @FXML private TextArea soapResult;

    // =================== gRPC tab ===================
    @FXML private TextField cityField;
    @FXML private TableView<WeatherModel> weatherTable;
    @FXML private TableColumn<WeatherModel, String> cityWeatherCol;
    @FXML private TableColumn<WeatherModel, String> tempCol;
    @FXML private TableColumn<WeatherModel, String> humidityCol;
    @FXML private TableColumn<WeatherModel, String> pressureCol;
    @FXML private TableColumn<WeatherModel, String> windCol;

    // =================== Opće ===================
    @FXML private Label userInfoLabel;
    @FXML private Label bottomStatus;

    private final ObservableList<ProductModel> products = FXCollections.observableArrayList();
    private final ObservableList<WeatherModel> weatherData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupProductsTable();
        setupWeatherTable();
        setupUserInfo();
        setupAccessControl();
        loadProducts();

        // Popuni XML i JSON input s primjerom
        xmlInput.setText("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<tns:product xmlns:tns=\"http://algebra.hr/iis/product\">\n" +
        "  <tns:name>Primjer proizvoda</tns:name>\n" +
        "  <tns:type>simple</tns:type>\n" +
        "  <tns:status>publish</tns:status>\n" +
        "  <tns:sku>PROD-001</tns:sku>\n" +
        "  <tns:price>29.99</tns:price>\n" +
        "</tns:product>");

        jsonInput.setText("{\n  \"name\": \"Primjer proizvoda\",\n  \"price\": \"29.99\",\n  \"type\": \"simple\"\n}");
    }

    // =================== Postavljanje tablica ===================

    private void setupProductsTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        skuCol.setCellValueFactory(new PropertyValueFactory<>("sku"));
        productsTable.setItems(products);

        // Omogući edit/delete kad je odabran red
        productsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                boolean selected = newVal != null;
                editButton.setDisable(!selected || !ApiClient.isFullAccess());
                deleteButton.setDisable(!selected || !ApiClient.isFullAccess());
            });
    }

    private void setupWeatherTable() {
        cityWeatherCol.setCellValueFactory(new PropertyValueFactory<>("city"));
        tempCol.setCellValueFactory(new PropertyValueFactory<>("temperature"));
        humidityCol.setCellValueFactory(new PropertyValueFactory<>("humidity"));
        pressureCol.setCellValueFactory(new PropertyValueFactory<>("pressure"));
        windCol.setCellValueFactory(new PropertyValueFactory<>("wind"));
        weatherTable.setItems(weatherData);
    }

    private void setupUserInfo() {
        userInfoLabel.setText("Prijavljeni: " + ApiClient.getUsername() +
            " (" + ApiClient.getUserRole() + ")");
    }

    private void setupAccessControl() {
        // READ_ONLY ne može dodavati/uređivati/brisati
        boolean fullAccess = ApiClient.isFullAccess();
        addButton.setDisable(!fullAccess);
        if (!fullAccess) {
            addButton.setTooltip(new Tooltip("Samo FULL_ACCESS korisnici mogu dodavati proizvode"));
        }
    }

    // =================== Proizvodi CRUD ===================

    @FXML
    private void handleRefresh() {
        loadProducts();
    }

    private void loadProducts() {
        setStatus("Učitavanje...");
        new Thread(() -> {
            try {
                List<Map<String, Object>> data = ApiClient.getAllProducts();
                List<ProductModel> list = data.stream().map(p -> new ProductModel(
                    toLong(p.get("id")),
                    str(p.get("name")),
                    str(p.get("price")),
                    str(p.get("type")),
                    str(p.get("status")),
                    toInt(p.get("stockQuantity")),
                    str(p.get("sku")),
                    str(p.get("description")),
                    str(p.get("stockStatus"))
                )).toList();

                Platform.runLater(() -> {
                    products.setAll(list);
                    setStatus("Učitano " + list.size() + " proizvoda");
                });
            } catch (Exception e) {
                Platform.runLater(() -> setStatus("Greška: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleAddProduct() {
        showProductDialog(null);
    }

    @FXML
    private void handleEditProduct() {
        ProductModel selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected != null) showProductDialog(selected);
    }

    @FXML
    private void handleDeleteProduct() {
        ProductModel selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Obrisati proizvod \"" + selected.getName() + "\"?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Potvrda brisanja");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            new Thread(() -> {
                try {
                    ApiClient.deleteProduct(selected.getId());
                    Platform.runLater(() -> {
                        loadProducts();
                        setStatus("Proizvod obrisan.");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> setStatus("Greška pri brisanju: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void showProductDialog(ProductModel existing) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/hr/algebra/iis/client/view/ProductDialog.fxml"));
            VBox root = loader.load();
            ProductDialogController dc = loader.getController();

            if (existing != null) dc.setProduct(existing);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(existing == null ? "Novi proizvod" : "Uredi proizvod");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/hr/algebra/iis/client/view/style.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();

            if (dc.isSaved()) {
                Map<String, Object> data = dc.getProductData();
                new Thread(() -> {
                    try {
                        if (existing == null) {
                            ApiClient.createProduct(data);
                        } else {
                            ApiClient.updateProduct(existing.getId(), data);
                        }
                        Platform.runLater(() -> {
                            loadProducts();
                            setStatus(existing == null ? "Proizvod kreiran." : "Proizvod ažuriran.");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> setStatus("Greška: " + e.getMessage()));
                    }
                }).start();
            }
        } catch (IOException e) {
            setStatus("Greška pri otvaranju dijaloga: " + e.getMessage());
        }
    }

    // =================== XML/JSON Validacija ===================

    @FXML
    private void handleValidateBoth() {
        String xml = xmlInput.getText();
        String json = jsonInput.getText();

        new Thread(() -> {
            try {
                // Poziv na /api/products/validate
                String result = ApiClient.validateXmlAndJson(xml, json);
                Platform.runLater(() -> validationResult.setText(result));
            } catch (Exception e) {
                Platform.runLater(() -> validationResult.setText("Greška: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleValidateXml() {
        new Thread(() -> {
            try {
                String result = ApiClient.validateGeneratedXml();
                Platform.runLater(() -> validationResult.setText(result));
            } catch (Exception e) {
                Platform.runLater(() -> validationResult.setText("Greška: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleGenerateXml() {
        new Thread(() -> {
            try {
                String result = ApiClient.generateXml();
                Platform.runLater(() -> validationResult.setText(result));
            } catch (Exception e) {
                Platform.runLater(() -> validationResult.setText("Greška: " + e.getMessage()));
            }
        }).start();
}

    // =================== SOAP Pretraga ===================

    @FXML
    private void handleSoapSearch() {
        String term = soapSearchField.getText().trim();
        if (term.isEmpty()) {
            soapResult.setText("Unesite pojam za pretragu.");
            return;
        }

        soapResult.setText("Pretraživanje...");
        new Thread(() -> {
            try {
                String result = ApiClient.callSoapSearch(term);
                Platform.runLater(() -> soapResult.setText(result));
            } catch (Exception e) {
                Platform.runLater(() -> soapResult.setText("Greška: " + e.getMessage()));
            }
        }).start();
    }

    // =================== gRPC Temperatura ===================

    @FXML
    private void handleGetTemperature() {
        String city = cityField.getText().trim();
        if (city.isEmpty()) {
            setStatus("Unesite naziv grada.");
            return;
        }

        weatherData.clear();
        setStatus("Dohvaćanje temperature...");

        new Thread(() -> {
            try {
                List<WeatherModel> data = GrpcClient.getTemperature(city);
                Platform.runLater(() -> {
                    weatherData.setAll(data);
                    setStatus("Temperatura dohvaćena za: " + city);
                });
            } catch (Exception e) {
                Platform.runLater(() -> setStatus("gRPC greška: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleGetTemperatureStream() {
        String city = cityField.getText().trim();
        if (city.isEmpty()) {
            setStatus("Unesite naziv grada.");
            return;
        }

        weatherData.clear();
        setStatus("Streaming temperatura...");

        new Thread(() -> {
            try {
                List<WeatherModel> data = GrpcClient.getTemperatureStream(city);
                Platform.runLater(() -> {
                    weatherData.setAll(data);
                    setStatus("Pronađeno " + data.size() + " postaja za: " + city);
                });
            } catch (Exception e) {
                Platform.runLater(() -> setStatus("gRPC streaming greška: " + e.getMessage()));
            }
        }).start();
    }

    // =================== Odjava ===================

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/hr/algebra/iis/client/view/LoginView.fxml"));
            BorderPane root = loader.load();

            Stage stage = (Stage) userInfoLabel.getScene().getWindow();
            Scene scene = new Scene(root, 450, 350);
            scene.getStylesheets().add(
                getClass().getResource("/hr/algebra/iis/client/view/style.css")
                    .toExternalForm());

            stage.setTitle("IIS Projekt - Prijava");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (IOException e) {
            setStatus("Greška pri odjavi: " + e.getMessage());
        }
    }

    // =================== Pomoćne metode ===================

    private void setStatus(String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            bottomStatus.setText(message);
        });
    }

    private String str(Object o) { return o != null ? o.toString() : ""; }
    private long toLong(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.parseLong(o.toString()); } catch (Exception e) { return 0; }
    }
    private int toInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return 0; }
    }
}

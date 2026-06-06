package hr.algebra.iis.client.controller;

import hr.algebra.iis.client.model.ProductModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * KORAK 6: Dijalog za dodavanje i uređivanje proizvoda.
 */
public class ProductDialogController {

    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField skuField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField stockField;
    @FXML private ComboBox<String> stockStatusCombo;
    @FXML private TextArea descriptionField;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;

    private boolean saved = false;

    @FXML
    public void initialize() {
        typeCombo.getItems().addAll("simple", "variable", "grouped", "external");
        typeCombo.setValue("simple");

        statusCombo.getItems().addAll("publish", "draft", "pending");
        statusCombo.setValue("publish");

        stockStatusCombo.getItems().addAll("instock", "outofstock", "onbackorder");
        stockStatusCombo.setValue("instock");
    }

    @FXML
    private void handleSave() {
        if (!validate()) return;
        saved = true;
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        saved = false;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    public boolean isSaved() {
        return saved;
    }

    public void setProduct(ProductModel product) {
        nameField.setText(product.getName());
        priceField.setText(product.getPrice());
        skuField.setText(product.getSku());
        typeCombo.setValue(product.getType() != null ? product.getType() : "simple");
        statusCombo.setValue(product.getStatus() != null ? product.getStatus() : "publish");
        stockField.setText(String.valueOf(product.getStockQuantity()));
        stockStatusCombo.setValue(product.getStockStatus() != null ? product.getStockStatus() : "instock");
        descriptionField.setText(product.getDescription());
    }

    public Map<String, Object> getProductData() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", nameField.getText().trim());
        data.put("price", priceField.getText().trim());
        data.put("sku", skuField.getText().trim());
        data.put("type", typeCombo.getValue());
        data.put("status", statusCombo.getValue());
        data.put("stockStatus", stockStatusCombo.getValue());
        data.put("description", descriptionField.getText().trim());

        String stockText = stockField.getText().trim();
        if (!stockText.isEmpty()) {
            try {
                data.put("stockQuantity", Integer.parseInt(stockText));
            } catch (NumberFormatException e) {
                data.put("stockQuantity", 0);
            }
        }
        return data;
    }

    public boolean validate() {
        if (nameField.getText().trim().isEmpty()) {
            errorLabel.setText("Naziv proizvoda je obavezan.");
            return false;
        }
        if (priceField.getText().trim().isEmpty()) {
            errorLabel.setText("Cijena je obavezna.");
            return false;
        }
        if (!priceField.getText().trim().matches("^[0-9]+(\\.[0-9]{1,2})?$")) {
            errorLabel.setText("Cijena mora biti broj (npr. 29.99).");
            return false;
        }
        errorLabel.setText("");
        return true;
    }
}

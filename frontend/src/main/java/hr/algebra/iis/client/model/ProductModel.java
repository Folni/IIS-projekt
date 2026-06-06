package hr.algebra.iis.client.model;

import javafx.beans.property.*;

/**
 * JavaFX model za prikaz proizvoda u TableView.
 */
public class ProductModel {

    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty price = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final IntegerProperty stockQuantity = new SimpleIntegerProperty();
    private final StringProperty sku = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty stockStatus = new SimpleStringProperty();

    public ProductModel(long id, String name, String price, String type,
                        String status, int stockQuantity, String sku,
                        String description, String stockStatus) {
        this.id.set(id);
        this.name.set(name);
        this.price.set(price);
        this.type.set(type);
        this.status.set(status);
        this.stockQuantity.set(stockQuantity);
        this.sku.set(sku != null ? sku : "");
        this.description.set(description != null ? description : "");
        this.stockStatus.set(stockStatus != null ? stockStatus : "instock");
    }

    // Getteri za property-je (potrebni za TableView)
    public LongProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty priceProperty() { return price; }
    public StringProperty typeProperty() { return type; }
    public StringProperty statusProperty() { return status; }
    public IntegerProperty stockQuantityProperty() { return stockQuantity; }
    public StringProperty skuProperty() { return sku; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty stockStatusProperty() { return stockStatus; }

    // Getteri za vrijednosti
    public long getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getPrice() { return price.get(); }
    public String getType() { return type.get(); }
    public String getStatus() { return status.get(); }
    public int getStockQuantity() { return stockQuantity.get(); }
    public String getSku() { return sku.get(); }
    public String getDescription() { return description.get(); }
    public String getStockStatus() { return stockStatus.get(); }
}

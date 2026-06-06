package hr.algebra.iis.client.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * JavaFX model za prikaz vremenskih podataka u TableView.
 */
public class WeatherModel {

    private final StringProperty city = new SimpleStringProperty();
    private final StringProperty temperature = new SimpleStringProperty();
    private final StringProperty humidity = new SimpleStringProperty();
    private final StringProperty pressure = new SimpleStringProperty();
    private final StringProperty wind = new SimpleStringProperty();

    public WeatherModel(String city, double temperature, String humidity,
                        String pressure, String wind) {
        this.city.set(city);
        this.temperature.set(String.format("%.1f °C", temperature));
        this.humidity.set(humidity);
        this.pressure.set(pressure);
        this.wind.set(wind);
    }

    public StringProperty cityProperty() { return city; }
    public StringProperty temperatureProperty() { return temperature; }
    public StringProperty humidityProperty() { return humidity; }
    public StringProperty pressureProperty() { return pressure; }
    public StringProperty windProperty() { return wind; }
}

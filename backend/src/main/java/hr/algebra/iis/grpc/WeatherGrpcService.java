package hr.algebra.iis.grpc;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * KORAK 4: gRPC implementacija koja dohvaća podatke s DHMZ API-ja
 * i vraća temperaturu prema nazivu grada.
 *
 * DHMZ XML: https://vrijeme.hr/hrvatska_n.xml
 */
@Slf4j
@GrpcService
public class WeatherGrpcService extends WeatherServiceGrpc.WeatherServiceImplBase {

    private static final String DHMZ_URL = "https://vrijeme.hr/hrvatska_n.xml";

    /**
     * Unary RPC: dohvaća temperaturu za (dio) naziva grada.
     * Ako postoji više podudaranja, vraća samo prvo.
     */
    @Override
    public void getTemperature(CityRequest request,
                               StreamObserver<TemperatureResponse> responseObserver) {
        log.info("gRPC getTemperature poziv za grad: '{}'", request.getCityName());
        try {
            List<TemperatureResponse> results = fetchFromDhmz(request.getCityName());
            if (results.isEmpty()) {
                responseObserver.onNext(TemperatureResponse.newBuilder()
                        .setFound(false)
                        .setErrorMessage("Grad '" + request.getCityName() + "' nije pronađen u DHMZ podacima")
                        .build());
            } else {
                responseObserver.onNext(results.get(0));
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC greška", e);
            responseObserver.onNext(TemperatureResponse.newBuilder()
                    .setFound(false)
                    .setErrorMessage("Greška pri dohvaćanju podataka: " + e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }

    /**
     * Server-streaming RPC: vraća SVE gradove koji odgovaraju dijelu naziva.
     * Korisno kad upišeš npr. "Zagreb" i dobiješ sve postaje.
     */
    @Override
    public void getTemperatureStream(CityRequest request,
                                     StreamObserver<TemperatureResponse> responseObserver) {
        log.info("gRPC getTemperatureStream za: '{}'", request.getCityName());
        try {
            List<TemperatureResponse> results = fetchFromDhmz(request.getCityName());
            if (results.isEmpty()) {
                responseObserver.onNext(TemperatureResponse.newBuilder()
                        .setFound(false)
                        .setErrorMessage("Nema rezultata za: " + request.getCityName())
                        .build());
            } else {
                for (TemperatureResponse r : results) {
                    responseObserver.onNext(r);
                }
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC streaming greška", e);
            responseObserver.onError(e);
        }
    }

    /**
     * Dohvaća i parsira DHMZ XML, filtrira po dijelu naziva grada
     */
    private List<TemperatureResponse> fetchFromDhmz(String cityQuery) throws Exception {
        List<TemperatureResponse> results = new ArrayList<>();
        String query = cityQuery.toLowerCase().trim();

        // Dohvati XML s DHMZ servera
        URL url = new URL(DHMZ_URL);
        InputStream is = url.openStream();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = factory.newDocumentBuilder().parse(is);
        doc.getDocumentElement().normalize();

        // DHMZ XML struktura: <Hrvatska><Grad><GradIme>Zagreb</GradIme><Podatci>...
        NodeList gradovi = doc.getElementsByTagName("Grad");
        log.info("DHMZ: pronađeno {} gradova u XML-u", gradovi.getLength());

        for (int i = 0; i < gradovi.getLength(); i++) {
            Element grad = (Element) gradovi.item(i);
            String gradIme = getTagValue("GradIme", grad);

            if (gradIme != null && gradIme.toLowerCase().contains(query)) {
                // Dohvati meteorološke podatke
                String temp     = getTagValue("Temp", grad);
                String vlaga    = getTagValue("Vlaga", grad);
                String tlak     = getTagValue("Tlak", grad);
                String vjetar   = getTagValue("VjetarBrzina", grad);
                String vrijemeS = getTagValue("Datum", grad);

                TemperatureResponse response = TemperatureResponse.newBuilder()
                        .setCity(gradIme)
                        .setTemperature(temp != null ? parseDouble(temp) : 0.0)
                        .setHumidity(vlaga != null ? vlaga + "%" : "N/A")
                        .setPressure(tlak != null ? tlak + " hPa" : "N/A")
                        .setWind(vjetar != null ? vjetar + " km/h" : "N/A")
                        .setTimestamp(vrijemeS != null ? vrijemeS : "")
                        .setFound(true)
                        .build();

                results.add(response);
                log.debug("Pronađen grad: {} ({}°C)", gradIme, temp);
            }
        }
        return results;
    }

    private String getTagValue(String tagName, Element element) {
        NodeList list = element.getElementsByTagName(tagName);
        if (list.getLength() > 0) {
            Node node = list.item(0);
            if (node != null && node.getFirstChild() != null) {
                return node.getFirstChild().getNodeValue().trim();
            }
        }
        return null;
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.replace(",", "."));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}

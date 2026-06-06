package hr.algebra.iis.client.service;

import hr.algebra.iis.client.model.WeatherModel;
import hr.algebra.iis.grpc.CityRequest;
import hr.algebra.iis.grpc.TemperatureResponse;
import hr.algebra.iis.grpc.WeatherServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * KORAK 4 + 6: gRPC klijent za dohvat DHMZ temperature.
 * Koristi generirane protobuf klase s backend modula.
 */
public class GrpcClient {

    private static final String HOST = "localhost";
    private static final int PORT = 9090;

    /**
     * Unary RPC - dohvati temperaturu za jedan grad
     */
    public static List<WeatherModel> getTemperature(String cityName) {
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress(HOST, PORT)
            .usePlaintext()
            .build();

        try {
            WeatherServiceGrpc.WeatherServiceBlockingStub stub =
                WeatherServiceGrpc.newBlockingStub(channel);

            CityRequest request = CityRequest.newBuilder()
                .setCityName(cityName)
                .build();

            TemperatureResponse response = stub.getTemperature(request);

            List<WeatherModel> results = new ArrayList<>();
            if (response.getFound()) {
                results.add(new WeatherModel(
                    response.getCity(),
                    response.getTemperature(),
                    response.getHumidity(),
                    response.getPressure(),
                    response.getWind()
                ));
            }
            return results;
        } finally {
            channel.shutdown();
        }
    }

    /**
     * Server-streaming RPC - dohvati sve gradove koji odgovaraju pojmu
     */
    public static List<WeatherModel> getTemperatureStream(String cityName) {
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress(HOST, PORT)
            .usePlaintext()
            .build();

        try {
            WeatherServiceGrpc.WeatherServiceBlockingStub stub =
                WeatherServiceGrpc.newBlockingStub(channel);

            CityRequest request = CityRequest.newBuilder()
                .setCityName(cityName)
                .build();

            Iterator<TemperatureResponse> responses =
                stub.getTemperatureStream(request);

            List<WeatherModel> results = new ArrayList<>();
            while (responses.hasNext()) {
                TemperatureResponse r = responses.next();
                if (r.getFound()) {
                    results.add(new WeatherModel(
                        r.getCity(),
                        r.getTemperature(),
                        r.getHumidity(),
                        r.getPressure(),
                        r.getWind()
                    ));
                }
            }
            return results;
        } finally {
            channel.shutdown();
        }
    }
}

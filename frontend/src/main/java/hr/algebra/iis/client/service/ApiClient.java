package hr.algebra.iis.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * KORAK 6: HTTP klijent za pozivanje backend REST API-ja.
 * Sprema JWT token nakon prijave i šalje ga u svim zahtjevima.
 */
public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080";
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType JSON = MediaType.get("application/json");

    // Singleton JWT token
    private static String accessToken = null;
    private static String userRole = null;
    private static String username = null;

    // =================== Autentifikacija ===================

    public static boolean login(String user, String password) throws IOException {
        String body = mapper.writeValueAsString(Map.of("username", user, "password", password));
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/auth/login")
                .post(RequestBody.create(body, JSON))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) return false;
            Map<String, Object> data = mapper.readValue(response.body().string(), new TypeReference<>() {});
            accessToken = (String) data.get("accessToken");
            Map<String, Object> userInfo = (Map<String, Object>) data.get("user");
            userRole = (String) userInfo.get("role");
            username = user;
            return true;
        }
    }

    public static String getUserRole() { return userRole; }
    public static String getUsername() { return username; }
    public static boolean isFullAccess() { return "FULL_ACCESS".equals(userRole); }

    // =================== Proizvodi (GET) ===================

    public static List<Map<String, Object>> getAllProducts() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/products")
                .header("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            checkResponse(response);
            return mapper.readValue(response.body().string(), new TypeReference<>() {});
        }
    }

    public static Map<String, Object> getProductById(long id) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/products/" + id)
                .header("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            checkResponse(response);
            return mapper.readValue(response.body().string(), new TypeReference<>() {});
        }
    }

    // =================== Proizvodi (POST/PUT/DELETE) ===================

    public static Map<String, Object> createProduct(Map<String, Object> product) throws IOException {
        String body = mapper.writeValueAsString(product);
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/products")
                .header("Authorization", "Bearer " + accessToken)
                .post(RequestBody.create(body, JSON))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            checkResponse(response);
            return mapper.readValue(response.body().string(), new TypeReference<>() {});
        }
    }

    public static Map<String, Object> updateProduct(long id, Map<String, Object> product) throws IOException {
        String body = mapper.writeValueAsString(product);
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/products/" + id)
                .header("Authorization", "Bearer " + accessToken)
                .put(RequestBody.create(body, JSON))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            checkResponse(response);
            return mapper.readValue(response.body().string(), new TypeReference<>() {});
        }
    }

    public static void deleteProduct(long id) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/products/" + id)
                .header("Authorization", "Bearer " + accessToken)
                .delete()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            checkResponse(response);
        }
    }

    // =================== SOAP poziv ===================

    public static String callSoapSearch(String searchTerm) throws IOException {
        String soapBody = """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                              xmlns:iis="http://algebra.hr/iis/soap">
               <soapenv:Header/>
               <soapenv:Body>
                  <iis:SearchProductsRequest>
                     <searchTerm>%s</searchTerm>
                  </iis:SearchProductsRequest>
               </soapenv:Body>
            </soapenv:Envelope>
            """.formatted(searchTerm);

        Request request = new Request.Builder()
                .url(BASE_URL + "/ws")
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("Authorization", "Bearer " + accessToken)
                .post(RequestBody.create(soapBody, MediaType.get("text/xml")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            return response.body().string();
        }
    }

    // =================== XML generiranje ===================

    public static String generateXml() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/products/generate-xml")
                .header("Authorization", "Bearer " + accessToken)
                .post(RequestBody.create("{}", JSON))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            checkResponse(response);
            return response.body().string();
        }
    }

    // =================== Validacija ===================

    public static String validateXmlAndJson(String xml, String json) throws IOException {
        HttpUrl url = HttpUrl.parse(BASE_URL + "/api/products/validate").newBuilder()
                .addQueryParameter("xml", xml)
                .addQueryParameter("json", json)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + accessToken)
                .post(RequestBody.create("", MediaType.get("application/x-www-form-urlencoded")))
                .build();

        // Koristimo form body umjesto query params
        RequestBody formBody = new FormBody.Builder()
                .add("xml", xml)
                .add("json", json)
                .build();

        Request formRequest = new Request.Builder()
                .url(BASE_URL + "/api/products/validate")
                .header("Authorization", "Bearer " + accessToken)
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(formRequest).execute()) {
            return response.body().string();
        }
    }

    public static String validateGeneratedXml() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/products/validate-xml")
                .header("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            return response.body().string();
        }
    }

    private static void checkResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("API greška " + response.code() + ": " + response.message());
        }
    }
}

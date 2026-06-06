package hr.algebra.iis.soap;

import hr.algebra.iis.service.XmlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import java.io.StringWriter;
import java.util.List;

/**
 * KORAK 2: SOAP endpoint koji prima pojam za pretragu
 * i vraća filtrirane zapise iz XML datoteke koristeći XPath.
 *
 * WSDL je dostupan na: http://localhost:8080/ws/products.wsdl
 * Namespace: http://algebra.hr/iis/soap
 */
@Slf4j
@Endpoint
@RequiredArgsConstructor
public class ProductSoapEndpoint {

    private static final String NAMESPACE_URI = "http://algebra.hr/iis/soap";
    private static final String NAMESPACE_PRODUCT = "http://algebra.hr/iis/product";

    private final XmlService xmlService;

    /**
     * SOAP metoda: SearchProducts
     * Prima: <SearchProductsRequest><searchTerm>naziv</searchTerm></SearchProductsRequest>
     * Vraća: <SearchProductsResponse> s listom pronađenih proizvoda
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "SearchProductsRequest")
    @ResponsePayload
    public Source searchProducts(@RequestPayload Source request) {
        try {
            // Parsiraj ulazni SOAP zahtjev
            String searchTerm = extractSearchTerm(request);
            log.info("SOAP SearchProducts poziv - pojam: '{}'", searchTerm);

            // XPath pretraga (Korak 2)
            List<String> results = xmlService.searchProductsByXPath(searchTerm);
            log.info("SOAP: pronađeno {} rezultata za '{}'", results.size(), searchTerm);

            // Izgradnja SOAP odgovora
            return buildResponse(searchTerm, results);

        } catch (Exception e) {
            log.error("SOAP greška", e);
            throw new RuntimeException("SOAP greška: " + e.getMessage(), e);
        }
    }

    /**
     * SOAP metoda: GenerateXml
     * Pokreće generiranje XML datoteke iz trenutnih podataka
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GenerateXmlRequest")
    @ResponsePayload
    public Source generateXml(@RequestPayload Source request) {
        try {
            // TODO: dohvati sve proizvode i generiraj XML
            // Ovo se poziva prije pretrage da XML datoteka bude aktualna
            log.info("SOAP GenerateXml poziv");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder().newDocument();
            Element resp = doc.createElementNS(NAMESPACE_URI, "GenerateXmlResponse");
            Element status = doc.createElement("status");
            status.setTextContent("OK");
            resp.appendChild(status);
            doc.appendChild(resp);
            return new DOMSource(doc);
        } catch (Exception e) {
            throw new RuntimeException("Greška pri generiranju XML-a", e);
        }
    }

    // =================== Privatne metode ===================

    private String extractSearchTerm(Source source) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            StringWriter writer = new StringWriter();
            t.transform(source, new StreamResult(writer));
            String xml = writer.toString();

            // Jednostavan regex za ekstrakciju
            int start = xml.indexOf("<searchTerm>") + 12;
            int end = xml.indexOf("</searchTerm>");
            if (start > 11 && end > start) {
                return xml.substring(start, end).trim();
            }
            return "";
        } catch (Exception e) {
            log.error("Greška pri ekstrakciji search pojma", e);
            return "";
        }
    }

    private Source buildResponse(String searchTerm, List<String> xmlFragments) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element response = doc.createElementNS(NAMESPACE_URI, "SearchProductsResponse");
        doc.appendChild(response);

        // Pojam pretrage
        Element termEl = doc.createElement("searchTerm");
        termEl.setTextContent(searchTerm);
        response.appendChild(termEl);

        // Broj rezultata
        Element countEl = doc.createElement("totalResults");
        countEl.setTextContent(String.valueOf(xmlFragments.size()));
        response.appendChild(countEl);

        // Lista rezultata
        Element resultsEl = doc.createElement("results");
        for (String fragment : xmlFragments) {
            // Parsiraj fragment kao XML i ugradi u odgovor
            try {
                Document fragDoc = builder.parse(
                    new org.xml.sax.InputSource(new java.io.StringReader(fragment)));
                Node imported = doc.importNode(fragDoc.getDocumentElement(), true);
                resultsEl.appendChild(imported);
            } catch (Exception e) {
                // Ako parsiranje ne uspije, dodaj kao tekst
                Element item = doc.createElement("product");
                item.setTextContent(fragment);
                resultsEl.appendChild(item);
            }
        }
        response.appendChild(resultsEl);

        return new DOMSource(doc);
    }
}

package hr.algebra.iis.service;

import hr.algebra.iis.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * KORAK 2: Generira XML datoteku iz podataka i implementira XPath pretragu.
 * KORAK 3: Priprema XML za Jakarta XML validaciju.
 */
@Slf4j
@Service
public class XmlService {

    @Value("${app.xml.output-path:src/main/resources/xml/products.xml}")
    private String xmlOutputPath;

    /**
     * KORAK 2: Generira XML datoteku sa svim proizvodima.
     * Ova se XML datoteka koristi kao izvor podataka za SOAP XPath pretragu.
     */
    public String generateProductsXml(List<ProductDto> products) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // Korijen elementa s namespace-om koji odgovara XSD shemi
            Element root = doc.createElementNS("http://algebra.hr/iis/product", "tns:products");
            root.setAttribute("xmlns:tns", "http://algebra.hr/iis/product");
            doc.appendChild(root);

            for (ProductDto p : products) {
                Element productEl = doc.createElementNS("http://algebra.hr/iis/product", "tns:product");

                addElement(doc, productEl, "tns:name", p.getName());
                addElement(doc, productEl, "tns:slug", p.getSlug());
                addElement(doc, productEl, "tns:type", p.getType());
                addElement(doc, productEl, "tns:status", p.getStatus());
                addElement(doc, productEl, "tns:description", p.getDescription());
                addElement(doc, productEl, "tns:sku", p.getSku());
                addElement(doc, productEl, "tns:price", p.getPrice());
                addElement(doc, productEl, "tns:stockQuantity",
                        p.getStockQuantity() != null ? p.getStockQuantity().toString() : null);
                addElement(doc, productEl, "tns:stockStatus", p.getStockStatus());

                // Kategorije
                if (p.getCategories() != null && !p.getCategories().isEmpty()) {
                    Element categoriesEl = doc.createElementNS(
                            "http://algebra.hr/iis/product", "tns:categories");
                    for (ProductDto.CategoryDto cat : p.getCategories()) {
                        Element catEl = doc.createElementNS(
                                "http://algebra.hr/iis/product", "tns:category");
                        addElement(doc, catEl, "tns:id", cat.getId() != null ? cat.getId().toString() : "0");
                        addElement(doc, catEl, "tns:name", cat.getName());
                        categoriesEl.appendChild(catEl);
                    }
                    productEl.appendChild(categoriesEl);
                }

                root.appendChild(productEl);
            }

            // Pretvori DOM u String
            String xmlContent = documentToString(doc);

            // Spremi u datoteku
            saveToFile(xmlContent);

            log.info("XML datoteka generirana: {} ({}  proizvoda)", xmlOutputPath, products.size());
            return xmlContent;

        } catch (Exception e) {
            log.error("Greška pri generiranju XML-a", e);
            throw new RuntimeException("Ne mogu generirati XML datoteku: " + e.getMessage());
        }
    }

    /**
     * KORAK 2: XPath pretraga nad generiranom XML datotekom.
     * Vraća listu XML fragmenata koji odgovaraju traženom pojmu.
     */
    public List<String> searchProductsByXPath(String searchTerm) {
        List<String> results = new ArrayList<>();
        try {
            // Učitaj XML datoteku
            File xmlFile = new File(xmlOutputPath);
            if (!xmlFile.exists()) {
                log.warn("XML datoteka ne postoji: {}. Generirajte ju prvo.", xmlOutputPath);
                return results;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            // Postavljamo namespace context za XPath
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            xpath.setNamespaceContext(new javax.xml.namespace.NamespaceContext() {
                @Override
                public String getNamespaceURI(String prefix) {
                    return "tns".equals(prefix) ? "http://algebra.hr/iis/product" : null;
                }
                @Override
                public String getPrefix(String ns) { return null; }
                @Override
                public java.util.Iterator<String> getPrefixes(String ns) { return null; }
            });

            // XPath izraz - traži proizvode čije ime ili opis sadrži pojam
            String xpathExpr = String.format(
                "//tns:product[contains(translate(tns:name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s') " +
                "or contains(translate(tns:description,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s') " +
                "or contains(translate(tns:sku,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s')]",
                searchTerm.toLowerCase(), searchTerm.toLowerCase(), searchTerm.toLowerCase()
            );

            XPathExpression expr = xpath.compile(xpathExpr);
            NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            log.info("XPath pretraga za '{}' pronašla {} rezultata", searchTerm, nodeList.getLength());

            for (int i = 0; i < nodeList.getLength(); i++) {
                results.add(nodeToString(nodeList.item(i)));
            }

        } catch (Exception e) {
            log.error("Greška pri XPath pretrazi", e);
            throw new RuntimeException("XPath pretraga nije uspjela: " + e.getMessage());
        }
        return results;
    }

    /**
     * Čita XML datoteku kao String (za JAXB validaciju u Koraku 3)
     */
    public String readXmlFile() throws IOException {
        return Files.readString(Paths.get(xmlOutputPath));
    }

    // =================== Pomoćne metode ===================

    private void addElement(Document doc, Element parent, String tagName, String value) {
        if (value == null || value.isBlank()) return;
        Element el = doc.createElementNS("http://algebra.hr/iis/product", tagName);
        el.setTextContent(value);
        parent.appendChild(el);
    }

    private String documentToString(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    private String nodeToString(Node node) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString().trim();
    }

    private void saveToFile(String content) throws IOException {
        File file = new File(xmlOutputPath);
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), content);
    }
}

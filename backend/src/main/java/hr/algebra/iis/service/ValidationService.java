package hr.algebra.iis.service;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * KORAK 1: Validacija XML i JSON podataka prema shemama.
 * KORAK 3: Jakarta XML (JAXB) validacija.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private final ObjectMapper objectMapper;

    /**
     * KORAK 1: Validira XML string prema XSD shemi product.xsd
     * @return lista grešaka, prazna ako je XML validan
     */
    public List<String> validateXml(String xmlContent) {
        List<String> errors = new ArrayList<>();
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            var xsdResource = new ClassPathResource("xsd/product.xsd");
            var schema = schemaFactory.newSchema(xsdResource.getURL());

            var validator = schema.newValidator();
            List<String> validationErrors = new ArrayList<>();

            validator.setErrorHandler(new org.xml.sax.ErrorHandler() {
                @Override
                public void warning(org.xml.sax.SAXParseException e) {
                    validationErrors.add("Upozorenje [linija " + e.getLineNumber() + "]: " + e.getMessage());
                }
                @Override
                public void error(org.xml.sax.SAXParseException e) {
                    validationErrors.add("Greška [linija " + e.getLineNumber() + "]: " + e.getMessage());
                }
                @Override
                public void fatalError(org.xml.sax.SAXParseException e) {
                    validationErrors.add("Kritična greška [linija " + e.getLineNumber() + "]: " + e.getMessage());
                }
            });

            validator.validate(new StreamSource(new StringReader(xmlContent)));
            errors.addAll(validationErrors);

        } catch (Exception e) {
            errors.add("Greška pri validaciji XML-a: " + e.getMessage());
            log.error("XML validacija neuspješna", e);
        }
        return errors;
    }

    /**
     * KORAK 1: Validira JSON string prema JSON Schema (product-schema.json)
     * @return lista grešaka, prazna ako je JSON validan
     */
    public List<String> validateJson(String jsonContent) {
        List<String> errors = new ArrayList<>();
        try {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            var schemaResource = new ClassPathResource("xsd/product-schema.json");
            JsonSchema jsonSchema = factory.getSchema(schemaResource.getInputStream());

            JsonNode jsonNode = objectMapper.readTree(jsonContent);
            Set<ValidationMessage> validationMessages = jsonSchema.validate(jsonNode);

            for (ValidationMessage msg : validationMessages) {
                errors.add("Greška: " + msg.getMessage());
            }

        } catch (Exception e) {
            errors.add("Greška pri validaciji JSON-a: " + e.getMessage());
            log.error("JSON validacija neuspješna", e);
        }
        return errors;
    }

    /**
     * KORAK 3: Jakarta XML (JAXB) validacija generiranog XML-a.
     * Provjerava je li XML u skladu s pravilima JAXB modela.
     */
    public List<String> validateWithJakartaXml(String xmlContent, Class<?> jaxbClass) {
        List<String> errors = new ArrayList<>();
        try {
            JAXBContext context = JAXBContext.newInstance(jaxbClass);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // Postavljamo validacijski event handler
            unmarshaller.setEventHandler(event -> {
                String msg = "JAXB [" + event.getSeverity() + "] " + event.getMessage();
                errors.add(msg);
                log.warn("JAXB validacijska poruka: {}", msg);
                return true; // nastavi čak i uz greške da prikupimo sve
            });

            // Pokušaj unmarshallinga - ako XML ne odgovara modelu, dobit ćemo greške
            unmarshaller.unmarshal(new StringReader(xmlContent));

            if (errors.isEmpty()) {
                log.info("JAXB validacija uspješna - XML je ispravan");
            }

        } catch (JAXBException e) {
            errors.add("JAXB greška: " + e.getMessage());
            log.error("JAXB validacija neuspješna", e);
        }
        return errors;
    }
}

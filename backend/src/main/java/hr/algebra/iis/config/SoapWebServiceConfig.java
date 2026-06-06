package hr.algebra.iis.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

/**
 * KORAK 2: Konfiguracija Spring WS (SOAP) servisa.
 * WSDL se automatski generira na /ws/products.wsdl
 */
@EnableWs
@Configuration
public class SoapWebServiceConfig extends WsConfigurerAdapter {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
            ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean(name = "products")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema productSchema) {
        DefaultWsdl11Definition definition = new DefaultWsdl11Definition();
        definition.setPortTypeName("ProductsPort");
        definition.setLocationUri("/ws");
        definition.setTargetNamespace("http://algebra.hr/iis/soap");
        definition.setSchema(productSchema);
        return definition;
    }

    @Bean
    public XsdSchema productSchema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/product.xsd"));
    }
}

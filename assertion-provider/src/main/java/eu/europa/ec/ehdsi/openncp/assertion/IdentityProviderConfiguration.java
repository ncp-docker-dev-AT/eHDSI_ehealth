package eu.europa.ec.ehdsi.openncp.assertion;

import eu.epsos.ISecurityTokenService;
import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapBindingConstants;
import org.apache.cxf.binding.soap.interceptor.SoapHeaderInterceptor;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.xml.ws.Endpoint;

@Configuration
public class IdentityProviderConfiguration {

//    static {
//        //  System.setProperty("javax.net.debug", "all");
//        System.setProperty("javax.net.ssl.trustStore", "/home/dg-sante/LocalData/inception/xhealth-connect/xhealth-gateway/xhealth-initiating-gateway/src/main/resources/truststore.jks");
//        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
//        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
//        System.setProperty("javax.net.ssl.keyStore", "/home/dg-sante/LocalData/inception/xhealth-connect/xhealth-portal/src/main/resources/keystore.jks");
//        System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
//    }

    @Bean
    public ServletRegistrationBean<CXFServlet> dispatcherServlet() {

        return new ServletRegistrationBean<>(new CXFServlet(), "/services/*");
    }

    @Bean
    @Primary
    public DispatcherServletPath dispatcherServletPathProvider() {
        return () -> "";
    }

    @Bean(name = Bus.DEFAULT_BUS_ID)
    public SpringBus springBus() {

        SpringBus cxfBus = new SpringBus();
        cxfBus.getFeatures().add(loggingFeature());
        cxfBus.getFeatures().add(new WSAddressingFeature());
        return cxfBus;
    }

    @Bean
    public LoggingFeature loggingFeature() {

        LoggingFeature loggingFeature = new LoggingFeature();
        loggingFeature.setPrettyLogging(true);
        return loggingFeature;
    }

    @Bean
    public Endpoint endpoint(ISecurityTokenService serviceEndpoint) {

        EndpointImpl endpoint = new EndpointImpl(springBus(), serviceEndpoint);
        endpoint.setBindingUri(SoapBindingConstants.SOAP12_BINDING_ID);
        endpoint.getInInterceptors().add(new SoapHeaderInterceptor());
        endpoint.publish("/ClientConnectorService");
        return endpoint;
    }
}

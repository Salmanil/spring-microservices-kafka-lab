package com.example.employee_api.config;

import java.io.IOException;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class HttpConnectorConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainer(
            @Value("${app.security.https-port:8443}") int httpsPort,
            @Value("${server.ssl.key-store-password}") String keyStorePassword,
            @Value("${server.ssl.key-store-type}") String keyStoreType,
            @Value("${server.ssl.key-alias}") String keyAlias) {
        return factory -> factory.addAdditionalTomcatConnectors(
                createHttpsConnector(httpsPort, keyStorePassword, keyStoreType, keyAlias));
    }

    private Connector createHttpsConnector(int port, String keyStorePassword, String keyStoreType, String keyAlias) {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("https");
        connector.setPort(port);
        connector.setSecure(true);

        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
        protocol.setSSLEnabled(true);
        SSLHostConfig sslHostConfig = new SSLHostConfig();
        sslHostConfig.setHostName("_default_");

        SSLHostConfigCertificate certificate = new SSLHostConfigCertificate(
                sslHostConfig,
                SSLHostConfigCertificate.Type.RSA);
        certificate.setCertificateKeystorePassword(keyStorePassword);
        certificate.setCertificateKeystoreType(keyStoreType);
        certificate.setCertificateKeyAlias(keyAlias);
        certificate.setCertificateKeystoreFile(resolveKeyStorePath());
        sslHostConfig.addCertificate(certificate);
        connector.addSslHostConfig(sslHostConfig);
        return connector;
    }

    private String resolveKeyStorePath() {
        try {
            return new ClassPathResource("employee-api-keystore.p12").getFile().getAbsolutePath();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to resolve HTTPS keystore from classpath", exception);
        }
    }
}

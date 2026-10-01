package io.jeannyil.configurations;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.Optional;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

import io.quarkus.runtime.annotations.RegisterForReflection;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.SdkHttpConfigurationOption;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.utils.AttributeMap;

/**
 * Shared S3 client for the Camel aws2-s3 routes.
 * Disables aws-chunked framing and optional checksums so S3-compatible stores
 * (local-s3, NooBaa) persist the raw object body instead of chunk signatures.
 *
 * Class-level {@link ApplicationScoped} scopes this producer bean.
 * Method-level {@link ApplicationScoped} on {@link #s3Client()} scopes the
 * produced {@link S3Client} so Camel's {@code #s3Client} lookup reuses one client.
 */
@ApplicationScoped
@RegisterForReflection
public class S3ClientProducer {

    @ConfigProperty(name = "s3.endpoint")
    String endpoint;

    @ConfigProperty(name = "s3.region")
    String region;

    @ConfigProperty(name = "s3.access-key")
    String accessKey;

    @ConfigProperty(name = "s3.secret-key")
    String secretKey;

    @ConfigProperty(name = "s3.trust-all-certificates", defaultValue = "false")
    boolean trustAllCertificates;

    @ConfigProperty(name = "s3.trust-certificate-pem")
    Optional<String> trustCertificatePem;

    @ConfigProperty(name = "s3.path-style-access", defaultValue = "true")
    boolean pathStyleAccess;

    @ConfigProperty(name = "s3.chunked-encoding-enabled", defaultValue = "false")
    boolean chunkedEncodingEnabled;

    @ConfigProperty(name = "s3.request-checksum-calculation", defaultValue = "WHEN_REQUIRED")
    String requestChecksumCalculation;

    @ConfigProperty(name = "s3.response-checksum-validation", defaultValue = "WHEN_REQUIRED")
    String responseChecksumValidation;

    private S3Client s3Client;

    /**
     * Shared client for both aws2-s3 routes via {@code amazonS3Client=#s3Client}.
     * Chunked encoding and checksums come from config so local-s3 and NooBaa store
     * the raw object body. {@code forcePathStyle} is set only on this builder
     * (not also on {@link S3Configuration}) to avoid a dual-configuration startup failure.
     */
    @Produces
    @Named("s3Client")
    @ApplicationScoped
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder()
                        .chunkedEncodingEnabled(chunkedEncodingEnabled)
                        .build())
                .requestChecksumCalculation(
                        RequestChecksumCalculation.valueOf(requestChecksumCalculation))
                .responseChecksumValidation(
                        ResponseChecksumValidation.valueOf(responseChecksumValidation))
                .httpClient(buildHttpClient());

        if (pathStyleAccess) {
            builder.forcePathStyle(true);
        }

        s3Client = builder.build();
        return s3Client;
    }

    /**
     * First match wins: trust-all skips verification; otherwise
     * {@code s3.trust-certificate-pem} (OpenShift service CA at
     * {@code /var/run/secrets/kubernetes.io/serviceaccount/service-ca.crt}) is loaded
     * for this client only; otherwise the JVM truststore is used. Local
     * {@code http://localhost:9000} never exercises TLS.
     */
    private SdkHttpClient buildHttpClient() {
        if (trustAllCertificates) {
            AttributeMap trustAll = AttributeMap.builder()
                    .put(SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES, Boolean.TRUE)
                    .build();
            return ApacheHttpClient.builder().buildWithDefaults(trustAll);
        }

        if (trustCertificatePem.isPresent() && !trustCertificatePem.get().isBlank()) {
            TrustManager[] trustManagers = trustManagersFromPem(Path.of(trustCertificatePem.get()));
            return ApacheHttpClient.builder()
                    .tlsTrustManagersProvider(() -> trustManagers)
                    .build();
        }

        return ApacheHttpClient.builder().build();
    }

    /**
     * The AWS SDK cannot take a PEM path. Read the file into an in-memory keystore
     * and build trust managers that replace the default truststore for this client only.
     */
    private static TrustManager[] trustManagersFromPem(Path pemPath) {
        try (InputStream in = Files.newInputStream(pemPath)) {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> certificates = factory.generateCertificates(in);
            if (certificates.isEmpty()) {
                throw new IllegalStateException("No certificates found in " + pemPath);
            }

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            int index = 0;
            for (Certificate certificate : certificates) {
                keyStore.setCertificateEntry("s3-ca-" + index++, certificate);
            }

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            return trustManagerFactory.getTrustManagers();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load S3 trust certificate from " + pemPath, e);
        }
    }

    /** Closes the client held in the field when this application-scoped producer shuts down. */
    @PreDestroy
    void close() {
        if (s3Client != null) {
            s3Client.close();
        }
    }
}

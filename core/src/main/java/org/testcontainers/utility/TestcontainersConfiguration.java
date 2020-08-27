package org.testcontainers.utility;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.UnstableAPI;

/**
 * Provides a mechanism for fetching configuration/defaults from the classpath.
 */
@Data
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TestcontainersConfiguration {

    private static String PROPERTIES_FILE_NAME = "testcontainers.properties";

    private static File ENVIRONMENT_CONFIG_FILE = new File(System.getProperty("user.home"), "." + PROPERTIES_FILE_NAME);

    private static final String AMBASSADOR_IMAGE = "richnorth/ambassador:latest";
    private static final String SOCAT_IMAGE = "alpine/socat:latest";
    private static final String VNC_RECORDER_IMAGE = "testcontainers/vnc-recorder:1.1.0";
    private static final String COMPOSE_IMAGE = "docker/compose:1.24.1";
    private static final String ALPINE_IMAGE = "alpine:3.5";
    private static final String RYUK_IMAGE = "testcontainers/ryuk:0.3.0";
    private static final String KAFKA_IMAGE = "confluentinc/cp-kafka";
    private static final String PULSAR_IMAGE = "apachepulsar/pulsar";
    private static final String LOCALSTACK_IMAGE = "localstack/localstack";
    private static final String SSHD_IMAGE = "testcontainers/sshd:1.0.0";

    private static final ImmutableMap<DockerImageName, String> CONTAINER_MAPPING = ImmutableMap.<DockerImageName, String>builder()
        .put(DockerImageName.parse(AMBASSADOR_IMAGE), "ambassador.container.image")
        .put(DockerImageName.parse(SOCAT_IMAGE), "socat.container.image")
        .put(DockerImageName.parse(VNC_RECORDER_IMAGE), "vncrecorder.container.image")
        .put(DockerImageName.parse(COMPOSE_IMAGE), "compose.container.image")
        .put(DockerImageName.parse(ALPINE_IMAGE), "tinyimage.container.image")
        .put(DockerImageName.parse(RYUK_IMAGE), "ryuk.container.image")
        .put(DockerImageName.parse(KAFKA_IMAGE), "kafka.container.image")
        .put(DockerImageName.parse(PULSAR_IMAGE), "pulsar.container.image")
        .put(DockerImageName.parse(LOCALSTACK_IMAGE), "localstack.container.image")
        .put(DockerImageName.parse(SSHD_IMAGE), "sshd.container.image")
        .build();

    @Getter(lazy = true)
    private static final TestcontainersConfiguration instance = loadConfiguration();

    @SuppressWarnings({"ConstantConditions", "unchecked", "rawtypes"})
    @VisibleForTesting
    static AtomicReference<TestcontainersConfiguration> getInstanceField() {
        // Lazy Getter from Lombok changes the field's type to AtomicReference
        return (AtomicReference) (Object) instance;
    }

    @Getter(AccessLevel.NONE)
    private final Properties environmentProperties;

    private final Properties properties = new Properties();

    TestcontainersConfiguration(Properties environmentProperties, Properties classpathProperties) {
        this.environmentProperties = environmentProperties;

        this.properties.putAll(classpathProperties);
        this.properties.putAll(environmentProperties);
    }

    @Deprecated
    public String getAmbassadorContainerImage() {
        return getImage(AMBASSADOR_IMAGE).asCanonicalNameString();
    }

    @Deprecated
    public String getSocatContainerImage() {
        return getImage(SOCAT_IMAGE).asCanonicalNameString();
    }

    @Deprecated
    public String getVncRecordedContainerImage() {
        return getImage(VNC_RECORDER_IMAGE).asCanonicalNameString();
    }

    @Deprecated
    public String getDockerComposeContainerImage() {
        return getImage(COMPOSE_IMAGE).asCanonicalNameString();
    }

    @Deprecated
    public String getTinyImage() {
        return getImage(ALPINE_IMAGE).asCanonicalNameString();
    }

    public boolean isRyukPrivileged() {
        return Boolean
            .parseBoolean((String) properties.getOrDefault("ryuk.container.privileged", "false"));
    }

    @Deprecated
    public String getRyukImage() {
        return getImage(RYUK_IMAGE).asCanonicalNameString();
    }

    @Deprecated
    public String getSSHdImage() {
        return getImage(SSHD_IMAGE).asCanonicalNameString();
    }

    public Integer getRyukTimeout() {
        return Integer.parseInt((String) properties.getOrDefault("ryuk.container.timeout", "30"));
    }

    @Deprecated
    public String getKafkaImage() {
        return getImage(KAFKA_IMAGE).asCanonicalNameString();
    }

    @Deprecated
    public String getPulsarImage() {
        return getImage(PULSAR_IMAGE).asCanonicalNameString();
    }

    @Deprecated
    public String getLocalStackImage() {
        return getImage(LOCALSTACK_IMAGE).asCanonicalNameString();
    }

    public boolean isDisableChecks() {
        return Boolean.parseBoolean((String) environmentProperties.getOrDefault("checks.disable", "false"));
    }

    @UnstableAPI
    public boolean environmentSupportsReuse() {
        return Boolean.parseBoolean((String) environmentProperties.getOrDefault("testcontainers.reuse.enable", "false"));
    }

    public String getDockerClientStrategyClassName() {
        return (String) environmentProperties.get("docker.client.strategy");
    }

    public String getTransportType() {
        return properties.getProperty("transport.type", "okhttp");
    }

    public Integer getImagePullPauseTimeout() {
        return Integer.parseInt((String) properties.getOrDefault("pull.pause.timeout", "30"));
    }

    @Synchronized
    public boolean updateGlobalConfig(@NonNull String prop, @NonNull String value) {
        try {
            if (value.equals(environmentProperties.get(prop))) {
                return false;
            }

            environmentProperties.setProperty(prop, value);

            ENVIRONMENT_CONFIG_FILE.createNewFile();
            try (OutputStream outputStream = new FileOutputStream(ENVIRONMENT_CONFIG_FILE)) {
                environmentProperties.store(outputStream, "Modified by Testcontainers");
            }

            // Update internal state only if environment config was successfully updated
            properties.setProperty(prop, value);
            return true;
        } catch (Exception e) {
            log.debug("Can't store environment property {} in {}", prop, ENVIRONMENT_CONFIG_FILE);
            return false;
        }
    }

    @SneakyThrows(MalformedURLException.class)
    private static TestcontainersConfiguration loadConfiguration() {
        return new TestcontainersConfiguration(
            readProperties(ENVIRONMENT_CONFIG_FILE.toURI().toURL()),
            Stream
                .of(
                    TestcontainersConfiguration.class.getClassLoader(),
                    Thread.currentThread().getContextClassLoader()
                )
                .map(it -> it.getResource(PROPERTIES_FILE_NAME))
                .filter(Objects::nonNull)
                .map(TestcontainersConfiguration::readProperties)
                .reduce(new Properties(), (a, b) -> {
                    a.putAll(b);
                    return a;
                })
        );
    }

    private static Properties readProperties(URL url) {
        log.debug("Testcontainers configuration overrides will be loaded from {}", url);
        Properties properties = new Properties();
        try (InputStream inputStream = url.openStream()) {
            properties.load(inputStream);
        } catch (FileNotFoundException e) {
            log.trace("Testcontainers config override was found on {} but the file was not found", url, e);
        } catch (IOException e) {
            log.warn("Testcontainers config override was found on {} but could not be loaded", url, e);
        }
        return properties;
    }

    private DockerImageName getImage(final String defaultValue) {
        return getConfiguredSubstituteImage(DockerImageName.parse(defaultValue));
    }

    DockerImageName getConfiguredSubstituteImage(DockerImageName original) {
        for (final Map.Entry<DockerImageName, String> entry : CONTAINER_MAPPING.entrySet()) {
            if (original.isCompatibleWith(entry.getKey())) {
                return
                    Optional.ofNullable(entry.getValue())
                        .map(properties::get)
                        .map(String::valueOf)
                        .map(DockerImageName::parse)
                        .orElse(original)
                        .asCompatibleSubstituteFor(original);
            }
        }
        return original;
    }
}

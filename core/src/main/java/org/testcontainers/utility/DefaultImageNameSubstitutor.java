package org.testcontainers.utility;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

import static org.testcontainers.utility.TestcontainersConfiguration.*;

/**
 * TODO: Javadocs
 */
@Slf4j
public class DefaultImageNameSubstitutor extends ImageNameSubstitutor {

    private final ImmutableMap<DockerImageName, Supplier<DockerImageName>> mapping;

    public DefaultImageNameSubstitutor() {
        this(TestcontainersConfiguration.getInstance());
    }

    @VisibleForTesting
    DefaultImageNameSubstitutor(TestcontainersConfiguration configuration) {
        this.mapping = ImmutableMap.<DockerImageName, Supplier<DockerImageName>>builder()
            .put(DEFAULT_AMBASSADOR, () -> DockerImageName.parse(configuration.getAmbassadorContainerImage()))
            .put(DEFAULT_SOCAT, () -> DockerImageName.parse(configuration.getSocatContainerImage()))
            .put(DEFAULT_VNC_RECORDER, () -> DockerImageName.parse(configuration.getVncRecordedContainerImage()))
            .put(DEFAULT_DOCKER_COMPOSE, () -> DockerImageName.parse(configuration.getDockerComposeContainerImage()))
            .put(DEFAULT_TINY_IMAGE, () -> DockerImageName.parse(configuration.getTinyImage()))
            .put(DEFAULT_RYUK, () -> DockerImageName.parse(configuration.getRyukImage()))
            .put(DEFAULT_KAFKA, () -> DockerImageName.parse(configuration.getKafkaImage()))
            .put(DEFAULT_PULSAR, () -> DockerImageName.parse(configuration.getPulsarImage()))
            .put(DEFAULT_LOCALSTACK, () -> DockerImageName.parse(configuration.getLocalStackImage()))
            .put(DEFAULT_SSHD, () -> DockerImageName.parse(configuration.getSSHdImage()))
            .build();
    }

    @Override
    public DockerImageName substitute(final DockerImageName original) {
        return mapping
            .getOrDefault(original, () -> original)
            .get()
            .asCompatibleSubstituteFor(original);
    }

    @Override
    protected int getPriority() {
        return 0;
    }
}

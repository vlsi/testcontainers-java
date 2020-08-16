package org.testcontainers.utility;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO: Javadocs
 */
@Slf4j
public class DefaultImageNameSubstitutor extends ImageNameSubstitutor {

    private final TestcontainersConfiguration configuration;

    public DefaultImageNameSubstitutor() {
        this(TestcontainersConfiguration.getInstance());
    }

    @VisibleForTesting
    DefaultImageNameSubstitutor(TestcontainersConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public DockerImageName performSubstitution(final DockerImageName original) {
        return configuration
            .getConfiguredSubstituteImage(original)
            .asCompatibleSubstituteFor(original);
    }

    @Override
    protected int getPriority() {
        return 0;
    }
}

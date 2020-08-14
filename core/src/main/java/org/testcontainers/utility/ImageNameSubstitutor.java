package org.testcontainers.utility;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import static java.util.Comparator.comparingInt;

/**
 * TODO: Javadocs
 */
@Slf4j
public abstract class ImageNameSubstitutor {

    @VisibleForTesting
    static ImageNameSubstitutor instance;

    public synchronized static ImageNameSubstitutor instance() {
        if (instance == null) {
            final ServiceLoader<ImageNameSubstitutor> serviceLoader = ServiceLoader.load(ImageNameSubstitutor.class);

            instance = StreamSupport.stream(serviceLoader.spliterator(), false)
                .peek(it -> log.debug("Found ImageNameSubstitutor using ServiceLoader: {} (priority {}) ", it, it.getPriority()))
                .max(comparingInt(ImageNameSubstitutor::getPriority))
                .orElseThrow(() -> new RuntimeException("Unable to find any ImageNameSubstitutor using ServiceLoader"));

            log.info("Using ImageNameSubstitutor: {}", instance);
        }

        return instance;
    }

    public abstract DockerImageName substitute(DockerImageName original);

    /**
     * Priority of this {@link ImageNameSubstitutor} compared to other instances that may be found by the service
     * loader. The highest priority instance found will always be used.
     * @return a priority
     */
    protected abstract int getPriority();
}

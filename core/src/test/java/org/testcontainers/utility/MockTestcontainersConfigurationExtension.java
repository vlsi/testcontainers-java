package org.testcontainers.utility;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicReference;

/**
 * This extension applies a spy on {@link TestcontainersConfiguration}
 * for testing features that depend on the global configuration.
 */
public final class MockTestcontainersConfigurationExtension implements Extension, AfterEachCallback, BeforeEachCallback {

    private static final Namespace NAMESPACE = Namespace.create(MockTestcontainersConfigurationExtension.class);

    private static final String PREVIOUS_REF = "previousRef";

    private static AtomicReference<TestcontainersConfiguration> REF = TestcontainersConfiguration.getInstanceField();

    @Override
    public void beforeEach(ExtensionContext context) {
        TestcontainersConfiguration previous = REF.get();
        if (previous == null) {
            previous = TestcontainersConfiguration.getInstance();
        }
        REF.set(Mockito.spy(previous));
        context.getStore(NAMESPACE).put(PREVIOUS_REF, previous);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        TestcontainersConfiguration previous = context.getStore(NAMESPACE).get(PREVIOUS_REF, TestcontainersConfiguration.class);
        REF.set(previous);
        Mockito.validateMockitoUsage();
    }
}

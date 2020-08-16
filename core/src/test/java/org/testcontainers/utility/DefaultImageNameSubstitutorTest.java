package org.testcontainers.utility;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.rnorth.visibleassertions.VisibleAssertions.assertTrue;

public class DefaultImageNameSubstitutorTest {

    public static final DockerImageName ORIGINAL_IMAGE = DockerImageName.parse("foo");
    public static final DockerImageName SUBSTITUTE_IMAGE = DockerImageName.parse("bar");
    private DefaultImageNameSubstitutor underTest;
    private TestcontainersConfiguration mockConfiguration;

    @Before
    public void setUp() {
        mockConfiguration = mock(TestcontainersConfiguration.class);
        underTest = new DefaultImageNameSubstitutor(mockConfiguration);
    }

    @Test
    public void testConfigurationLookup() {
        when(mockConfiguration.getConfiguredSubstituteImage(eq(ORIGINAL_IMAGE))).thenReturn(SUBSTITUTE_IMAGE);

        final DockerImageName substitute = underTest.performSubstitution(ORIGINAL_IMAGE);

        assertEquals("match is found", SUBSTITUTE_IMAGE, substitute);
        assertTrue("compatibility is automatically set", substitute.isCompatibleWith(ORIGINAL_IMAGE));
    }
}

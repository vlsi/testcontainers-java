package org.testcontainers.utility;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.rnorth.visibleassertions.VisibleAssertions.assertTrue;

public class DefaultImageNameSubstitutorTest {

    private DefaultImageNameSubstitutor underTest;
    private TestcontainersConfiguration mockConfiguration;

    @Before
    public void setUp() {
        mockConfiguration = mock(TestcontainersConfiguration.class);
        underTest = new DefaultImageNameSubstitutor(mockConfiguration);
    }

    @Test
    public void testLocalstackLookup() {
        when(mockConfiguration.getLocalStackImage()).thenReturn("localstack-substitute");

        final DockerImageName substitute = underTest.substitute(DockerImageName.parse("localstack/localstack"));

        assertEquals("match is found", DockerImageName.parse("localstack-substitute"), substitute);
        assertTrue("compatibility is automatically set", substitute.isCompatibleWith(TestcontainersConfiguration.DEFAULT_LOCALSTACK));
    }
}

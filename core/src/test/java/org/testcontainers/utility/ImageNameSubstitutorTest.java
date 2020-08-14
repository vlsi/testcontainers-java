package org.testcontainers.utility;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ImageNameSubstitutorTest {

    @Test
    public void simpleServiceLoadingTest() {
        final ImageNameSubstitutor imageNameSubstitutor = ImageNameSubstitutor.instance();

        assertTrue(imageNameSubstitutor instanceof DefaultImageNameSubstitutor);
    }
}

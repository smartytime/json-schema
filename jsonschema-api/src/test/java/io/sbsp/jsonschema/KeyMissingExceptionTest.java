package io.sbsp.jsonschema;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyMissingExceptionTest {

    @Test(expected = NullPointerException.class)
    public void testNullMessage() {
        new KeyMissingException(SchemaLocation.hashedRoot(this), null);

    }

    @Test(expected = NullPointerException.class)
    public void testNullLocationInput() {
        new KeyMissingException(null, "crafty");
    }



    @Test
    public void testMessage() {
        final KeyMissingException exception = new KeyMissingException(SchemaLocation.hashedRoot(this), "bob");
        assertThat(exception.getMessage()).isEqualTo("#: Missing value at key [bob]");
    }

}
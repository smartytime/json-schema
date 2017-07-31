package io.dugnutt.jsonschema.six;

import io.dugnutt.jsonschema.six.enums.FormatType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FormatTypeTest {

    @Test
    public void testFromFormat_JsonPointer() {
        final FormatType formatType = FormatType.fromFormat("json-pointer");
        assertThat(formatType).isNotNull().isEqualTo(FormatType.JSON_POINTER);
    }

    @Test
    public void testFromFormat_Null() {
        assertThat(FormatType.fromFormat(null)).isNull();
    }

    @Test
    public void testFromFormat_Invalid() {
        assertThat(FormatType.fromFormat("non-existent-type")).isNull();
    }
}
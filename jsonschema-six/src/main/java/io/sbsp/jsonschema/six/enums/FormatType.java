package io.sbsp.jsonschema.six.enums;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

/**
 * Represents each of the built-in format types in the json-schema specification.
 */
public enum FormatType {
    DATE_TIME, EMAIL, HOSTNAME, IPV4, IPV6, URI, URI_TEMPLATE, JSON_POINTER, URI_REFERENCE;

    public String toString() {
        return UPPER_UNDERSCORE.to(LOWER_HYPHEN, name());
    }

    public static FormatType fromFormat(String format) {
        if (format == null) {
            return null;
        }
        final String potentialMatch = LOWER_HYPHEN.to(UPPER_UNDERSCORE, format);
        try {
            return FormatType.valueOf(potentialMatch);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

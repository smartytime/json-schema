package org.everit.jsonschema.api;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

public enum FormatType {
    DATE_TIME, EMAIL, HOSTNAME, IPV4, IPV6, URI;

    public static FormatType fromFormat(String format) {
        if (format == null) {
            return null;
        }
        final String potentialMatch = LOWER_HYPHEN.to(UPPER_CAMEL, format);
        try {
            return FormatType.valueOf(potentialMatch);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String toString() {
        return UPPER_UNDERSCORE.to(LOWER_HYPHEN, name());
    }
}

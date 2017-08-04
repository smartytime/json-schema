package io.sbsp.jsonschema.enums;

import com.google.common.base.MoreObjects;

import java.util.EnumSet;
import java.util.Set;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.google.common.base.MoreObjects.firstNonNull;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft3;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft4;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft5;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft6;

/**
 * Represents each of the built-in format types in the json-schema specification.
 */
public enum FormatType {

    /**
     * from draft-06
     * <p>
     * email  A string instance is valid against this attribute if it is a valid
     * Internet email address as defined by RFC 5322, section 3.4.1
     * [RFC5322].
     */
    EMAIL(Draft3),

    /**
     * from draft-06
     * A string instance is valid against this attribute if it is a valid
     * date representation as defined by RFC 3339, section 5.6 [RFC3339].
     */
    DATE_TIME(Draft3),

    /**
     * from draft-06
     * <p>
     * A string instance is valid against this attribute if it is a valid
     * representation for an Internet host name, as defined by RFC 1034,
     * section 3.1 [RFC1034].
     */
    HOSTNAME(Draft4),

    /**
     * from draft-06
     * <p>
     * A string instance is valid against this attribute if it is a valid
     * representation of an IPv4 address according to the "dotted-quad" ABNF
     * syntax as defined in RFC 2673, section 3.2 [RFC2673].
     */
    IPV4(Draft4),

    /**
     * from draft-06
     * <p>
     * A string instance is valid against this attribute if it is a valid
     * representation of an IPv6 address as defined in RFC 2373, section 2.2
     * [RFC2373].
     */
    IPV6(Draft3),

    /**
     * from draft-06
     * <p>
     * A string instance is valid against this attribute if it is a valid
     * URI, according to [RFC3986].
     */
    URI(Draft3),

    /**
     * from draft-06
     * <p>
     * A string instance is valid against this attribute if it is a valid
     * URI Reference (either a URI or a relative-reference), according to
     * [RFC3986].
     */
    URI_REFERENCE(Draft6),

    /**
     * from draft-06
     * <p>
     * A string instance is valid against this attribute if it is a valid
     * URI Template (of any level), according to [RFC6570].
     */
    URI_TEMPLATE(Draft6),

    /**
     * from draft-06
     * <p>
     * A string instance is valid against this attribute if it is a valid
     * JSON Pointer, according to [RFC6901]
     */
    JSON_POINTER(Draft6),

    /**
     * from draft-03
     * <p>
     * date  This SHOULD be a date in the format of YYYY-MM-DD.  It is
     * recommended that you use the "date-time" format instead of "date"
     * unless you need to transfer only the date part.
     */
    DATE(Draft3, Draft3),

    /**
     * from draft-03
     * <p>
     * time  This SHOULD be a time in the format of hh:mm:ss.  It is
     * recommended that you use the "date-time" format instead of "time"
     * unless you need to transfer only the time part.
     */
    TIME(Draft3, Draft3),

    /**
     * from draft-03
     * <p>
     * This SHOULD be the difference, measured in
     * milliseconds, between the specified time and midnight, 00:00 of
     * January 1, 1970 UTC.  The value SHOULD be a number (integer or
     * float).
     */
    UTC_MILLISEC(Draft3, Draft3),

    /**
     * from draft-03
     * <p>
     * regex  A regular expression, following the regular expression
     * specification from ECMA 262/Perl 5.
     */
    REGEX(Draft3, Draft3),

    /**
     * from draft-03
     * <p>
     * phone  This SHOULD be a phone number (format MAY follow E.123).
     */
    PHONE(Draft3, Draft3),

    /**
     * from draft-03
     * <p>
     * color  This is a CSS color (like "#FF0000" or "red"), based on CSS
     * 2.1 [W3C.CR-CSS21-20070719].
     */
    COLOR(Draft3, Draft3),

    /**
     * from draft-03
     * <p>
     * style  This is a CSS style definition (like "color: red; background-
     * color:#FFF"), based on CSS 2.1 [W3C.CR-CSS21-20070719].
     */
    STYLE(Draft3, Draft3),

    /**
     * Renamed to uri-reference in draft-06
     *
     * See {@link #URI_REFERENCE}
     */
    URIREF(Draft5, Draft5),

    /**
     * Originally named host-name, this was renamed to hostname in draft-04
     * See {@link #HOSTNAME}
     */
    HOST_NAME(Draft3, Draft3),

    /**
     * Renamed to ipv4 in draft-04
     * <p>
     * See {@link #IPV4}
     */
    IP_ADDRESS(Draft3, Draft3),;

    private final String key;
    private final Set<JsonSchemaVersion> applicableVersions;

    FormatType(JsonSchemaVersion since, JsonSchemaVersion until) {
        this(null, since, until);
    }

    FormatType(String key, JsonSchemaVersion since, JsonSchemaVersion until) {
        this.key = MoreObjects.firstNonNull(key, UPPER_UNDERSCORE.to(LOWER_HYPHEN, name()));
        since = firstNonNull(since, Draft3);
        until = firstNonNull(until, Draft6);
        this.applicableVersions = EnumSet.range(since, until);
    }

    FormatType(JsonSchemaVersion since) {
        this(null, since, null);
    }

    public String toString() {
        return key;
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

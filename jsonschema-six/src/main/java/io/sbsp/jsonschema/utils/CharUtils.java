package io.sbsp.jsonschema.utils;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import lombok.SneakyThrows;

import java.net.URLDecoder;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;

public class CharUtils {
    private static final Escaper JSON_POINTER_SEGMENT_ESCAPER = Escapers.builder()
            .addEscape('~', "~0")
            .addEscape('/', "~1")
            .addEscape('"', "\\")
            .addEscape('\\', "\\\\")
            .build();

    private static final Unescaper JSON_POINTER_SEGMENT_UNESCAPER = Unescapers.builder()
            .addUnescape("~0", "~")
            .addUnescape("~1", "/")
            .addUnescape("\\", "\"")
            .addUnescape("\\\\", "\\")
            .build();

    private static final Splitter FORWARD_SLASH_SEPARATOR = Splitter.on('/');

    private static final Unescaper URL_SEGMENT_UNESCAPER = new Unescaper() {
        @Override
        @SneakyThrows
        public String unescape(CharSequence string) {
            return URLDecoder.decode(string.toString(), "UTF-8");
        }
    };

    public static boolean areNullOrBlank(String... toCheck) {
        checkNotNull(toCheck, "toCheck must not be null");
        checkArgument(toCheck.length > 0, "Must check at least one item");
        for (String s : toCheck) {
            if (!Strings.isNullOrEmpty(s)) {
                return false;
            }
        }
        return true;
    }

    public static String escapeForJsonPointerSegment(String string) {
        return jsonPointerSegmentEscaper().escape(string);
    }

    public static String escapeForURIPointerSegment(String string) {
        return urlPathSegmentEscaper().escape(escapeForJsonPointerSegment(string));
    }

    public static Splitter forwardSlashSeparator() {
        return FORWARD_SLASH_SEPARATOR;
    }

    public static Escaper jsonPointerSegmentEscaper() {
        return JSON_POINTER_SEGMENT_ESCAPER;
    }

    public static Unescaper jsonPointerSegmentUnescaper() {
        return JSON_POINTER_SEGMENT_UNESCAPER;
    }

    public static int tryParsePositiveInt(final String s) {
        if (s == null) {
            throw new NumberFormatException("Null string");
        }

        int num = 0;
        final int len = s.length();

        // Build the number.
        final int max = -Integer.MAX_VALUE;
        final int multmax = max / 10;

        int i = 0;
        while (i < len) {
            int d = s.charAt(i++) - '0';
            if ((d < 0 || d > 9) || (num < multmax)) {
                return -1;
            }

            num *= 10;
            if (num < (max + d)) {
                return -1;
            }
            num -= d;
        }
        return -1 * num;
    }

    public static Unescaper urlSegmentUnescaper() {
        return URL_SEGMENT_UNESCAPER;
    }
}

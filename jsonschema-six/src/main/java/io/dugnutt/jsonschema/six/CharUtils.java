package io.dugnutt.jsonschema.six;

import com.google.common.base.Splitter;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import lombok.SneakyThrows;

import java.net.URLDecoder;

public class CharUtils {
    public static final Escaper JSON_POINTER_SEGMENT_ESCAPER = Escapers.builder()
            .addEscape('~', "~0")
            .addEscape('/', "~1")
            .addEscape('"', "\\")
            .addEscape('\\', "\\\\")
            .build();

    public static final Unescaper JSON_POINTER_SEGMENT_UNESCAPER = Unescapers.builder()
            .addUnescape("~0", "~")
            .addUnescape("~1", "/")
            .addUnescape("\\", "\"")
            .addUnescape("\\\\", "\\")
            .build();

    public static final Splitter FORWARD_SLASH_SEPARATOR = Splitter.on('/').omitEmptyStrings().trimResults();

    public static final Unescaper URL_SEGMENT_UNESCAPER = new Unescaper() {
        @Override
        @SneakyThrows
        public String unescape(CharSequence string) {
            return URLDecoder.decode(string.toString(), "UTF-8");
        }
    };

}

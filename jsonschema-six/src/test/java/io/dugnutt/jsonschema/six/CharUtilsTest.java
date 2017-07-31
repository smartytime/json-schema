package io.dugnutt.jsonschema.six;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;
import io.dugnutt.jsonschema.utils.CharUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.Duration;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class CharUtilsTest {

    @Test
    public void testtryParsePositiveInt_HappyPath() {
        int i = CharUtils.tryParsePositiveInt("331");
        Assertions.assertThat(i).isEqualTo(331);
    }

    @Test
    public void testtryParsePositiveInt_HappyPath_Decimal() {
        int i = CharUtils.tryParsePositiveInt("1.1");
        Assertions.assertThat(i).isEqualTo(-1);
    }

    @Test
    public void testtryParsePositiveInt_HappyPath_Large() {
        int i = CharUtils.tryParsePositiveInt("12354312");
        Assertions.assertThat(i).isEqualTo(12354312);
    }

    @Test
    public void testtryParsePositiveInt_HappyPath_Negative() {
        int i = CharUtils.tryParsePositiveInt("-5");
        Assertions.assertThat(i).isEqualTo(-1);
    }

    @Test
    public void testtryParsePositiveInt_HappyPath_Zero() {
        int i = CharUtils.tryParsePositiveInt("0");
        Assertions.assertThat(i).isEqualTo(0);
    }

    @Test
    public void testtryParsePositiveInt_NonHappyPath() {
        int i = CharUtils.tryParsePositiveInt("33d1");
        Assertions.assertThat(i).isEqualTo(-1);
    }

    @Test
    public void testtryParsePositiveInt_Perf() {
        String[] attempts = new String[2000000];
        char[] combinations = new char[] {
                '1','2','3','4','5','6','.','3','-'
        };
        Random random = new Random(System.currentTimeMillis());
        int i=0;
        for (String attempt : attempts) {
            StringBuilder string= new StringBuilder();
            int digits = random.nextInt(5) + 1;
            for(int d = 0; d<digits; d++) {
                char pickedChar = combinations[random.nextInt(combinations.length)];
                string.append(pickedChar);
            }
            attempts[i++] = string.toString();
        }

        Set<Integer> parsedGoog = new HashSet<>();
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (String attempt : attempts) {
            Integer parsed = Ints.tryParse(attempt);
        }
        Duration elapsed = stopwatch.elapsed();

        stopwatch.reset().start();
        for (String attempt : attempts) {
            int parsed = CharUtils.tryParsePositiveInt(attempt);
        }
        Duration elapsedMe = stopwatch.elapsed();

        System.out.println(elapsedMe.toMillis() / (double) elapsed.toMillis());
    }
}
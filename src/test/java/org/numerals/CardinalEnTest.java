/*
 * Copyright 2007-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.numerals;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Verifies the English port against the Java engine. */
class CardinalEnTest {

    private static final Locale EN = Locale.of("en");

    private static String cardinal(String number) {
        return CardinalEngine.cardinal(number, EN);
    }

    @ParameterizedTest
    @CsvSource({
        // units
        "0, zero",
        "1, one",
        "9, nine",
        // teens and special tens
        "10, ten",
        "11, eleven",
        "12, twelve",
        "13, thirteen",
        "14, fourteen",
        "15, fifteen",
        "16, sixteen",
        "17, seventeen",
        "18, eighteen",
        "19, nineteen",
        "20, twenty",
        // compound tens
        "21, twenty-one",
        "30, thirty",
        "99, ninety-nine",
        // hundreds
        "100, one hundred",
        "101, one hundred one",
        "999, nine hundred ninety-nine",
        // thousands
        "1000, one thousand",
        "2000, two thousand",
        // millions and composition
        "1000000, one million",
        "21000000, twenty-one million",
        "1234567, one million two hundred thirty-four thousand five hundred sixty-seven",
        // extended short scale
        "1000000000, one billion",
        "1000000000000, one trillion",
        "1000000000000000, one quadrillion",
        "1000000000000000000, one quintillion",
        "1000000000000000000000, one sextillion",
    })
    void rendersEnglishCardinals(String number, String expected) {
        assertEquals(expected, cardinal(number));
    }

    @Test
    void stripsLeadingZeros() {
        assertEquals("one", cardinal("001"));
        assertEquals("zero", cardinal("000"));
    }

    @Test
    void rejectsNonDigits() {
        assertThrows(NumberFormatException.class, () -> cardinal("12a"));
    }

    @Test
    void maxRange24Digits() {
        // 24 nines must render fully, up to sextillion (range matches Spanish).
        String out = cardinal("999999999999999999999999");
        assertTrue(out.startsWith("nine hundred ninety-nine sextillion"),
                "24-digit max should render up to sextillion: " + out);
    }
}

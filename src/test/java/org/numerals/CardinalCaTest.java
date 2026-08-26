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

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Verifica el port del idioma Catalan (ca): vint-i-, apocope u/un, centenas con guion, escala larga. */
class CardinalCaTest {

    private static final Locale CA = Locale.of("ca", "ES");

    private static String cardinal(String number) {
        return CardinalEngine.cardinal(number, CA);
    }

    @ParameterizedTest
    @CsvSource({
        "0, zero",
        "1, u",
        "10, deu",
        "11, onze",
        "15, quinze",
        "16, setze",
        "17, disset",
        "18, divuit",
        "19, dinou",
        "20, vint",
        // veintenas con "i"
        "21, vint-i-u",
        "22, vint-i-dos",
        "23, vint-i-tres",
        "29, vint-i-nou",
        // 30..90 con guion y "un"
        "31, trenta-un",
        "32, trenta-dos",
        "41, quaranta-un",
        "99, noranta-nou",
        // centenas
        "100, cent",
        "101, cent u",
        "123, cent vint-i-tres",
        "200, dos-cents",
        "500, cinc-cents",
        "999, nou-cents noranta-nou",
        // mil (no "un mil")
        "1000, mil",
        "1001, mil u",
        "2000, dos mil",
        "31000, trenta-un mil",
        // apocope "un" ante palabra de escala
        "1000000, un milió",
        "1000000000000, un bilió",
        "21000000, vint-i-un milions",
        "21000000000000, vint-i-un bilions",
        // "nou"/"vuit" NO se apocopan
        "9000000, nou milions",
        "8000000, vuit milions",
        // escala larga
        "1000000000, mil milions",
        "1000000000000000, mil bilions",
        "1234567, un milió dos-cents trenta-quatre mil cinc-cents seixanta-set",
        "1000000000000000000, un trilió",
        // ceros a la izquierda
        "000, zero",
        "007, set",
    })
    void rendersCatalanCardinals(String number, String expected) {
        assertEquals(expected, cardinal(number));
    }

    @Test
    void rejectsNonDigits() {
        assertThrows(NumberFormatException.class, () -> cardinal("12.5"));
    }
}

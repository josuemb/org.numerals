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

/** Verifica el port del idioma Galego (gl), incluido el conector "e". */
class CardinalGlTest {

    private static final Locale GL = Locale.of("gl", "ES");

    private static String cardinal(String number) {
        return CardinalEngine.cardinal(number, GL);
    }

    @ParameterizedTest
    @CsvSource({
        "0, cero",
        "1, un",
        "10, dez",
        "11, once",
        "15, quince",
        "16, dezaseis",
        "19, dezanove",
        "20, vinte",
        // conector "e"
        "21, vinte e un",
        "32, trinta e dous",
        "99, noventa e nove",
        // centenas cen/cento
        "100, cen",
        "101, cento e un",
        "123, cento e vinte e tres",
        "200, douscentos",
        "500, cincocentos",
        "999, novecentos e noventa e nove",
        // mil
        "1000, mil",
        "2000, dous mil",
        // conector "e" entre grupos
        "1001, mil e un",
        "1100, mil e cen",
        "2015, dous mil e quince",
        "2500, dous mil e cincocentos",
        "1000001, un millón e un",
        "1000100, un millón e cen",
        "1000234, un millón douscentos e trinta e catro",
        "1234567, un millón douscentos e trinta e catro mil cincocentos e sesenta e sete",
        // escala y plurales
        "1000000, un millón",
        "21000000, vinte e un millóns",
        "1000000000000, un billón",
        "1000000000000000000, un trillón",
        // ceros a la izquierda
        "000, cero",
        "007, sete",
    })
    void rendersGalicianCardinals(String number, String expected) {
        assertEquals(expected, cardinal(number));
    }

    @Test
    void rejectsNonDigits() {
        assertThrows(NumberFormatException.class, () -> cardinal("abc"));
    }
}

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

/** Verifica el port del idioma Espanol contra el motor Java. */
class CardinalEsTest {

    private static final Locale ES = Locale.of("es");

    private static String cardinal(String number) {
        return CardinalEngine.cardinal(number, ES);
    }

    @ParameterizedTest
    @CsvSource({
        // unidades
        "0, cero",
        "1, uno",
        "9, nueve",
        // teens y decenas especiales
        "10, diez",
        "11, once",
        "15, quince",
        "16, dieciséis",
        "19, diecinueve",
        "20, veinte",
        "21, veintiuno",
        "22, veintidós",
        "23, veintitrés",
        "25, veinticinco",
        "26, veintiséis",
        // decenas compuestas
        "30, treinta",
        "33, treinta y tres",
        "99, noventa y nueve",
        // centenas
        "100, cien",
        "101, ciento uno",
        "123, ciento veintitrés",
        "200, doscientos",
        "500, quinientos",
        "999, novecientos noventa y nueve",
        // miles: "mil" (no "un mil"), y plural
        "1000, mil",
        "1001, mil uno",
        "2000, dos mil",
        "21000, veintiún mil",
        // millones: apocope "un millon", plural "millones"
        "1000000, un millón",
        "2000000, dos millones",
        "1000001, un millón uno",
    })
    void rendersSpanishCardinals(String number, String expected) {
        assertEquals(expected, cardinal(number));
    }

    @Test
    void stripsLeadingZeros() {
        assertEquals("uno", cardinal("001"));
        assertEquals("cero", cardinal("000"));
    }

    @Test
    void rejectsNonDigits() {
        assertThrows(NumberFormatException.class, () -> cardinal("12a"));
    }

    @Test
    void handlesMaxLength24Digits() {
        // 1 seguido de 23 ceros = un trillon (escala larga es: trillon = 10^18)
        String value = "1" + "0".repeat(18);
        assertEquals("un trillón", cardinal(value));
    }
}

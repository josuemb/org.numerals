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

/** Verifica'l puertu de la llingua Asturiana contra'l motor Java. */
class CardinalAstTest {

    private static final Locale AST = Locale.of("ast");

    private static String cardinal(String number) {
        return CardinalEngine.cardinal(number, AST);
    }

    @ParameterizedTest
    @CsvSource({
        // unidaes
        "0, ceru",
        "1, unu",
        "7, siete",
        "9, nueve",
        // teens y decenes especiales
        "10, diez",
        "11, once",
        "15, quince",
        "16, dieciséis",
        "19, diecinueve",
        "20, venti",
        // vientigrupu: 21 ye siempres "ventiún" (apocopau con tilde)
        "21, ventiún",
        "23, ventitrés",
        "25, venticinco",
        // decenes compuestes (apocope unu->un)
        "30, trenta",
        "31, trenta y un",
        "40, cuarenta",
        "70, setenta",
        // centenes: cien exactu / cientu con restu
        "100, cien",
        "101, cientu unu",
        "123, cientu ventitrés",
        "200, doscientos",
        "500, quinientos",
        // miles: "mil" (non "un mil"), y composicion
        "1000, mil",
        "1001, mil unu",
        "2000, dos mil",
        // apocope na composicion de grupos
        "21000, ventiún mil",
        "31000, trenta y un mil",
        "21000000, ventiún millones",
        // millones/billones: apocope "un millón", plural "millones"
        "1000000, un millón",
        "1000001, un millón unu",
        "1000000000000, un billón",
        // composicion llarga
        "1234567, un millón doscientos trenta y cuatro mil quinientos sesenta y siete",
    })
    void rendersAsturianCardinals(String number, String expected) {
        assertEquals(expected, cardinal(number));
    }

    @Test
    void stripsLeadingZeros() {
        assertEquals("ceru", cardinal("000"));
        assertEquals("siete", cardinal("007"));
    }

    @Test
    void rejectsNonDigits() {
        assertThrows(NumberFormatException.class, () -> cardinal("abc"));
        assertThrows(NumberFormatException.class, () -> cardinal("12.5"));
        assertThrows(NumberFormatException.class, () -> cardinal("-5"));
    }

    @Test
    void handlesScaleCeiling() {
        // 10^18 = un trillón ye la pallabra d'escala mas alta definida.
        assertEquals("un trillón", cardinal("1000000000000000000"));
    }
}

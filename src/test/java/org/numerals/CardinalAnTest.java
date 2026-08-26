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

/**
 * Verifica o puerto de l'aragonés (an) contra o motor Java. As cadenas asperadas
 * salen d'o test Groovy {@code CardinalEdgeCasesTest} y d'as reglas d'a Biquipedia
 * (Academia de l'Aragonés, EDACAR 7, 2010).
 */
class CardinalAnTest {

    private static final Locale AN = Locale.of("an");

    private static String cardinal(String number) {
        return CardinalEngine.cardinal(number, AN);
    }

    @ParameterizedTest
    @CsvSource({
        // unidaz y formas aragonesas
        "0, zero",
        "1, uno",
        "8, ueito",
        "9, nueu",
        // teens
        "11, once",
        "15, quince",
        "16, deciséis",
        // decenas (formas normativas de l'Academia)
        "20, vente",
        "30, trenta",
        "60, sisanta",
        "70, setanta",
        "80, uitanta",
        "90, novanta",
        // 20-29: soldadas con tono grafico
        "21, ventiuno",
        "22, ventidós",
        "23, ventitrés",
        "26, ventiséis",
        // 30-99: decena + "y" + unidat sin tono
        "33, trenta y tres",
        "36, trenta y seis",
        "99, novanta y nueu",
        // centenas: cient / X cientos
        "100, cient",
        "101, cient uno",
        "123, cient ventitrés",
        "200, dos cientos",
        "500, cinco cientos",
        "999, nueu cientos novanta y nueu",
        // "mil" (no "un mil") y millars
        "1000, mil",
        "1001, mil uno",
        "2000, dos mil",
        // apocope uno -> un / ventiuno -> ventiún en grupos compuestos
        "21000, ventiún mil",
        "31000, trenta y un mil",
        "1000000, un milión",
        "1000000000000, un billón",
        // composición larga
        "1234567, un milión dos cientos trenta y cuatro mil cinco cientos sisanta y siete",
    })
    void rendersAragoneseCardinals(String number, String expected) {
        assertEquals(expected, cardinal(number));
    }

    @Test
    void leadingZerosAreIgnored() {
        assertEquals("zero", cardinal("000"));
        assertEquals("siete", cardinal("007"));
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(NumberFormatException.class, () -> new NumberValue(""));
        assertThrows(NumberFormatException.class, () -> cardinal("abc"));
        assertThrows(NumberFormatException.class, () -> cardinal("12.5"));
        assertThrows(NumberFormatException.class, () -> cardinal("-5"));
    }

    @Test
    void scaleUpToEighteenDigitsWorks() {
        // 10^18 = un trillón ye a zaguera parola d'escala definida.
        assertEquals("un trillón", cardinal("1000000000000000000"));
    }
}

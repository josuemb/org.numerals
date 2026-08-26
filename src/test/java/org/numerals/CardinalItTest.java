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

/** Verifica el port del idioma Italiano (it): elision ventuno/ventotto, mille/mila, escala -ione/-iardo. */
class CardinalItTest {

    private static final Locale IT = Locale.of("it", "IT");

    private static String cardinal(String number) {
        return CardinalEngine.cardinal(number, IT);
    }

    @ParameterizedTest
    @CsvSource({
        "0, zero",
        "1, uno",
        "9, nove",
        "10, dieci",
        "11, undici",
        "12, dodici",
        "15, quindici",
        "16, sedici",
        "17, diciassette",
        "18, diciotto",
        "19, diciannove",
        "20, venti",
        // elision de la vocal de la decena ante uno/otto
        "21, ventuno",
        "28, ventotto",
        "31, trentuno",
        "38, trentotto",
        "91, novantuno",
        "98, novantotto",
        // sin elision
        "22, ventidue",
        "23, ventitre",
        "35, trentacinque",
        "99, novantanove",
        // centenas: cento invariable
        "100, cento",
        "101, cento uno",
        "123, cento ventitre",
        "200, duecento",
        "500, cinquecento",
        "999, novecento novantanove",
        // mille / mila
        "1000, mille",
        "2000, duemila",
        "1001, milleuno",
        "2500, duemilacinquecento",
        // escala con elision "un"
        "1000000, un milione",
        "21000000, ventun milioni",
        "1000000000, un miliardo",
        "5000000000, cinque miliardi",
        "1000000000000, un bilione",
        "1000000000000000, un biliardo",
        "1000000000000000000, un trilione",
        // composicion grande
        "1234567, un milione duecento trentaquattromilacinquecento sessantasette",
        // ceros a la izquierda
        "000, zero",
        "007, sette",
    })
    void rendersItalianCardinals(String number, String expected) {
        assertEquals(expected, cardinal(number));
    }

    @Test
    void rejectsNonDigits() {
        assertThrows(NumberFormatException.class, () -> cardinal("12.5"));
    }
}

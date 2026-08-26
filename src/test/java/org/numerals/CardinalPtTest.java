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

/** Verifica el port del idioma Portugues (pt), incluido el conector "e". */
class CardinalPtTest {

    private static final Locale PT = Locale.of("pt", "BR");

    private static String cardinal(String number) {
        return CardinalEngine.cardinal(number, PT);
    }

    @ParameterizedTest
    @CsvSource({
        "0, zero",
        "1, um",
        "10, dez",
        "11, onze",
        "15, quinze",
        "16, dezesseis",
        "19, dezenove",
        "20, vinte",
        // conector "e" en decenas
        "21, vinte e um",
        "23, vinte e tres",
        "99, noventa e nove",
        // centenas cem/cento
        "100, cem",
        "101, cento e um",
        "123, cento e vinte e tres",
        "200, duzentos",
        "500, quinhentos",
        "999, novecentos e noventa e nove",
        // mil (no "um mil")
        "1000, mil",
        "2000, dois mil",
        // conector "e" entre grupos
        "1001, mil e um",
        "1100, mil e cem",
        "2500, dois mil e quinhentos",
        "2015, dois mil e quinze",
        "1000001, um milhão e um",
        "1000100, um milhão e cem",
        // espacio (sin "e") cuando el resto tiene centenas + mas
        "1234, mil duzentos e trinta e quatro",
        "1000234, um milhão duzentos e trinta e quatro",
        "1234567, um milhão duzentos e trinta e quatro mil quinhentos e sessenta e sete",
        // escala y plurales
        "1000000, um milhão",
        "21000000, vinte e um milhões",
        "1000000000000, um bilhão",
        "1000000000000000000, um trilhão",
    })
    void rendersPortugueseCardinals(String number, String expected) {
        assertEquals(expected, cardinal(number));
    }

    @Test
    void leadingZerosAreIgnored() {
        assertEquals("zero", cardinal("000"));
    }

    @Test
    void rejectsNonDigits() {
        assertThrows(NumberFormatException.class, () -> cardinal("abc"));
    }
}

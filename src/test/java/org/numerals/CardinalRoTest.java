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
 * Verifica el port del idioma Rumano (ro): teens "spre", conector "si",
 * regla "de" ante palabra de escala, genero de 1 (o mie / un milion).
 */
class CardinalRoTest {

    private static final Locale RO = Locale.of("ro", "RO");

    private static String cardinal(String number) {
        return CardinalEngine.cardinal(number, RO);
    }

    @ParameterizedTest
    @CsvSource({
        // unidades
        "0, zero",
        "1, unu",
        "2, doi",
        "3, trei",
        "5, cinci",
        "9, noua",
        "10, zece",
        // teens "spre" (14/16 irregulares)
        "11, unsprezece",
        "12, doisprezece",
        "13, treisprezece",
        "14, paisprezece",
        "15, cincisprezece",
        "16, saisprezece",
        "17, saptesprezece",
        "18, optsprezece",
        "19, nouasprezece",
        // conector "si"
        "20, douazeci",
        "21, douazeci si unu",
        "22, douazeci si doi",
        "30, treizeci",
        "35, treizeci si cinci",
        "60, saizeci",
        "99, nouazeci si noua",
        // centenas
        "100, o suta",
        "101, o suta unu",
        "123, o suta douazeci si trei",
        "200, doua sute",
        "300, trei sute",
        "500, cinci sute",
        "999, noua sute nouazeci si noua",
        // miles: "o mie" (femenino)
        "1000, o mie",
        "1001, o mie unu",
        "2000, doua mii",
        "3000, trei mii",
        // regla "de" ante escala
        "20000, douazeci de mii",
        "100000000, o suta de milioane",
        "5000000, cinci milioane",
        "999999, noua sute nouazeci si noua de mii noua sute nouazeci si noua",
        // genero de 1 ante escala
        "1000000, un milion",
        "1000000000, un miliard",
        "21000, douazeci si una de mii",
        "21000000, douazeci si unu de milioane",
        "101000, o suta una mii",
        // escala en magnitudes reales
        "2000000, doua milioane",
        "1000000000000, un trilion",
        // composicion grande
        "1234567, un milion doua sute treizeci si patru de mii cinci sute saizeci si sapte",
        // ceros a la izquierda
        "000, zero",
        "007, sapte",
    })
    void rendersRomanianCardinals(String number, String expected) {
        assertEquals(expected, cardinal(number));
    }

    @Test
    void rejectsNonDigits() {
        assertThrows(NumberFormatException.class, () -> cardinal("12.5"));
    }
}

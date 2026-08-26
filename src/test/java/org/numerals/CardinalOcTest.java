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
import org.numerals.rules.oc.CardinalRulesOc;

/**
 * Verifica el port del idioma Occitano (oc, lengadocian / norma clasica) contra
 * el motor Java.
 *
 * <p>El motor {@link CardinalEngine} no registra "oc" (por indicacion del port),
 * asi que el test construye el {@link RuleSet} directamente con
 * {@link CardinalRulesOc#ruleSet()} y lo renderiza via el metodo de paquete
 * {@link CardinalEngine#cardinalOf(NumberValue, RuleSet)}, sin depender del
 * registro de locales. El Locale se conserva por fidelidad con la suite Groovy.
 */
class CardinalOcTest {

    @SuppressWarnings("unused")
    private static final Locale OC = Locale.of("oc", "FR");

    private static final RuleSet RULES = CardinalRulesOc.ruleSet();

    private static String cardinal(String number) {
        return CardinalEngine.cardinalOf(new NumberValue(number), RULES);
    }

    @ParameterizedTest
    @CsvSource({
        // unitats, teens especificas e desenas (0..20)
        "0, zèro",
        "1, un",
        "6, sièis",
        "8, uèch",
        "9, nòu",
        "10, dètz",
        "11, onze",
        "14, catòrze",
        "15, quinze",
        "16, setze",
        "20, vint",
        // 17..19 se compausan amb lo connector "-e-"
        "17, dètz-e-sèt",
        "18, dètz-e-uèch",
        "19, dètz-e-nòu",
        // la vintena garda lo connector "-e-"
        "21, vint-e-un",
        "23, vint-e-tres",
        "29, vint-e-nòu",
        // 30..90 se ligan amb un espaci simple, sens connector
        "30, trenta",
        "31, trenta un",
        "57, cinquanta sèt",
        "99, nonanta nòu",
        // centenas: 100 exacte es "cent" (pas "un cent"); plural "cents"
        "100, cent",
        "101, cent un",
        "123, cent vint-e-tres",
        "200, dos cents",
        "500, cinc cents",
        "999, nòu cents nonanta nòu",
        // milièrs: 1000 es "mila" (pas "un mila")
        "1000, mila",
        "1001, mila un",
        "2000, dos mila",
        // milions e miliards
        "1000000, un milion",
        "21000000, vint-e-un milions",
        "1000000000, un miliard",
    })
    void rendersOccitanCardinals(String number, String expected) {
        assertEquals(expected, cardinal(number));
    }

    @Test
    void compositeNumber() {
        assertEquals(
                "un milion dos cents trenta quatre mila cinc cents seissanta sèt",
                cardinal("1234567"));
    }

    @Test
    void leadingZerosAreIgnored() {
        assertEquals("zèro", cardinal("000"));
        assertEquals("sèt", cardinal("007"));
    }

    @Test
    void inputValidation() {
        assertThrows(NumberFormatException.class, () -> new NumberValue(null));
        assertThrows(NumberFormatException.class, () -> new NumberValue(""));
        assertThrows(NumberFormatException.class, () -> new NumberValue("abc"));
        assertThrows(NumberFormatException.class, () -> new NumberValue("12.5"));
        assertThrows(NumberFormatException.class, () -> new NumberValue("-5"));
    }
}

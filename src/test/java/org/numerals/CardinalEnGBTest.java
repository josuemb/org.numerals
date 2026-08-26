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
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.numerals.rules.en.gb.CardinalRulesEnGB;

/**
 * Verifies the British English (en_GB) port against the Java engine.
 *
 * <p>en_GB is not registered in {@link CardinalEngine}, so this test builds the
 * rule set directly through {@link CardinalRulesEnGB#ruleSet()} and renders it
 * with the package-private {@link CardinalEngine#cardinalOf(NumberValue, RuleSet)}.
 * The locale is built with {@code Locale.of("en", "GB")} for documentation, even
 * though resolution is bypassed. Expected outputs are derived from the Groovy
 * rules and the Groovy edge-case test: the only difference from base English is
 * the "and" connector inserted before a trailing group below one hundred.
 */
class CardinalEnGBTest {

    @SuppressWarnings("unused")
    private static final Locale EN_GB = Locale.of("en", "GB");

    private static final RuleSet RULES = CardinalRulesEnGB.ruleSet();

    private static String cardinal(String number) {
        return CardinalEngine.cardinalOf(new NumberValue(number), RULES);
    }

    @ParameterizedTest
    @CsvSource({
        // Below one hundred there is no trailing group, so "and" never appears.
        "0, zero",
        "1, one",
        "9, nine",
        "10, ten",
        "11, eleven",
        "16, sixteen",
        "19, nineteen",
        "20, twenty",
        "21, twenty-one",
        "99, ninety-nine",
        // Hundreds insert "and" before a remainder below one hundred.
        "100, one hundred",
        "101, one hundred and one",
        "105, one hundred and five",
        "110, one hundred and ten",
        "123, one hundred and twenty-three",
        "345, three hundred and forty-five",
        "999, nine hundred and ninety-nine",
        // Thousands insert "and" before a remainder below one hundred.
        "1000, one thousand",
        "1005, one thousand and five",
        "1023, one thousand and twenty-three",
        "2001, two thousand and one",
        // A remainder of one hundred or greater takes no "and" at this level.
        "1100, one thousand one hundred",
        "1105, one thousand one hundred and five",
        // Millions.
        "1000000, one million",
        "1000001, one million and one",
        // "and" appears within each hundreds group, not between scale groups.
        "1234567, one million two hundred and thirty-four thousand five hundred and sixty-seven",
    })
    void rendersBritishCardinals(String number, String expected) {
        assertEquals(expected, cardinal(number));
    }

    @Test
    void stripsLeadingZeros() {
        assertEquals("one", cardinal("001"));
        assertEquals("zero", cardinal("000"));
    }

    @Test
    void unitsTeensTensNeverContainAnd() {
        assertFalse(cardinal("21").contains(" and "),
                "Numbers below one hundred must not contain the 'and' connector");
    }
}

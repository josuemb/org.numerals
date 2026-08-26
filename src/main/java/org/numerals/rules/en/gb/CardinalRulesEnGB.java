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

package org.numerals.rules.en.gb;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.function.Function;

import org.numerals.NumberValue;
import org.numerals.RuleSet;

/**
 * Cardinal rules for British English (en_GB).
 *
 * <p>Direct port of the Groovy class {@code org.numerals.rules.en.GB.CardinalRules}.
 * British English is a locale variant of the base English (en) rules and is
 * identical except that the word "and" is inserted before the final group of a
 * number when that remaining group is below one hundred, for example
 * "one hundred and twenty-three", "one thousand and five", "one million and one".
 * The base English (en) rules omit this connector.
 *
 * <p>The structure (units/tens tables + suffix table by position range +
 * recursive composition) is preserved; what changes is the form: the Groovy
 * closures are now {@link org.numerals.CardinalRule} lambdas and the Map literals
 * are {@link java.util.Map#of}. Groovy's {@code number[-1]} maps to
 * {@link NumberValue#lastDigit()}, {@code number[-1..-1]} to
 * {@link NumberValue#lastDigits(int)}, and the high-order group slice to
 * {@link NumberValue#firstDigits(int)}.
 */
public final class CardinalRulesEnGB {

    private static final int POSITION_MIN = 1;
    private static final int POSITION_MAX = 24;
    private static final int DIGIT_MIN = 0;
    private static final int DIGIT_MAX = 9;

    private static final String SEPARATOR_COMMON = " ";
    private static final String SEPARATOR_TENS = "-";
    // British English connector inserted before a trailing group below 100.
    private static final String SEPARATOR_AND = " and ";
    // A group with fewer than this many digits is below one hundred, so the
    // "and" connector applies before it (units or tens remainder).
    private static final int BELOW_HUNDRED_DIGITS = 3;

    private static final Map<Integer, String> units = Map.of(
            0, "zero", 1, "one", 2, "two", 3, "three", 4, "four",
            5, "five", 6, "six", 7, "seven", 8, "eight", 9, "nine");

    private static final Map<Integer, String> tens = Map.of(
            1, "ten", 2, "twenty", 3, "thirty", 4, "forty", 5, "fifty",
            6, "sixty", 7, "seventy", 8, "eighty", 9, "ninety");

    private static final Map<Integer, String> specialTens = Map.of(1, "teen");

    private static final Map<Integer, String> specialTensOne = Map.of(
            1, "eleven", 2, "twelve", 3, "thirteen", 5, "fifteen", 8, "eighteen");

    /** Scale suffix by position range. */
    private record Suffix(int from, int to, String suffix) {
        boolean covers(int position) {
            return position >= from && position <= to;
        }
    }

    private static final Suffix[] suffixes = {
        new Suffix(3, 3, "hundred"),
        new Suffix(4, 6, "thousand"),
        new Suffix(7, 9, "million"),
        new Suffix(10, 12, "billion"),
        new Suffix(13, 15, "trillion"),
        new Suffix(16, 18, "quadrillion"),
        new Suffix(19, 21, "quintillion"),
        new Suffix(22, 24, "sextillion"),
    };

    private CardinalRulesEnGB() {
    }

    /** Builds the rule set for all supported positions. */
    public static RuleSet ruleSet() {
        RuleSet rules = new RuleSet();
        for (int position = POSITION_MIN; position <= POSITION_MAX; position++) {
            switch (position) {
                case 1 -> registerUnits(rules);
                case 2 -> registerTens(rules);
                default -> registerCommon(rules, position);
            }
        }
        return rules;
    }

    private static Suffix getSuffix(int position) {
        for (Suffix suffix : suffixes) {
            if (suffix.covers(position)) {
                return suffix;
            }
        }
        throw new MissingResourceException(
                "Cannot found class suffix for position:" + position,
                CardinalRulesEnGB.class.getName(), "getSuffix");
    }

    // Units (position 1). Zero only sounds "zero" when it is the whole number.
    private static void registerUnits(RuleSet rules) {
        for (int digit = DIGIT_MIN; digit <= DIGIT_MAX; digit++) {
            int d = digit;
            rules.put(1, digit, (number, recurse) ->
                    (d == 0 && number.size() != 1) ? "" : units.get(d));
        }
    }

    // Tens (position 2): digit 1 covers teens; other tens compose "X-unit".
    private static void registerTens(RuleSet rules) {
        for (int digit = DIGIT_MIN; digit <= DIGIT_MAX; digit++) {
            int d = digit;
            if (digit == 1) {
                rules.put(2, digit, (number, recurse) -> {
                    int u = number.lastDigit();
                    switch (u) {
                        case 0:
                            return tens.get(d);
                        case 1:
                        case 2:
                        case 3:
                        case 5:
                        case 8:
                            return specialTensOne.get(u);
                        default:
                            return recurse.apply(number.lastDigits(1)) + specialTens.get(d);
                    }
                });
            } else {
                rules.put(2, digit, (number, recurse) -> cardinalTens(number, recurse, d));
            }
        }
    }

    // "twenty", "thirty-three", etc. (tens 2-9).
    private static String cardinalTens(
            NumberValue number, Function<NumberValue, String> recurse, int ten) {
        StringBuilder cardinal = new StringBuilder(tens.get(ten));
        if (number.lastDigit() != 0) {
            cardinal.append(SEPARATOR_TENS).append(recurse.apply(number.lastDigits(1)));
        }
        return cardinal.toString();
    }

    // Positions 3+ (hundreds, thousands, millions, ...): all digits use the common case.
    private static void registerCommon(RuleSet rules, int position) {
        for (int digit = DIGIT_MIN; digit <= DIGIT_MAX; digit++) {
            rules.put(position, digit, (number, recurse) -> cardinalCommon(number, recurse));
        }
    }

    /**
     * Common long-scale case: separates the high-order group from its remainder
     * and applies the suffix. British English inserts "and" before the trailing
     * group when that group is below one hundred (for example "one hundred and
     * five", "one thousand and twenty-three", "one million and one"). When the
     * trailing group is one hundred or greater no connector is used at this
     * level; the "and" then appears deeper in the recursion, inside that group.
     */
    private static String cardinalCommon(NumberValue number, Function<NumberValue, String> recurse) {
        StringBuilder cardinal = new StringBuilder();

        int pos = number.size();
        Suffix suffix = getSuffix(pos);

        // The high-order group spans from the current position to the start of
        // the range; the remainder is the digits below that suffix.
        int groupDigits = pos - suffix.from() + 1;
        NumberValue group = number.firstDigits(groupDigits);
        NumberValue remainder = number.lastDigits(suffix.from() - 1);

        cardinal.append(recurse.apply(group));
        cardinal.append(SEPARATOR_COMMON);
        cardinal.append(suffix.suffix());

        if (!remainder.equalsInt(0)) {
            // British English: "and" before a remainder below one hundred,
            // otherwise a plain space and the remainder recurses on its own.
            if (remainder.size() < BELOW_HUNDRED_DIGITS) {
                cardinal.append(SEPARATOR_AND);
            } else {
                cardinal.append(SEPARATOR_COMMON);
            }
            cardinal.append(recurse.apply(remainder));
        }

        return cardinal.toString();
    }
}

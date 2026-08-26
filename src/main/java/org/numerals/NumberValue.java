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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A validated, immutable numeric value stored as its canonical digit string.
 *
 * <p>Leading zeros are stripped (an all-zero input canonicalizes to "0"), and
 * only decimal digits are accepted. The rule engine addresses digits and
 * sub-ranges through this type, so all the "slice the last N digits" logic that
 * the rules rely on lives here rather than being repeated per language.
 *
 * <p>This is the Java equivalent of the original Groovy {@code Number} class;
 * it is named {@code NumberValue} to avoid clashing with {@link java.lang.Number}.
 */
public final class NumberValue {

    private static final String ZERO = "0";
    private static final Pattern VALID_NUMBER = Pattern.compile("(0*)(\\d*)");

    private final String value;

    /** Builds a value from any object whose {@code toString()} is a digit string. */
    public NumberValue(Object raw) {
        this.value = validate(String.valueOf(raw));
    }

    private NumberValue(String alreadyValid, boolean trusted) {
        this.value = alreadyValid;
    }

    private static String validate(String number) {
        if (number == null || number.isEmpty()) {
            throw new NumberFormatException(
                    "The number: [" + number + "] cannot be null nor empty");
        }
        Matcher matcher = VALID_NUMBER.matcher(number);
        if (!matcher.matches()) {
            throw new NumberFormatException(
                    "The number: [" + number + "] is invalid. Only digits are valid.");
        }
        String leadingZeros = matcher.group(1);
        String rest = matcher.group(2);
        if (!leadingZeros.isEmpty() && rest.isEmpty()) {
            return ZERO;
        }
        return rest;
    }

    /** Number of digits. */
    public int size() {
        return value.length();
    }

    /** The digit at index {@code idx} (0-based from the left) as an int 0-9. */
    public int digitAt(int idx) {
        return value.charAt(idx) - '0';
    }

    /**
     * The last digit (position 1, the units digit). Mirrors the Groovy
     * {@code number[-1]} idiom that the rules use constantly.
     */
    public int lastDigit() {
        return digitAt(value.length() - 1);
    }

    /**
     * A new value holding the last {@code count} digits (re-validated, so any
     * leading zeros in the slice are canonicalized). Mirrors {@code number[-count..-1]}.
     */
    public NumberValue lastDigits(int count) {
        return new NumberValue(value.substring(value.length() - count));
    }

    /**
     * A new value holding the first {@code count} digits. Mirrors the
     * high-order group slice {@code number[-pos..-(from)]} once the caller has
     * translated the negative bounds into a left-anchored length.
     */
    public NumberValue firstDigits(int count) {
        return new NumberValue(value.substring(0, count));
    }

    /** True when this value equals the given integer (compared by canonical text). */
    public boolean equalsInt(long other) {
        return value.equals(Long.toString(other));
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && value.equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}

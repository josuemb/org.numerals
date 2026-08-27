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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Covers the typed overloads (long / BigInteger), supportedLocales(), and that
 * the trusted-slice optimization preserves the exact output of the Object-based
 * path across the full 0-100000 range.
 */
class CardinalEngineApiTest {

    private static final Locale ES = new Locale("es", "MX");

    @Test
    void longOverloadMatchesStringOverload() {
        assertEquals(CardinalEngine.cardinal("45789", ES), CardinalEngine.cardinal(45789L, ES));
        assertEquals(CardinalEngine.cardinal("0", ES), CardinalEngine.cardinal(0L, ES));
        assertEquals(CardinalEngine.cardinal("1000000", ES), CardinalEngine.cardinal(1_000_000L, ES));
    }

    @Test
    void bigIntegerHandlesValuesBeyondLong() {
        // 21 digits: larger than Long.MAX_VALUE (~19 digits), within the 24-digit ceiling.
        BigInteger big = new BigInteger("123456789012345678901");
        String out = CardinalEngine.cardinal(big, ES);
        assertTrue(out != null && !out.isBlank(), "BigInteger beyond long must convert: " + out);
        assertEquals(CardinalEngine.cardinal("123456789012345678901", ES), out);
    }

    @Test
    void supportedLocalesListsAllEleven() {
        Set<String> locales = CardinalEngine.supportedLocales();
        assertEquals(11, locales.size(), locales.toString());
        assertTrue(locales.containsAll(java.util.List.of(
            "es", "en", "en.GB", "pt", "it", "ca", "gl", "ro", "oc", "ast", "an")));
    }

    @Test
    void supportedLocalesIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class,
            () -> CardinalEngine.supportedLocales().add("zz"));
    }

    @Test
    void trustedSlicePreservesOutputAcrossFullRange() {
        // The trusted-slice path must produce byte-for-byte the same output as
        // before for every number that exercises the recursive slicing.
        for (int i = 0; i <= 100000; i++) {
            String viaLong = CardinalEngine.cardinal((long) i, ES);
            String viaString = CardinalEngine.cardinal(Integer.toString(i), ES);
            assertEquals(viaString, viaLong, "mismatch at " + i);
            assertFalse(viaLong.isBlank(), "empty at " + i);
        }
    }
}

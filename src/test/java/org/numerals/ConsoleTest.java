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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Covers the CLI argument parsing in {@link Console}: the {@code --locale} flag
 * in any position and form, and the {@code --help} output. These lock the fix
 * for the bug where {@code --locale} was only honored as the first argument
 * (so {@code numerals 123 --locale es} treated "--locale" and "es" as numbers
 * and fell back to the JVM locale).
 */
class ConsoleTest {

    private static String run(String... args) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            Console.main(args);
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void localeFlagTrailing() {
        // The reported bug: flag after the number must still select Spanish.
        String out = run("45789", "--locale", "es");
        assertTrue(out.contains("[45789]=[cuarenta y cinco mil setecientos ochenta y nueve]"),
            "trailing --locale es must produce Spanish output, was: " + out);
    }

    @Test
    void localeFlagLeading() {
        String out = run("--locale", "es", "45789");
        assertTrue(out.contains("cuarenta y cinco mil setecientos ochenta y nueve"),
            "leading --locale es must produce Spanish output, was: " + out);
    }

    @Test
    void localeFlagEqualsForm() {
        String out = run("45789", "--locale=es");
        assertTrue(out.contains("cuarenta y cinco mil setecientos ochenta y nueve"),
            "--locale=es form must work, was: " + out);
    }

    @Test
    void localeFlagInTheMiddleAppliesToAllNumbers() {
        String out = run("16", "--locale", "es", "22", "100");
        assertTrue(out.contains("[16]=[dieciséis]"), out);
        assertTrue(out.contains("[22]=[veintidós]"), out);
        assertTrue(out.contains("[100]=[cien]"), out);
    }

    @Test
    void localeAcceptsUnderscoreCountryForm() {
        String out = run("101", "--locale", "en_GB");
        assertTrue(out.contains("[101]=[one hundred and one]"), out);
    }

    @Test
    void flagsAreNotTreatedAsNumbers() {
        // Regression guard: no argument that is a flag should show up as an
        // "invalid number" error line.
        String out = run("10", "--locale", "es");
        assertFalse(out.contains("--locale"), "the flag must not be echoed as a number: " + out);
        assertFalse(out.contains("invalid"), "no invalid-number error expected: " + out);
    }

    @Test
    void helpFlagPrintsUsageAndExamples() {
        String out = run("--help");
        assertTrue(out.contains("Usage:"), out);
        assertTrue(out.contains("--locale"), out);
        assertTrue(out.contains("Examples:"), out);
        assertTrue(out.contains("Supported locales:"), out);
    }

    @Test
    void shortHelpFlagMatchesLongForm() {
        assertTrue(run("-h").contains("Usage:"));
    }

    @Test
    void noArgumentsShowsHelp() {
        assertTrue(run().contains("Usage:"), "no args should print help");
    }
}

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

import java.util.Locale;

/**
 * Command-line front end: prints the cardinal of each numeric argument.
 *
 * <p>Usage:
 * <pre>
 *   numerals 123 45 1000
 *   numerals --locale es 123
 * </pre>
 *
 * <p>Without {@code --locale} it uses the JVM default locale, matching the
 * original Groovy {@code Console}. The optional flag is the one deliberate
 * improvement over the Groovy CLI, which could only be steered through
 * {@code -Duser.language}.
 */
public final class Console {

    private Console() {
    }

    public static void main(String[] args) {
        Locale locale = Locale.getDefault();
        int firstNumber = 0;

        if (args.length >= 2 && "--locale".equals(args[0])) {
            locale = Locale.forLanguageTag(args[1].replace('_', '-'));
            firstNumber = 2;
        }

        for (int i = firstNumber; i < args.length; i++) {
            String number = args[i];
            try {
                String cardinal = CardinalEngine.cardinal(number, locale);
                System.out.println("[" + number + "]=[" + cardinal + "]");
            } catch (RuntimeException e) {
                System.out.println("[" + number + "]=[Error:" + e.getMessage() + "]");
            }
        }
    }
}

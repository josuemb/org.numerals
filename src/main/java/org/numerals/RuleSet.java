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

import java.util.HashMap;
import java.util.Map;

/**
 * The rules for one language: a lookup from (position, leading digit) to the
 * {@link CardinalRule} that resolves it.
 *
 * <p>"Position" is the number of digits (1 = units, 2 = tens, 3 = hundreds, and
 * so on); "digit" is the leading digit of the number being resolved. This is the
 * Java form of the Groovy {@code Map<position, Map<digit, Closure>>} that each
 * {@code CardinalRules} class produced through its static initializer.
 */
public final class RuleSet {

    private final Map<Integer, Map<Integer, CardinalRule>> rulesByPosition = new HashMap<>();

    /** Registers the rule for a (position, digit) pair. */
    public void put(int position, int digit, CardinalRule rule) {
        rulesByPosition
                .computeIfAbsent(position, ignored -> new HashMap<>())
                .put(digit, rule);
    }

    /**
     * Looks up the rule for a position and digit, failing the same way the
     * Groovy engine did when a language leaves a slot unpopulated.
     */
    public CardinalRule ruleFor(int position, int digit) {
        Map<Integer, CardinalRule> byDigit = rulesByPosition.get(position);
        if (byDigit == null) {
            throw new IllegalStateException(
                    "Config Error: rules for position [" + position + "] were not provided");
        }
        CardinalRule rule = byDigit.get(digit);
        if (rule == null) {
            throw new IllegalStateException(
                    "Config Error: rule for position [" + position
                            + "], digit [" + digit + "] was not provided");
        }
        return rule;
    }
}

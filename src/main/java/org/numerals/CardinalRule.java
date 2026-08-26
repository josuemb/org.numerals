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

import java.util.function.Function;

/**
 * One rule for turning a number (at a known position and leading digit) into its
 * cardinal text.
 *
 * <p>This is the Java replacement for the Groovy closures the original stored in
 * {@code Map<position, Map<digit, Closure>>}. The Groovy engine had to inspect
 * {@code closure.maximumNumberOfParameters} at runtime to decide whether a rule
 * needed the recursive callback; here every rule takes the same two arguments
 * and a rule that does not recurse simply ignores {@code recurse}, so the engine
 * invokes them all uniformly.
 */
@FunctionalInterface
public interface CardinalRule {

    /**
     * @param number  the number this rule is resolving
     * @param recurse callback to render a sub-slice of the number (the engine
     *                passes {@code CardinalEngine::cardinalOf} bound to the same
     *                rule set), used by rules that compose smaller groups
     * @return the cardinal text for this number under this rule
     */
    String apply(NumberValue number, Function<NumberValue, String> recurse);
}

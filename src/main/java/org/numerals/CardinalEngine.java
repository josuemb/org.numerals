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
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.numerals.rules.an.CardinalRulesAn;
import org.numerals.rules.ast.CardinalRulesAst;
import org.numerals.rules.ca.CardinalRulesCa;
import org.numerals.rules.en.CardinalRulesEn;
import org.numerals.rules.en.gb.CardinalRulesEnGB;
import org.numerals.rules.es.CardinalRulesEs;
import org.numerals.rules.gl.CardinalRulesGl;
import org.numerals.rules.it.CardinalRulesIt;
import org.numerals.rules.oc.CardinalRulesOc;
import org.numerals.rules.pt.CardinalRulesPt;
import org.numerals.rules.ro.CardinalRulesRo;

/**
 * Entry point: turns a number into its cardinal words for a given locale.
 *
 * <p>Two things the original Groovy split across {@code CardinalUtil} and
 * {@code Cardinal} are combined here:
 * <ul>
 *   <li><b>Locale resolution.</b> The Groovy version used
 *       {@code Class.forName("org.numerals.rules.<lang>.CardinalRules")} and
 *       stripped the locale from most specific (language.country.variant) to
 *       least. This version uses an <em>explicit registry</em> keyed by the same
 *       {@code language.country.variant} path, which is compile-time safe and
 *       needs no reflection. Fallback still walks from most to least specific.
 *   <li><b>Rule dispatch.</b> {@code cardinalOf} looks the rule up by the number's
 *       position (digit count) and leading digit, then invokes it, passing itself
 *       as the recursion callback so composite rules can render sub-groups.
 * </ul>
 *
 * <p><b>Thread safety.</b> This class is thread-safe: {@link #cardinal(Object,
 * Locale)} and {@link #cardinal(Object)} may be called concurrently from any
 * number of threads without external synchronization. The rule registry is
 * populated once in a static initializer and only read afterwards; the per-locale
 * rule-set cache is a {@link java.util.concurrent.ConcurrentHashMap} populated
 * with an idempotent factory; and each {@link RuleSet}, once built, is only read.
 * The generation itself holds no mutable state, so CPU-bound bulk work can be
 * parallelized directly, e.g.
 * {@code numbers.parallelStream().map(n -> CardinalEngine.cardinal(n, locale))}.
 */
public final class CardinalEngine {

    /**
     * Registry of locale path -> rule-set factory. New languages register here
     * instead of being discovered by reflection. The factory is a supplier so a
     * language's (immutable) rule set is built lazily and cached on first use.
     */
    private static final Map<String, Supplier<RuleSet>> REGISTRY = new HashMap<>();

    /**
     * Per-locale cache of resolved rule sets. A {@link ConcurrentHashMap} so
     * {@link #cardinal(Object, Locale)} can be called from multiple threads while
     * a not-yet-cached locale is being resolved. The factory passed to
     * {@code computeIfAbsent} is idempotent (it rebuilds the same immutable rule
     * set), so a benign duplicate build under contention is harmless.
     */
    private static final Map<String, RuleSet> CACHE = new ConcurrentHashMap<>();

    static {
        REGISTRY.put("es", CardinalRulesEs::ruleSet);
        REGISTRY.put("en", CardinalRulesEn::ruleSet);
        REGISTRY.put("en.GB", CardinalRulesEnGB::ruleSet);
        REGISTRY.put("pt", CardinalRulesPt::ruleSet);
        REGISTRY.put("it", CardinalRulesIt::ruleSet);
        REGISTRY.put("ca", CardinalRulesCa::ruleSet);
        REGISTRY.put("gl", CardinalRulesGl::ruleSet);
        REGISTRY.put("ro", CardinalRulesRo::ruleSet);
        REGISTRY.put("oc", CardinalRulesOc::ruleSet);
        REGISTRY.put("ast", CardinalRulesAst::ruleSet);
        REGISTRY.put("an", CardinalRulesAn::ruleSet);
    }

    private CardinalEngine() {
    }

    /** Cardinal of {@code value} using the JVM default locale. */
    public static String cardinal(Object value) {
        return cardinal(value, Locale.getDefault());
    }

    /** Cardinal of {@code value} for the given locale, with language/country/variant fallback. */
    public static String cardinal(Object value, Locale locale) {
        RuleSet rules = resolve(locale);
        return cardinalOf(new NumberValue(value), rules);
    }

    /**
     * Renders one number against a rule set: pick the rule for this number's
     * position and leading digit, then apply it. The recursion callback is this
     * same method bound to the same rule set.
     */
    static String cardinalOf(NumberValue number, RuleSet rules) {
        int position = number.size();
        int leadingDigit = number.digitAt(0);
        CardinalRule rule = rules.ruleFor(position, leadingDigit);
        return rule.apply(number, sub -> cardinalOf(sub, rules));
    }

    /**
     * Resolves the rule set for a locale, walking from most specific
     * (language.country.variant) to least (language), mirroring the original
     * {@code getRulesClass} strip loop.
     */
    private static RuleSet resolve(Locale locale) {
        String path = buildPath(locale);
        StringBuilder tried = new StringBuilder();
        while (true) {
            tried.append('[').append(path).append(']');
            Supplier<RuleSet> factory = REGISTRY.get(path);
            if (factory != null) {
                return CACHE.computeIfAbsent(path, ignored -> factory.get());
            }
            int lastDot = path.lastIndexOf('.');
            if (lastDot == -1) {
                break;
            }
            path = path.substring(0, lastDot);
        }
        throw new MissingResourceException(
                "No rules registered for locale, tried " + tried,
                CardinalEngine.class.getName(), locale.toString());
    }

    private static String buildPath(Locale locale) {
        StringBuilder path = new StringBuilder(locale.getLanguage());
        if (!locale.getCountry().isEmpty()) {
            path.append('.').append(locale.getCountry());
        }
        if (!locale.getVariant().isEmpty()) {
            path.append('.').append(locale.getVariant());
        }
        return path.toString();
    }
}

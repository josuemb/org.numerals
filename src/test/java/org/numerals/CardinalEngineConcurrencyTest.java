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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

/**
 * Backs the documented thread-safety guarantee: many threads calling
 * {@link CardinalEngine#cardinal(Object, Locale)} concurrently — including for
 * locales not yet cached — must all succeed and agree with the single-threaded
 * result. Before the cache was a ConcurrentHashMap, concurrent first-touch of a
 * locale could corrupt the plain HashMap.
 */
class CardinalEngineConcurrencyTest {

    @Test
    void parallelStreamOverManyNumbersMatchesSequential() {
        Locale es = new Locale("es", "MX");
        List<String> nums = new ArrayList<>();
        for (int i = 0; i < 20000; i++) {
            nums.add(Integer.toString(i));
        }

        List<String> sequential = nums.stream()
                .map(n -> CardinalEngine.cardinal(n, es))
                .toList();

        List<String> parallel = nums.parallelStream()
                .map(n -> CardinalEngine.cardinal(n, es))
                .toList();

        assertEquals(sequential, parallel,
                "parallel results must be identical to sequential results");
    }

    @Test
    void concurrentFirstTouchOfMultipleLocalesIsSafe() throws InterruptedException {
        // Several locales, all likely uncached at test start, hit at once to
        // exercise the cache's concurrent computeIfAbsent path.
        Locale[] locales = {
            new Locale("es"), new Locale("en"), Locale.forLanguageTag("en-GB"),
            new Locale("pt"), new Locale("it"), new Locale("ca"),
            new Locale("gl"), new Locale("ro"), new Locale("oc"),
            new Locale("ast"), new Locale("an")
        };
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();
        int threads = 32;
        List<Thread> pool = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            final int seed = t;
            Thread thread = new Thread(() -> {
                try {
                    for (int i = 0; i < 2000; i++) {
                        Locale locale = locales[(seed + i) % locales.length];
                        String out = CardinalEngine.cardinal(Integer.toString(i), locale);
                        assertTrue(out != null && !out.isBlank());
                    }
                } catch (Throwable e) {
                    errors.add(e);
                }
            });
            pool.add(thread);
        }
        pool.forEach(Thread::start);
        for (Thread thread : pool) {
            thread.join();
        }
        assertTrue(errors.isEmpty(),
                "no thread should fail; first error: " + (errors.peek() == null ? "" : errors.peek()));
    }
}

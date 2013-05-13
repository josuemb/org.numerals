package org.cardinalnumbers.rules.es

import org.junit.*;
import org.numerals.rules.es.CardinalRules;

import static org.junit.Assert.*

class CardinalRulesTest {
	@Test
	public void testRules() {
		def rules = CardinalRules.rules
		assertNotNull("Rules cannot be null",rules)
		assertTrue("Rules should be a Map",rules instanceof Map)
		assertTrue("Rules cannot be empty",rules.size() > 0)
        rules.each { position, cardinals ->
            assertNotNull("Cardinals for position $position cannot be null",cardinals)
            assertTrue("Cardinals for position $position should be a Map",cardinals instanceof Map)
            cardinals.each { digit, cardinal ->
                assertNotNull("Cardinal for position $position, $digit cannot be null",cardinal)
                assertTrue(cardinal instanceof String || cardinal instanceof Closure)
            }
        }
	}
}

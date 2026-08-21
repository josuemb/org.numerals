package org.cardinalnumbers.rules.es

import org.junit.jupiter.api.Test
import org.numerals.rules.es.CardinalRules;

import static org.junit.jupiter.api.Assertions.*

class CardinalRulesTest {
	@Test
	public void testRules() {
		def rules = CardinalRules.rules
		assertNotNull(rules, "Rules cannot be null")
		assertTrue(rules instanceof Map, "Rules should be a Map")
		assertTrue(rules.size() > 0, "Rules cannot be empty")
        rules.each { position, cardinals ->
            assertNotNull(cardinals, "Cardinals for position $position cannot be null")
            assertTrue(cardinals instanceof Map, "Cardinals for position $position should be a Map")
            cardinals.each { digit, cardinal ->
                assertNotNull(cardinal, "Cardinal for position $position, $digit cannot be null")
                assertTrue(cardinal instanceof String || cardinal instanceof Closure)
            }
        }
	}
}

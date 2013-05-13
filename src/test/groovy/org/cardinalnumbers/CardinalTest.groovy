package org.cardinalnumbers

import org.junit.*;
import org.numerals.Cardinal;
import org.numerals.Number;

import static org.junit.Assert.*

class CardinalTest {
	@Test
	public void testGetRuleFromEmptyRules() {
        Cardinal cardinal = new Cardinal()
        boolean testOK = false
        try {
            cardinal.getRule(1,1)
        } catch(RuntimeException e) {
            testOK = true
        }
        assertTrue("Error testing method Cardinal.getRule(position,digit) with empty rules",testOK)
	}
    @Test
    public void testGetRuleFromInvalidPositionRules() {
        Cardinal cardinal = new Cardinal(rules:[1:[1:"uno"]])
        boolean testOK = false
        try {
            cardinal.getRule(10,1)
        } catch(RuntimeException e) {
            testOK = true
        }
        assertTrue("Error testing method Cardinal.getRule(position,digit) with invalid position",testOK)        
    }
    @Test
    public void testGetRuleFromInvalidDigitRules() {
        Cardinal cardinal = new Cardinal(rules:[1:[1:"uno"]])
        boolean testOK = false
        try {
            cardinal.getRule(1,2)
        } catch(RuntimeException e) {
            testOK = true
        }
        assertTrue("Error testing method Cardinal.getRule(position,digit) with invalid digit",testOK)        
    }
    @Test
    public void testGetRuleOK() {
        Cardinal cardinal = new Cardinal(rules:[1:[1:"uno"]])
        boolean testOK = false
        def rule = cardinal.getRule(1,1)
        assertEquals("Error testing method Cardinal.getRule(position,digit) with valid position and digit",rule,"uno")
    }
    @Test
    public void testGetCardinalString() {
        Cardinal cardinal = new Cardinal(rules:[1:[1:"uno"]])
        Number number = new Number("1")        
        boolean testOK = false
        def cardinalString = cardinal.getCardinal(number)
        assertEquals("Error testing method Cardinal.getCardinal(number) with StringRule",cardinalString,"uno")
    }
    @Test
    public void testGetCardinalClosureSimple() {
        Cardinal cardinal = new Cardinal(rules:[1:[1:{"uno"}]])
        Number number = new Number("1")        
        boolean testOK = false
        def cardinalString = cardinal.getCardinal(number)
        assertEquals("Error testing method Cardinal.getCardinal(number) with simple Closure",cardinalString,"uno")
    }
    @Test
    public void testGetCardinalClosureComplex() {
        Cardinal cardinal = new Cardinal(rules:[1:[1:{1+2+3}]])
        Number number = new Number("1")        
        boolean testOK = false
        def cardinalString = cardinal.getCardinal(number)
        assertEquals("Error testing method Cardinal.getCardinal(number) with complex Closure",cardinalString,"6")
    }
    @Test
    public void testGetCardinalClosureOneParameter() {
        Cardinal cardinal = new Cardinal(rules:[1:[1:{number->number[0]+1}]])
        Number number = new Number("1")        
        boolean testOK = false
        def cardinalString = cardinal.getCardinal(number)
        assertEquals("Error testing method Cardinal.getCardinal(number) with complex Closure",cardinalString,"2")
    }
    @Test
    public void testGetCardinalClosureRecursive() {
        Cardinal cardinal = new Cardinal(rules:[1:[3:"tres"],2:[2:{number,getCardinal->"veinti${getCardinal(number[-1..-1])}"}]])
        Number number = new Number("23")        
        boolean testOK = false
        def cardinalString = cardinal.getCardinal(number)
        assertEquals("Error testing method Cardinal.getCardinal(number) with recursive Closure",cardinalString,"veintitres")
    }
}

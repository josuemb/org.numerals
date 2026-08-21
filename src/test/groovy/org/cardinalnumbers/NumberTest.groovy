package org.cardinalnumbers

import org.junit.jupiter.api.Test
import org.numerals.Number;

import static org.junit.jupiter.api.Assertions.*

class NumberTest {
	@Test
	public void testDefaultConstructor() {
        def number = new Number()
        assertNotNull(number, "Error creating org.numerals.Number")
        assertEquals(number, "", "Error setting default value for a new org.numerals.Number")
	}
    @Test
    public void testSetValue() {
        def number = new Number()
        def val = "123"
        number.value = val
        assertEquals(number.value, val, "Error setting new value for a new org.numerals.Number")
    }
    @Test
    public void testInvalidNumbers() {
        def numbers = [null,""," ", "1 ", "A", "1A", ".", "1.", "-1", "1 2"]
        def failed = []
        Number number = null
        boolean testOk = false
        numbers.each { numberString ->
            testOk = false
            try {        
                number = new Number(numberString)
            } catch(NumberFormatException e) {
                testOk = true
            }
            if(testOk == false) {
               failed << numberString 
            }
        }
        assertTrue(testOk, "Error testing invalid numbers $failed")
    }
    @Test
    public void testValidNumbers() {
        int i1 = 1
        Integer i2 = new Integer("2")
        BigInteger b1 = new BigInteger("3")
        def numbers = [0,"0","00",1,3,"01233","1231233",i1,i2,b1]
        def failed = []
        Number number = null
        boolean testOk = false
        numbers.each { numberString ->
            testOk = true
            try {        
                number = new Number(numberString)
            } catch(NumberFormatException e) {
                testOk = false
            }
            if(testOk == false) {
               failed << numberString 
            }
        }
        assertTrue(testOk, "Error testing valid numbers $failed")
    }
    @Test
    public void testSizeProperty() {
        def val = "123"
        def number = new Number(val)
        assertEquals(val.size(), number.size(), "Error testing Number.size() property")
    }
    @Test
    public void testGetAtInteger() {
        def number = new Number("123")
        assertEquals(number[1], 2, "Error testing Number.getAt(int) property")
    }
    @Test
    public void testGetAtRange() {
        def number = new Number("1234")
        assertEquals(number[1..2], new Number("23"), "Error testing Number.getAt(Range) property")
    }
    @Test
    public void testEquals() {
        def values = [new Number("1234"),1234,"1234",new BigInteger("1234")]
        def number = new Number("1234")
        values.each { value ->
            assertTrue(number==value, "Error testing Number.equals() property with values: $number == $value")
        }
    }
    @Test
    public void testHashCode() {
        def number1 = new Number("123")
        def number2 = new Number("345")
        assertNotSame(number1.hashCode(), number2.hashCode(), "Error testing Number.hashCode() method")
    }
    @Test
    public void testEach() {
        def number = new Number("123")
        def arrNumber = []
        number.each{arrNumber<<it}
        assertTrue(arrNumber.size() > 0, "Error testing Number.each() method")
    }
}

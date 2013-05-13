package org.cardinalnumbers

import org.junit.Test
import org.numerals.Number;

import static org.junit.Assert.*

class NumberTest {
	@Test
	public void testDefaultConstructor() {
        def number = new Number()
        assertNotNull("Error creating org.numerals.Number",number)
        assertEquals("Error setting default value for a new org.numerals.Number",number,"")
	}
    @Test
    public void testSetValue() {
        def number = new Number()
        def val = "123"
        number.value = val
        assertEquals("Error setting new value for a new org.numerals.Number",number.value,val)
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
        assertTrue("Error testing invalid numbers $failed",testOk)
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
        assertTrue("Error testing valid numbers $failed",testOk)
    }
    @Test
    public void testSizeProperty() {
        def val = "123"
        def number = new Number(val)
        assertEquals("Error testing Number.size() property",val.size(),number.size())
    }
    @Test
    public void testGetAtInteger() {
        def number = new Number("123")
        assertEquals("Error testing Number.getAt(int) property",number[1],2)
    }
    @Test
    public void testGetAtRange() {
        def number = new Number("1234")
        assertEquals("Error testing Number.getAt(Range) property",number[1..2],new Number("23"))
    }
    @Test
    public void testEquals() {
        def values = [new Number("1234"),1234,"1234",new BigInteger("1234")]
        def number = new Number("1234")
        values.each { value ->
            assertTrue("Error testing Number.equals() property with values: $number == $value",number==value)
        }
    }
    @Test
    public void testHashCode() {
        def number1 = new Number("123")
        def number2 = new Number("345")
        assertNotSame("Error testing Number.hashCode() method",number1.hashCode(),number2.hashCode())
    }
    @Test
    public void testEach() {
        def number = new Number("123")
        def arrNumber = []
        number.each{arrNumber<<it}
        assertTrue("Error testing Number.each() method",arrNumber.size() > 0)
    }
}

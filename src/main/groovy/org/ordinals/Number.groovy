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

package org.ordinals

/**
 * Custom Number class for get the ordinals.<br/>
 * 
 * @author Josue Mart&iacute;nez Buenrrostro (@josuemb)
 */
class Number {
	private final static ZERO = "0"
	private final static REGEX_VALID_NUMBER = /(0*)(\d*)/
	private String value

	Number(){
		this.value = ""
	}

	Number(Object value) {
		setValue value
	}

	String getValue() {
		return this.value
	}

	void setValue(Object value) {
		value = value.toString()
		value = validateNumber(value)
		this.value = value
	}

	private String validateNumber(String number){
		def validatedNumber = ""
		if(!number) {
			throw new NumberFormatException("The number: [$number] cannot be null either empty")
		}
		def matcher = (number =~ REGEX_VALID_NUMBER)
		if(matcher.matches() == false) {
			throw new NumberFormatException("The number: [$number] is invalid. Only digits are valid.")
		}
		def leftZeros = matcher[0][1]
		def otherDigits = matcher[0][2]
		if(leftZeros && !otherDigits){
			validatedNumber = ZERO
		} else {
			validatedNumber = otherDigits
		}
	}

	int size() {
		return value.size()
	}

	Object getAt(int idx) {
		return value.getAt(idx).toInteger()
	}

	Object getAt(Range range){
		return new Number(value.getAt(range))
	}

	boolean equals(Object obj) {
		return this.value.equals(obj.toString())
	}

	int hashCode() {
		value.hashCode()
	}

	String toString() {
		return value
	}

	Object each(Closure closure) {
		return value.each(closure)
	}
}

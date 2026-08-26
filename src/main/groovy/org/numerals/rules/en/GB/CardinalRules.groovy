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

package org.numerals.rules.en.GB

import java.util.List;

import org.numerals.Number

/**
 * Class containing rules to get cardinals of a given number
 * in British English (en_GB).<br/><br/>
 *
 * This is a locale variant of the base English (en) rules. British English is
 * identical to the base English rules except that the word "and" is inserted
 * before the final group of a number when that remaining group is less than one
 * hundred, for example: "one hundred and twenty-three", "one thousand and five",
 * "one million and one". The base English (en) rules omit this connector.
 *
 * The static property "rules" get the rules to get cardinals.
 *
 * @author Josue Mart&iacute;nez Buenrrostro (@josuemb)
 */
class CardinalRules {

	private static final SEPARATOR_RULES = "/"
	private static final SEPARATOR_COMMON = " "
	private static final SEPARATOR_TENS = "-"
	private static final SEPARATOR_HUNDRED = " "
	// British English connector inserted before a trailing group below 100.
	private static final SEPARATOR_AND = " and "
	// A group with fewer than this many digits is below one hundred, so the
	// "and" connector applies before it (units or tens remainder).
	private static final BELOW_HUNDRED_DIGITS = 3
	private static final POSITION_MIN = 1
	private static final POSITION_MAX = 24
	private static final DIGIT_MIN = 0
	private static final DIGIT_MAX = 9

	private static final Map units = [0:"zero", 1:"one", 2:"two",3:"three",4:"four",5:"five",6:"six",7:"seven",8:"eight",9:"nine"]
	private static final Map tens = [1:"ten", 2:"twenty",3:"thirty",4:"forty",5:"fifty",6:"sixty",7:"seventy",8:"eighty",9:"ninety"]
	private static final Map specialTens = [1:"teen"]
	private static final Map specialTensOne = [1:"eleven",2:"twelve",3:"thirteen",5:"fifteen",8:"eighteen"]

	static final Map rules = [:]

	// Sets the rules for each position.
	static {
		(POSITION_MIN..POSITION_MAX).each{ position ->
			switch( position ) {
				case 1: rules[position] = getRulesUnits()
					break
				case 2: rules[position] = getRulesTens()
					break
				default: rules[position] = getRulesCommon()
			}
		}
	}

	// Suffixes for each range of positions.
	private static final List suffixes = [
		[range:3..3,suffix:"hundred"],
		[range:4..6,suffix:"thousand"],
		[range:7..9,suffix:"million"],
		[range:10..12,suffix:"billion"],
		[range:13..15,suffix:"trillion"],
		[range:16..18,suffix:"quadrillion"],
		[range:19..21,suffix:"quintillion"],
		[range:22..24,suffix:"sextillion"]
	]


	/**
	 * Looks up the suffix for a given range of positions.
	 * @param position Position.
	 * @return Map containing the range of each suffix and the suffix itself.
	 */
	private static Map getSuffix(int position){
		Map suffixesTmp = suffixes.find{it.range.isCase(position)}
		if(!suffixesTmp) {
			throw new MissingResourceException("Cannot found class suffix for position:$position", CardinalRules.class.name, "getSuffix")
		}
		return suffixesTmp
	}

	/**
	 * Gets the rules to build the cardinals for the units (special case).
	 * @return Map with the rules to determine the numbers.
	 */
	private static Map getRulesUnits(){
		Map rulesUnits = [:]
		(DIGIT_MIN..DIGIT_MAX).each { digit ->
			rulesUnits[digit] = { number ->
				digit == 0 && number.size()!=1?"":units[digit]
			}
		}
		return rulesUnits
	}

	/**
	 * Gets the cardinal for the tens.
	 * @param number Number (tens only) to get the cardinal for.
	 * @param getCardinal Closure injected automatically to recursively call the closure that determines the cardinals.
	 * @param units Units.
	 * @return String with the cardinal.
	 * @see groovy.lang.Closure
	 */
	private static String getCardinalTens ( Number number, Closure getCardinal, int units ) {
		StringBuilder cardinal = new StringBuilder()
		cardinal << tens[units]
		if( number[-1] != 0 ) {
			cardinal << SEPARATOR_TENS
			cardinal << getCardinal(number[-1..-1])
		}
		return cardinal.toString()
	}

	/**
	 * Gets the rules to build the cardinals for the tens (special case).
	 * @return Map with the rules to determine the numbers.
	 */
	private static Map getRulesTens(){
		Map rulesTens = [:]
		(DIGIT_MIN..DIGIT_MAX).each { digit ->
			switch ( digit ) {
				case 1: rulesTens[digit] = { number, getCardinal ->
					String cardinal
					// Checks the value of the units.
					switch (number[-1]) {
						case 0: cardinal = tens[digit]
							break
						case [1, 2, 3, 5, 8]: cardinal = specialTensOne[number[-1]]
							break
						default:
							cardinal = "${getCardinal(number[-1..-1])}${specialTens[digit]}"
					}
					return cardinal
				}
					break
				default: rulesTens[digit] = { number, getCardinal ->
					getCardinalTens(number, getCardinal, digit)
				}
			}
		}
		return rulesTens
	}

	/**
	 * Gets the rules to build the cardinals for the common cases (from thousands onwards).
	 * British English inserts "and" before the trailing group when that group
	 * is below one hundred (for example "one hundred and five", "one thousand
	 * and twenty-three", "one million and one"). When the trailing group is one
	 * hundred or greater no connector is used at this level; the "and" then
	 * appears deeper in the recursion, inside that group.
	 * @param number Number to get the cardinal for.
	 * @param getCardinal Closure injected automatically to recursively call the closure that determines the cardinals.
	 * @return String with the cardinal.
	 * @see groovy.lang.Closure
	 */
	private static String getCardinalCommon ( Number number, Closure getCardinal ) {
		StringBuilder cardinal = new StringBuilder()

		def pos = number.size()
		def suffix = getSuffix(pos)

		def group = number[-(pos)..-(suffix.range.from)]
		def remainder = number[-(suffix.range.from-1)..-1]

		cardinal << getCardinal(group)
		cardinal << SEPARATOR_COMMON
		cardinal << suffix.suffix

		if( remainder != 0 ){
			// British English: "and" before a remainder below one hundred,
			// otherwise a plain space and the remainder recurses on its own.
			if( remainder.size() < BELOW_HUNDRED_DIGITS ) {
				cardinal << SEPARATOR_AND
			} else {
				cardinal << SEPARATOR_COMMON
			}
			cardinal << getCardinal(remainder)
		}

		return cardinal.toString()
	}

	/**
	 * Gets the rules to build the cardinals for most of the numbers.
	 * @return Map with the rules to determine the numbers.
	 */
	private static Map getRulesCommon() {
		Map rulesTmp = [:]
		(DIGIT_MIN..DIGIT_MAX).each { digit ->
			switch ( digit ) {
				default:
					rulesTmp[digit] = { number, getCardinal ->
						getCardinalCommon(number, getCardinal)
					}
			}
		}
		return rulesTmp
	}
}

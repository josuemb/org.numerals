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
 * Get the cardinals using given rules.<br/>
 * 
 * @author Josue Mart&iacute;nez Buenrrostro (@josuemb)
 */
class Cardinal {
	Map rules = [:]

	String getCardinal(Number number){
		String cardinal = ""
		def digits = number.size()
		def rule = getRule(digits, number[-digits])
		if(rule instanceof Closure) {
			if(rule.maximumNumberOfParameters > 1) {
				cardinal = rule.call(number, this.&getCardinal)
			} else {
				cardinal = rule.call(number)
			}
		} else {
			cardinal = rule
		}
	}

	private Object getRule(Object position, Object digit){
		def rulesByPos = rules.get(position)
		if(!rulesByPos){
			throw new RuntimeException("Config Error: Rules to calculate cardinal at position [$position] has not been given")
		}
		if(!(rulesByPos instanceof Map)) {
			throw new RuntimeException("Config Error: Rules to calculate cardinal at position [$position] is not a Map")
		}
		def rule = rulesByPos.get(digit)
		if(!rule){
			throw new RuntimeException("Config Error: Rules to calculate cardinal at position [$position], digit [$digit] has not been given")
		}
		if(!(rule instanceof String || rule instanceof Closure)) {
			throw new RuntimeException("Config Error: Rules to calculate cardinal at position [$position], digit [$digit] is invalid, only String and Closure are valid rules")
		}
		return rule
	}
}

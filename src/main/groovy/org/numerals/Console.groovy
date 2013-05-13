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

package org.numerals

/**
 * Class for using cardinals from command line.<br/>
 * 
 * @author Josue Mart&iacute;nez Buenrrostro (@josuemb)
 */
class Console {
	static void main(args) {
		String cardinal
		args.each { number ->
			try {
				cardinal = CardinalUtil.getCardinal(number)
				println "[$number]=[$cardinal]"
			} catch(Exception e) {
				println "[$number]=[Error:${e.message}]"
			}
		}
	}
}


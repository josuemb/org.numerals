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
 * Utilities for easy get cardinal.<br/>
 * 
 * @author Josue Mart&iacute;nez Buenrrostro (@josuemb)
 */
class CardinalUtil {
	private final static RULES_PACKAGE_NAME = "org.ordinals.rules"
	private final static RULES_CLASS_NAME = "CardinalRules"
	private final static RULES_METHOD_NAME = "getRules"

	static String getCardinal(Object value){
		Locale locale = Locale.getDefault()
		String cardinal = getCardinal(value,locale)
		return cardinal
	}

	static String getCardinal(Object value, Locale locale) {
		Class rulesClass = getRulesClass(locale)
		String cardinal = getCardinal(value,rulesClass)
		return cardinal
	}

	static String getCardinal(Object value, Class rulesClass) {
		String cardinal = ""
		Number number = new Number(value)
		Map rules = getRules(rulesClass)
		cardinal = getCardinal(number, rules)
		return cardinal
	}

	static String getCardinal(Number number, Map rules) {
		String cardinal = ""
		Cardinal calc = new Cardinal(rules:rules)
		cardinal = calc.getCardinal(number)
		return cardinal
	}

	private static Class getRulesClass(Locale locale) {
		List searchList = []
		String path = "$locale.language${locale.country?'.':''}$locale.country${locale.variant?'.':''}$locale.variant"
		String clazzName = ""
		Class clazz = null
		boolean blnSearchPending = true
		while(clazz == null && blnSearchPending) {
			clazzName = "$RULES_PACKAGE_NAME.$path.$RULES_CLASS_NAME"
			searchList << clazzName
			try {
				clazz = Class.forName(clazzName)
			} catch(ClassNotFoundException e) {
				if(path.lastIndexOf('.') == -1) {
					blnSearchPending = false
				}else {
					path = path.substring(0,path.lastIndexOf('.'))
				}
			}
		}
		if(clazz == null) {
			throw new MissingResourceException("Cannot found class $RULES_CLASS_NAME in paths $searchList", CardinalUtil.class.name, "getRulesClass")
		}
		return clazz
	}

	private static Map getRules(Class rulesClass) {
		def rules = [:]
		def method
		try {
			method = rulesClass.getMethod(RULES_METHOD_NAME)
		} catch (NoSuchMethodException e) {
			throw new MissingResourceException("Cannot found a [$RULES_METHOD_NAME] method of class $rulesClass.name", rulesClass.name, RULES_METHOD_NAME)
		}
		if(!method){
			throw new MissingResourceException("Cannot found a [$RULES_METHOD_NAME] method of class $rulesClass.name", rulesClass.name, RULES_METHOD_NAME)
		}
		if(!(method.returnType == Map)) {
			throw new ClassCastException("The [$RULES_METHOD_NAME] property should return a [$java.util.Map.name] type")
		}
		rules = rulesClass."$RULES_METHOD_NAME"()
		return rules
	}
}

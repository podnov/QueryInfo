package com.evanzeimet.queryinfo.jpa.predicate;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 * #%L
 * queryinfo-jpa
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 Evan Zeimet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * It appears that hibernate parses at least numeric types out of the box:
 *
 * <pre>
 * http://grepcode.com/file/repo1.maven.org/maven2/org.hibernate/hibernate-entitymanager/4.3.10.Final/org/hibernate/jpa/criteria/predicate/ComparisonPredicate.java#ComparisonPredicate.%3Cinit%3E%28org.hibernate.jpa.criteria.CriteriaBuilderImpl%2Corg.hibernate.jpa.criteria.predicate.ComparisonPredicate.ComparisonOperator%2Cjavax.persistence.criteria.Expression%2Cjava.lang.Object%29
 * </pre>
 *
 * Let's parse everything else.
 */
import javax.persistence.criteria.Expression;

import com.evanzeimet.queryinfo.QueryInfoException;
import com.evanzeimet.queryinfo.QueryInfoUtils;
import com.evanzeimet.queryinfo.condition.ConditionOperator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class FieldValueParser {

	private ObjectMapper objectMapper;

	public FieldValueParser() {
		objectMapper = new QueryInfoUtils().createObjectMapper();
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public Object parse(Expression<?> path,
			ConditionOperator conditionOperator,
			String fieldValue) throws QueryInfoException {
		Object result;
		Class<?> javaType = path.getJavaType();

		boolean isEitherInOperator = ConditionOperator.isEitherInOperator(conditionOperator);

		if (isEitherInOperator) {
			result = parseIn(path, fieldValue);
		} else if (Boolean.class.isAssignableFrom(javaType)) {
			result = parseBoolean(fieldValue);
		} else if (Date.class.isAssignableFrom(javaType)) {
			result = parseDate(fieldValue);
		} else {
			result = fieldValue;
		}

		return result;
	}

	protected Boolean parseBoolean(String fieldValue) {
		return Boolean.valueOf(fieldValue);
	}

	protected Date parseDate(String fieldValue) throws QueryInfoException {
		Date result;

		try {
			DateFormat dateFormat = objectMapper.getDateFormat();
			result = dateFormat.parse(fieldValue);
		} catch (ParseException e) {
			throw new QueryInfoException(e);
		}

		return result;
	}

	protected <T> List<T> parseIn(Expression<?> path,
			String fieldValue) throws QueryInfoException {
		Class<?> javaType = path.getJavaType();

		TypeFactory typeFactory = objectMapper.getTypeFactory();
		CollectionType collectionType = typeFactory.constructCollectionType(ArrayList.class,
				javaType);

		List<T> result;

		try {
			result = objectMapper.readValue(fieldValue, collectionType);
		} catch (IOException e) {
			String message = String.format("Could not parse [%s] as list of [%s]", fieldValue, javaType);
			throw new QueryInfoException(message, e);
		}

		return result;
	}
}

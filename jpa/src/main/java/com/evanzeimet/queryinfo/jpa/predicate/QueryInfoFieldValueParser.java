package com.evanzeimet.queryinfo.jpa.predicate;

import static com.evanzeimet.queryinfo.condition.ConditionOperator.isEitherInOperator;
import static com.evanzeimet.queryinfo.condition.ConditionOperator.isEitherNullOperator;

/*
 * #%L
 * queryinfo-jpa
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 Evan Zeimet
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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.Expression;

import com.evanzeimet.queryinfo.QueryInfoException;
import com.evanzeimet.queryinfo.QueryInfoUtils;
import com.evanzeimet.queryinfo.condition.ConditionOperator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * It appears that hibernate parses at least numeric types out of the box:
 *
 * <pre>
 * http://grepcode.com/file/repo1.maven.org/maven2/org.hibernate/hibernate-entitymanager/4.3.10.Final/org/hibernate/jpa/criteria/predicate/ComparisonPredicate.java#ComparisonPredicate.%3Cinit%3E%28org.hibernate.jpa.criteria.CriteriaBuilderImpl%2Corg.hibernate.jpa.criteria.predicate.ComparisonPredicate.ComparisonOperator%2Cjavax.persistence.criteria.Expression%2Cjava.lang.Object%29
 * </pre>
 *
 * Let's parse everything else.
 */
public class QueryInfoFieldValueParser {

	private ObjectMapper objectMapper;

	public QueryInfoFieldValueParser() {
		objectMapper = new QueryInfoUtils().createObjectMapper();
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public Object parseLiteral(JsonNode fieldValue) throws QueryInfoException {
		return parseType(fieldValue, Object.class);
	}

	public Object parseLiteralForExpression(Expression<?> expression,
			ConditionOperator conditionOperator,
			JsonNode fieldValue) throws QueryInfoException {
		Object result;
		Class<?> javaType = expression.getJavaType();

		if (isEitherInOperator(conditionOperator)) {
			result = parseInForExpression(expression, fieldValue);
		} else if (isEitherNullOperator(conditionOperator)) {
			result = null;
		} else if (Date.class.isAssignableFrom(javaType)) {
			result = parseDate(fieldValue);
		} else {
			result = parseType(fieldValue, javaType);
		}

		return result;
	}

	protected Date parseDate(JsonNode fieldValue) throws QueryInfoException {
		Date result;
		String stringValue = parseType(fieldValue, String.class);

		try {
			DateFormat dateFormat = objectMapper.getDateFormat();
			result = dateFormat.parse(stringValue);
		} catch (ParseException e) {
			throw new QueryInfoException(e);
		}

		return result;
	}

	protected <T> List<T> parseInForExpression(Expression<?> path,
			JsonNode fieldValue) throws QueryInfoException {
		Class<?> javaType = path.getJavaType();

		TypeFactory typeFactory = objectMapper.getTypeFactory();
		CollectionType collectionType = typeFactory.constructCollectionType(List.class, javaType);

		return objectMapper.convertValue(fieldValue, collectionType);
	}

	protected <T> T parseType(JsonNode fieldValue, Class<T> javaType) throws QueryInfoException {
		T result;

		try {
			result = objectMapper.convertValue(fieldValue, javaType);
		} catch (IllegalArgumentException e) {
			String message = String.format("Could not parse [%s] as [%s]",
					fieldValue,
					javaType);
			throw new QueryInfoException(message, e);
		}

		return result;
	}
}

package com.evanzeimet.queryinfo.jpa.field;

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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.evanzeimet.queryinfo.jpa.join.QueryInfoJoinType;

@Retention(RUNTIME)
@Target(METHOD)
public @interface QueryInfoField {

	/**
	 * This is used in specific use-cases when you have a joined entity whose
	 * class cannot be easily determined at run-time.
	 */
	public Class<?> entityClass() default QueryInfoField.class;

	public boolean isOrderable() default true;

	public boolean isPredicateable() default true;

	public boolean isSelectable() default true;

	public QueryInfoJoinType joinType() default QueryInfoJoinType.UNSPECIFIED;

	public String name() default "";
}

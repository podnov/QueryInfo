package com.evanzeimet.queryinfo.jpa.field;

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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.evanzeimet.queryinfo.jpa.entity.TestOrganizationEntity_;
import com.evanzeimet.queryinfo.jpa.entity.TestPersonEntity_;

public class QueryInfoFieldNameBuilderTest {

	@Test
	public void build() {
		String actual = QueryInfoFieldNameBuilder.create(TestOrganizationEntity_.employees)
				.add(TestPersonEntity_.spouse)
				.add(TestPersonEntity_.firstName)
				.build();
		String expected = "employees.spouse.firstName";

		assertEquals(expected, actual);
	}

}
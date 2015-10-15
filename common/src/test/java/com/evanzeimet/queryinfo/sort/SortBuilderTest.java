package com.evanzeimet.queryinfo.sort;

/*
 * #%L
 * queryinfo-common
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

import org.junit.Before;
import org.junit.Test;

import com.evanzeimet.queryinfo.sort.Sort;
import com.evanzeimet.queryinfo.sort.SortBuilder;
import com.evanzeimet.queryinfo.sort.SortDirection;

import static org.junit.Assert.*;

public class SortBuilderTest {

	private SortBuilder builder;

	@Before
	public void setUp() {
		builder = new SortBuilder();
	}

	@Test
	public void build() {
		String givenFieldName = "my field name";
		SortDirection givenDirection = SortDirection.ASC;

		Sort actualSort = builder.direction(givenDirection)
				.fieldName(givenFieldName)
				.build();

		String actualFieldName = actualSort.getFieldName();
		assertEquals(givenFieldName, actualFieldName);

		String actualDirection = actualSort.getDirection();
		String expectedDirection = givenDirection.getText();
		assertEquals(expectedDirection, actualDirection);
	}
}

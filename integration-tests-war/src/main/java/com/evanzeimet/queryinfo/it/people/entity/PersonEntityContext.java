package com.evanzeimet.queryinfo.it.people.entity;

/*
 * #%L
 * queryinfo-integration-tests-war
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

import javax.ejb.Stateless;

import com.evanzeimet.queryinfo.it.people.PersonEntity;
import com.evanzeimet.queryinfo.jpa.entity.AbstractQueryInfoEntityContext;
import com.evanzeimet.queryinfo.jpa.entity.QueryInfoEntityContext;

@Stateless
public class PersonEntityContext
	extends AbstractQueryInfoEntityContext<PersonEntity>
	implements QueryInfoEntityContext<PersonEntity> {

	@Override
	public Class<PersonEntity> getEntityClass() {
		return PersonEntity.class;
	}

}

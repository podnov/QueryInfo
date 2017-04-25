package com.evanzeimet.queryinfo.jpa.entity;

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

import javax.persistence.criteria.From;
import com.evanzeimet.queryinfo.jpa.jpacontext.QueryInfoJPAContext;

public interface QueryInfoEntityContextRegistry {

	<Entity> QueryInfoEntityContext<Entity> getContext(From<?, Entity> from);

	<Entity> QueryInfoEntityContext<Entity> getContext(Class<Entity> entityClass);

	<Entity> QueryInfoEntityContext<Entity> getContextForRoot(QueryInfoJPAContext<Entity, ?> jpaContext);

	List<QueryInfoEntityContext<?>> getContexts();

}

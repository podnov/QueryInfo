package com.evanzeimet.queryinfo.jpa.join;

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

import javax.persistence.criteria.JoinType;

public enum QueryInfoJoinType {

	INNER,
	LEFT,
	RIGHT,
	UNSPECIFIED;

	QueryInfoJoinType() {

	}

	public static boolean isUnspecified(QueryInfoJoinType joinType) {
		boolean result = UNSPECIFIED.equals(joinType);
		result = (result || (joinType == null));
		return result;
	}

	public JoinType toJpaType() {
		JoinType result;

		if (UNSPECIFIED.equals(this)) {
			/**
			 * Default set per JPA CriteriaQuery join method default.
			 */
			result = JoinType.INNER;
		} else {
			String name = name();
			result = JoinType.valueOf(name);
		}

		return result;
	}

}

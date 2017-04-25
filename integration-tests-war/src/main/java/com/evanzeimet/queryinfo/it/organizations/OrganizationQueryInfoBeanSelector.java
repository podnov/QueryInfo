package com.evanzeimet.queryinfo.it.organizations;

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

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.evanzeimet.queryinfo.QueryInfo;
import com.evanzeimet.queryinfo.QueryInfoException;
import com.evanzeimet.queryinfo.QueryInfoUtils;
import com.evanzeimet.queryinfo.it.organizations.entity.OrganizationEntityQueryInfoBean;
import com.evanzeimet.queryinfo.it.organizations.tuple.OrganizationTupleQueryInfoBean;

@Stateless
public class OrganizationQueryInfoBeanSelector {

	@Inject
	private OrganizationEntityQueryInfoBean entityQueryInfoBean;

	@Inject
	private OrganizationTupleQueryInfoBean tupleQueryInfoBean;

	private QueryInfoUtils utils;

	@PostConstruct
	protected void postConstruct() {
		utils = new QueryInfoUtils();
	}

	@SuppressWarnings("unchecked")
	public <T> T query(QueryInfo queryInfo) throws QueryInfoException {
		T result;
		boolean useEntityBean = utils.hasRequestedAllFields(queryInfo);

		if (useEntityBean) {
			result = (T) entityQueryInfoBean.query(queryInfo);
		} else {
			result = (T) tupleQueryInfoBean.query(queryInfo);
		}

		return result;
	}

}

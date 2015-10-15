package com.evanzeimet.queryinfo;

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

import java.io.Serializable;
import java.util.List;

import com.evanzeimet.queryinfo.condition.ConditionGroup;
import com.evanzeimet.queryinfo.pagination.PaginationInfo;
import com.evanzeimet.queryinfo.sort.Sort;

public interface QueryInfo extends Serializable {

	ConditionGroup getConditionGroup();

	void setConditionGroup(ConditionGroup conditionGroup);

	PaginationInfo getPaginationInfo();

	void setPaginationInfo(PaginationInfo paginationInfo);

	List<String> getRequestedFields();

	void setRequestedFields(List<String> requestedFields);

	List<Sort> getSorts();

	void setSorts(List<Sort> sorts);
}

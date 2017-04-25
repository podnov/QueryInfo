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

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.evanzeimet.queryinfo.condition.Condition;
import com.evanzeimet.queryinfo.condition.ConditionGroup;
import com.evanzeimet.queryinfo.condition.DefaultConditionGroup;
import com.evanzeimet.queryinfo.pagination.DefaultPaginationInfo;
import com.evanzeimet.queryinfo.pagination.PaginationInfo;
import com.evanzeimet.queryinfo.selection.Selection;
import com.evanzeimet.queryinfo.selection.SelectionBuilder;
import com.evanzeimet.queryinfo.sort.Sort;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QueryInfoUtils {

	private Integer defaultPageSize = 10;

	public QueryInfoUtils() {

	}

	public Integer getDefaultPageSize() {
		return defaultPageSize;
	}

	public void setDefaultPageSize(Integer defaultPageSize) {
		this.defaultPageSize = defaultPageSize;
	}

	public <T> T coalesce(T given, T defaultValue) {
		T result;

		if (given == null) {
			result = defaultValue;
		} else {
			result = given;
		}

		return result;
	}

	public void coalesceConditionGroup(QueryInfo result) {
		ConditionGroup conditionGroup = result.getConditionGroup();
		conditionGroup = coalesceConditionGroup(conditionGroup);
		result.setConditionGroup(conditionGroup);
	}

	public ConditionGroup coalesceConditionGroup(ConditionGroup conditionGroup) {
		ConditionGroup result;

		if (conditionGroup == null) {
			result = new DefaultConditionGroup();
		} else {
			result = conditionGroup;
		}

		List<ConditionGroup> conditionGroups = result.getConditionGroups();

		if (conditionGroups == null) {
			conditionGroups = new ArrayList<>();
			result.setConditionGroups(conditionGroups);
		}

		int conditionGroupsCount = conditionGroups.size();

		for (int conditionGroupIndex = 0; conditionGroupIndex < conditionGroupsCount; conditionGroupIndex++) {
			conditionGroup = conditionGroups.get(conditionGroupIndex);
			conditionGroup = coalesceConditionGroup(conditionGroup);
			conditionGroups.set(conditionGroupIndex, conditionGroup);
		}

		List<Condition> conditions = result.getConditions();

		if (conditions == null) {
			conditions = new ArrayList<>();
			result.setConditions(conditions);
		}

		return result;
	}

	public List<String> coalesceGroupByAttributePaths(QueryInfo queryInfo) {
		List<String> result = queryInfo.getGroupByAttributePaths();

		result = coalesceGroupByAttributePaths(result);
		queryInfo.setGroupByAttributePaths(result);

		return result;
	}

	public List<String> coalesceGroupByAttributePaths(List<String> groupByAttributePaths) {
		List<String> result;

		if (groupByAttributePaths == null) {
			result = new ArrayList<>();
		} else {
			result = groupByAttributePaths;
		}

		return result;
	}

	public PaginationInfo coalescePaginationInfo(QueryInfo queryInfo) {
		PaginationInfo result = queryInfo.getPaginationInfo();

		result = coalescePaginationInfo(result);
		queryInfo.setPaginationInfo(result);

		return result;
	}

	public PaginationInfo coalescePaginationInfo(PaginationInfo paginationInfo) {
		PaginationInfo result;

		if (paginationInfo == null) {
			result = new DefaultPaginationInfo();
		} else {
			result = paginationInfo;
		}

		Integer pageIndex = result.getPageIndex();

		if (pageIndex == null) {
			result.setPageIndex(0);
		}

		Integer pageSize = result.getPageSize();

		if (pageSize == null) {
			result.setPageSize(defaultPageSize);
		}

		return result;
	}

	public QueryInfo coalesceQueryInfo(QueryInfo queryInfo) {
		QueryInfo result;

		if (queryInfo == null) {
			result = new DefaultQueryInfo();
		} else {
			result = queryInfo;
		}

		coalesceConditionGroup(result);
		coalesceGroupByAttributePaths(result);
		coalescePaginationInfo(result);

		/*
		 * don't coalesce selections, null value means
		 * "give me everything"
		 */

		coalesceSorts(result);

		return result;
	}

	public List<Selection> coalesceSelections(QueryInfo queryInfo) {
		List<Selection> result = queryInfo.getSelections();

		result = coalesceSelections(result);
		queryInfo.setSelections(result);

		return result;
	}

	public List<Selection> coalesceSelections(List<Selection> selections) {
		List<Selection> result;

		if (selections == null) {
			result = new ArrayList<>();
		} else {
			result = selections;
		}

		return result;
	}

	public List<Sort> coalesceSorts(QueryInfo queryInfo) {
		List<Sort> result = queryInfo.getSorts();

		result = coalesceSorts(result);
		queryInfo.setSorts(result);

		return result;
	}

	public List<Sort> coalesceSorts(List<Sort> sorts) {
		List<Sort> result;

		if (sorts == null) {
			result = new ArrayList<>();
		} else {
			result = sorts;
		}

		return result;
	}

	public ObjectMapper createObjectMapper() {
		ObjectMapper result = new ObjectMapper();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		result.setDateFormat(dateFormat);

		result.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		return result;
	}

	public List<Selection> createSelectionsForAttributePaths(List<String> attributePaths) {
		int attributePathCount = attributePaths.size();
		List<Selection> result = new ArrayList<>(attributePathCount);
		SelectionBuilder selectionBuilder = SelectionBuilder.create();

		for (String attributePath : attributePaths) {
			Selection selection = selectionBuilder.attributePath(attributePath)
					.build();
			result.add(selection);
		}

		return result;
	}

	public boolean hasRequestedAllFields(QueryInfo queryInfo) {
		List<Selection> selections = queryInfo.getSelections();
		return (selections == null);
	}

	public <T> T objectify(String json, Class<T> clazz) throws QueryInfoException {
		T result;
		ObjectMapper objectMapper = createObjectMapper();

		try {
			result = objectMapper.readValue(json, clazz);
		} catch (IOException e) {
			throw new QueryInfoException(e);
		}

		return result;
	}

	public <T> T objectify(String json, Type typeOfT) throws QueryInfoException {
		T result;
		ObjectMapper objectMapper = createObjectMapper();

		try {
			JavaType javaType = objectMapper.constructType(typeOfT);
			result = objectMapper.readValue(json, javaType);
		} catch (IOException e) {
			throw new QueryInfoException(e);
		}

		return result;
	}

	public String stringifyJsonNode(JsonNode jsonNode) {
		return stringifyJsonNode(jsonNode, null);
	}

	protected String stringifyJsonNode(JsonNode jsonNode, String defaultValue) {
		String result;

		if (jsonNode == null) {
			result = defaultValue;
		} else {
			result = jsonNode.asText();
		}

		return result;
	}

	public JsonNode treeify(Object object) {
		ObjectMapper objectMapper = createObjectMapper();
		return objectMapper.valueToTree(object);
	}

}

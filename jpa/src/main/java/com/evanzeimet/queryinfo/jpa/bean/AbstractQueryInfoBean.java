package com.evanzeimet.queryinfo.jpa.bean;

import java.util.Iterator;

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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.evanzeimet.queryinfo.QueryInfo;
import com.evanzeimet.queryinfo.QueryInfoException;
import com.evanzeimet.queryinfo.QueryInfoRuntimeException;
import com.evanzeimet.queryinfo.QueryInfoUtils;
import com.evanzeimet.queryinfo.condition.ConditionGroup;
import com.evanzeimet.queryinfo.jpa.entity.QueryInfoEntityContext;
import com.evanzeimet.queryinfo.jpa.entity.QueryInfoEntityContextRegistry;
import com.evanzeimet.queryinfo.jpa.field.QueryInfoJPAAttributePathBuilder;
import com.evanzeimet.queryinfo.jpa.group.QueryInfoGroupByFactory;
import com.evanzeimet.queryinfo.jpa.iterator.AllPaginatedResultsIterator;
import com.evanzeimet.queryinfo.jpa.iterator.PaginatedResultIteratorDirection;
import com.evanzeimet.queryinfo.jpa.jpacontext.QueryInfoJPAContext;
import com.evanzeimet.queryinfo.jpa.jpacontext.QueryInfoJPAContextFactory;
import com.evanzeimet.queryinfo.jpa.jpacontext.QueryInfoJPAContexts;
import com.evanzeimet.queryinfo.jpa.order.QueryInfoOrderFactory;
import com.evanzeimet.queryinfo.jpa.predicate.QueryInfoPredicateFactory;
import com.evanzeimet.queryinfo.jpa.result.QueryInfoResultConverter;
import com.evanzeimet.queryinfo.jpa.selection.QueryInfoSelectionSetter;
import com.evanzeimet.queryinfo.pagination.DefaultPaginatedResult;
import com.evanzeimet.queryinfo.pagination.PaginatedResult;
import com.evanzeimet.queryinfo.pagination.PaginationInfo;

public abstract class AbstractQueryInfoBean<RootEntity, CriteriaQueryResult, QueryInfoResult>
		implements QueryInfoBean<RootEntity, CriteriaQueryResult, QueryInfoResult> {

	protected static final int DEFAULT_PAGE_INDEX = 0;
	protected static final int DEFAULT_MAX_RESULTS = 20;

	private QueryInfoBeanContext<RootEntity, CriteriaQueryResult, QueryInfoResult> beanContext;
	private final QueryInfoUtils queryInfoUtils = new QueryInfoUtils();

	public AbstractQueryInfoBean() {
		super();
	}

	public QueryInfoBeanContext<RootEntity, CriteriaQueryResult, QueryInfoResult> getBeanContext() {
		return beanContext;
	}

	public void setBeanContext(QueryInfoBeanContext<RootEntity, CriteriaQueryResult, QueryInfoResult> beanContext) {
		this.beanContext = beanContext;
	}

	protected EntityManager getEntityManager()  {
		if (beanContext == null) {
			String message = String.format("No bean context set on [%s]", getClass().getCanonicalName());
			throw new QueryInfoRuntimeException(message);
		}

		EntityManager entityManager = beanContext.getEntityManager();

		if (entityManager == null) {
			String message = String.format("No EntityManager provided by bean context [%s]", beanContext.getClass().getCanonicalName());
			throw new QueryInfoRuntimeException(message);
		}

		return entityManager;
	}

	protected QueryInfo coalesceQueryInfo(QueryInfo queryInfo) {
		return queryInfoUtils.coalesceQueryInfo(queryInfo);
	}

	@Override
	public Long count(QueryInfo queryInfo) throws QueryInfoException {
		Boolean useDistinctSelections = beanContext.getUseDistinctSelections();

		if (useDistinctSelections) {
			String message = "Distinct selections cannot be used with count queries because the count result will always be distinct";
			throw new QueryInfoException(message);
		}

		List<String> groupByAttributePaths = queryInfoUtils.coalesceGroupByAttributePaths(queryInfo);
		boolean isGroupBy = !groupByAttributePaths.isEmpty();

		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		queryInfo = coalesceQueryInfo(queryInfo);

		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);

		QueryInfoJPAContexts<RootEntity, Long> jpaContexts = createJpaContexts(criteriaQuery);

		setCountSelection(criteriaBuilder, criteriaQuery, jpaContexts);
		setQueryPredicates(jpaContexts, queryInfo);

		if (isGroupBy) {
			setQueryGroupBy(jpaContexts, queryInfo);
		}

		TypedQuery<Long> typedQuery = entityManager.createQuery(criteriaQuery);

		Long result;

		if (isGroupBy) {
			result = (long) typedQuery.getResultList().size();
		} else {
			result = typedQuery.getSingleResult();
		}

		return result;

	}

	@Override
	public QueryInfoJPAAttributePathBuilder<RootEntity, RootEntity> createJpaAttributePathBuilder() {
		QueryInfoEntityContextRegistry entityContextRegistry = beanContext.getEntityContextRegistry();
		Class<RootEntity> rootEntityClass = beanContext.getRootEntityClass();
		return QueryInfoJPAAttributePathBuilder.create(entityContextRegistry)
				.root(rootEntityClass);
	}

	protected <CriteriaQueryResultType> QueryInfoJPAContexts<RootEntity, CriteriaQueryResultType> createJpaContexts(CriteriaQuery<CriteriaQueryResultType> criteriaQuery) {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		QueryInfoJPAContextFactory jpaContextFactory = beanContext.getJpaContextFactory();
		Class<RootEntity> rootEntityClass = beanContext.getRootEntityClass();

		QueryInfoJPAContext<RootEntity, CriteriaQuery<CriteriaQueryResultType>> rootContext = jpaContextFactory.createJpaContext(criteriaBuilder,
				rootEntityClass,
				criteriaQuery);

		QueryInfoJPAContexts<RootEntity, CriteriaQueryResultType> result = new QueryInfoJPAContexts<>();
		result.setRootContext(rootContext);

		return result;
	}

	public QueryInfoEntityContext<RootEntity> getRootEntityContext() {
		Class<RootEntity> rootEntityClass = beanContext.getRootEntityClass();
		QueryInfoEntityContextRegistry entityContextRegistry = beanContext.getEntityContextRegistry();
		return entityContextRegistry.getContext(rootEntityClass);
	}

	@Override
	public List<QueryInfoResult> query(QueryInfo queryInfo) throws QueryInfoException {
		queryInfo = coalesceQueryInfo(queryInfo);

		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		Class<CriteriaQueryResult> resultClass = beanContext.getCriteriaQueryResultClass();
		CriteriaQuery<CriteriaQueryResult> criteriaQuery = criteriaBuilder.createQuery(resultClass);

		Boolean distinct = beanContext.getUseDistinctSelections();
		criteriaQuery.distinct(distinct);

		QueryInfoJPAContexts<RootEntity, CriteriaQueryResult> jpaContexts = createJpaContexts(criteriaQuery);

		setQuerySelections(jpaContexts, queryInfo);
		setQueryPredicates(jpaContexts, queryInfo);
		setQueryGroupBy(jpaContexts, queryInfo);
		setQueryOrders(jpaContexts, queryInfo);

		TypedQuery<CriteriaQueryResult> typedQuery = entityManager.createQuery(criteriaQuery);

		setPaginationInfo(typedQuery, queryInfo);

		List<CriteriaQueryResult> criteriaQueryResults = typedQuery.getResultList();

		QueryInfoResultConverter<CriteriaQueryResult, QueryInfoResult> resultConveter = beanContext.getResultConverter();

		return resultConveter.convert(criteriaQueryResults);
	}

	@Override
	public Iterator<QueryInfoResult> queryForIterator(QueryInfo queryInfo, PaginatedResultIteratorDirection direction) throws QueryInfoException {
		return new AllPaginatedResultsIterator<>(this, queryInfo, direction);
	}

	@Override
	public QueryInfoResult queryForOne(QueryInfo queryInfo) throws QueryInfoException {
		QueryInfoResult result;

		List<QueryInfoResult> queryResults = query(queryInfo);
		int resultCount = queryResults.size();

		if (resultCount == 1) {
			result = queryResults.get(0);
		} else if (resultCount > 1) {
			String message = String.format("Query for one expected 1 or 0 results but found [%s]",
				resultCount);
			throw new QueryInfoException(message);
		} else {
			result = null;
		}

		return result;
	}

	@Override
	public PaginatedResult<QueryInfoResult> queryForPaginatedResult(QueryInfo queryInfo)
			throws QueryInfoException {
		PaginatedResult<QueryInfoResult> result = new DefaultPaginatedResult<>();

		Long totalCount = count(queryInfo);
		result.setTotalCount(totalCount);

		if (totalCount > 0) {
			List<QueryInfoResult> pageResults = query(queryInfo);
			result.setPageResults(pageResults);
		}

		return result;
	}

	protected void setCountSelection(CriteriaBuilder criteriaBuilder,
			CriteriaQuery<Long> criteriaQuery,
			QueryInfoJPAContexts<RootEntity, Long> jpaContexts) {
		QueryInfoJPAContext<RootEntity, CriteriaQuery<Long>> rootContext = jpaContexts.getRootContext();
		Root<RootEntity> root = rootContext.getRoot();

		Expression<Long> countSelection = criteriaBuilder.count(root);
		criteriaQuery.select(countSelection);
	}

	protected void setPaginationInfo(TypedQuery<?> typedQuery,
			QueryInfo queryInfo) {
		PaginationInfo paginationInfo = queryInfo.getPaginationInfo();

		int firstResult;
		int maxResults;

		if (paginationInfo == null) {
			firstResult = DEFAULT_PAGE_INDEX;
			maxResults = DEFAULT_MAX_RESULTS;
		} else {
			Integer pageIndex = paginationInfo.getPageIndex();
			Integer pageSize = paginationInfo.getPageSize();

			if (pageIndex == null) {
				pageIndex = DEFAULT_PAGE_INDEX;
			}

			if (pageSize == null) {
				pageSize = DEFAULT_MAX_RESULTS;
			}

			firstResult = (pageIndex * pageSize);
			maxResults = pageSize;
		}

		typedQuery.setFirstResult(firstResult);
		typedQuery.setMaxResults(maxResults);
	}

	protected void setQueryGroupBy(QueryInfoJPAContexts<RootEntity, ?> jpaContexts,
			QueryInfo queryInfo) throws QueryInfoException {
		QueryInfoJPAContext<RootEntity, ?> rootContext = jpaContexts.getRootContext();
		QueryInfoEntityContextRegistry entityContextRegistry = beanContext.getEntityContextRegistry();
		QueryInfoGroupByFactory<RootEntity> groupByFactory = beanContext.getGroupByFactory();

		List<Expression<?>> groupByExpressions = groupByFactory.createGroupByExpressions(entityContextRegistry,
				rootContext,
				queryInfo);

		AbstractQuery<?> criteriaQuery = rootContext.getCriteriaQuery();

		criteriaQuery.groupBy(groupByExpressions);
	}

	protected void setQueryOrders(QueryInfoJPAContexts<RootEntity, CriteriaQueryResult> jpaContexts,
			QueryInfo queryInfo) throws QueryInfoException {
		QueryInfoJPAContext<RootEntity, CriteriaQuery<CriteriaQueryResult>> rootContext = jpaContexts.getRootContext();
		QueryInfoEntityContextRegistry entityContextRegistry = beanContext.getEntityContextRegistry();
		QueryInfoOrderFactory<RootEntity> orderFactory = beanContext.getOrderFactory();

		List<Order> orders = orderFactory.createOrders(entityContextRegistry, rootContext, queryInfo);

		CriteriaQuery<?> criteriaQuery = rootContext.getCriteriaQuery();

		criteriaQuery.orderBy(orders);
	}

	protected <CriteriaQueryResultType> void setQueryPredicates(QueryInfoJPAContexts<RootEntity, CriteriaQueryResultType> jpaContexts,
			QueryInfo queryInfo) throws QueryInfoException {
		QueryInfoJPAContext<RootEntity, CriteriaQuery<CriteriaQueryResultType>> rootContext = jpaContexts.getRootContext();
		QueryInfoEntityContextRegistry entityContextRegistry = beanContext.getEntityContextRegistry();
		QueryInfoPredicateFactory<RootEntity> predicateFactory = beanContext.getPredicateFactory();
		ConditionGroup conditionGroup = queryInfo.getConditionGroup();

		Predicate[] predicates = predicateFactory.createPredicates(entityContextRegistry,
				jpaContexts,
				rootContext,
				conditionGroup);

		AbstractQuery<?> criteriaQuery = rootContext.getCriteriaQuery();

		criteriaQuery.where(predicates);
	}

	protected void setQuerySelections(QueryInfoJPAContexts<RootEntity, CriteriaQueryResult> jpaContexts,
			QueryInfo queryInfo) throws QueryInfoException {
		QueryInfoEntityContextRegistry entityContextRegistry = beanContext.getEntityContextRegistry();
		QueryInfoSelectionSetter<RootEntity, CriteriaQueryResult> selectionSetter = beanContext.getSelectionSetter();
		QueryInfoJPAContext<RootEntity, CriteriaQuery<CriteriaQueryResult>> rootContext = jpaContexts.getRootContext();

		selectionSetter.setSelection(entityContextRegistry, rootContext, queryInfo);
	}

}

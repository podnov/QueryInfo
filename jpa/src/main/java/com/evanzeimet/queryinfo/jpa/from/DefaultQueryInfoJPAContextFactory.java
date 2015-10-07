package com.evanzeimet.queryinfo.jpa.from;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.evanzeimet.queryinfo.jpa.CriteriaQueryBeanContext;

public class DefaultQueryInfoJPAContextFactory<RootEntity>
		implements QueryInfoJPAContextFactory<RootEntity> {


	@Override
	public <CriteriaQueryResultType> QueryInfoJPAContext<RootEntity> createJpaContext(CriteriaBuilder criteriaBuilder,
			CriteriaQueryBeanContext<RootEntity, ?, ?> beanContext,
			CriteriaQuery<CriteriaQueryResultType> criteriaQuery) {
		Class<RootEntity> rootEntityClass = beanContext.getRootEntityClass();
		Root<RootEntity> root = criteriaQuery.from(rootEntityClass);

		QueryInfoJPAContext<RootEntity> result = new QueryInfoJPAContext<RootEntity>();

		result.setCriteriaBuilder(criteriaBuilder);
		result.setRoot(root);

		return result;
	}

}

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

import static com.evanzeimet.queryinfo.jpa.attribute.QueryInfoAttributeUtils.ENTITY_CONTEXT_REGISTRY_NOT_USED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang3.StringUtils;

import com.evanzeimet.queryinfo.jpa.attribute.QueryInfoAttributeType;
import com.evanzeimet.queryinfo.jpa.attribute.QueryInfoAttributeUtils;
import com.evanzeimet.queryinfo.jpa.entity.QueryInfoEntityContextRegistry;
import com.evanzeimet.queryinfo.jpa.join.QueryInfoJoinInfo;

public class QueryInfoJPAAttributePathBuilder<R, T> {

	private static final QueryInfoAttributeUtils attributeUtils = new QueryInfoAttributeUtils();

	private final QueryInfoEntityContextRegistry entityContextRegistry;
	private final List<Attribute<?, ?>> attributes;
	private final Class<R> root;

	protected QueryInfoJPAAttributePathBuilder(QueryInfoEntityContextRegistry entityContextRegistry,
			Class<R> root) {
		this.entityContextRegistry = entityContextRegistry;
		this.root = root;
		this.attributes = Collections.emptyList();
	}

	protected QueryInfoJPAAttributePathBuilder(QueryInfoEntityContextRegistry entityContextRegistry,
			Class<R> root,
			List<Attribute<?, ?>> parents,
			Attribute<?, ?> attribute) {
		this.entityContextRegistry = entityContextRegistry;
		this.root = root;
		this.attributes = asAttributeList(parents, attribute);
	}

	public <NT> QueryInfoJPAAttributePathBuilder<R, NT> add(PluralAttribute<? super T, ?, NT> attribute) {
		return new QueryInfoJPAAttributePathBuilder<R, NT>(entityContextRegistry,
				root,
				attributes,
				attribute);
	}

	public <NT> QueryInfoJPAAttributePathBuilder<R, NT> add(SingularAttribute<? super T, NT> attribute) {
		return new QueryInfoJPAAttributePathBuilder<R, NT>(entityContextRegistry,
				root,
				attributes,
				attribute);
	}

	protected List<Attribute<?, ?>> asAttributeList(Attribute<?, ?> attributeInfo) {
		List<Attribute<?, ?>> parents = Collections.emptyList();
		return asAttributeList(parents, attributeInfo);
	}

	protected List<Attribute<?, ?>> asAttributeList(List<Attribute<?, ?>> parents,
			Attribute<?, ?> attribute) {
		int attributeCount = (parents.size() + 1);
		List<Attribute<?, ?>> result = new ArrayList<>(attributeCount);

		result.addAll(parents);
		result.add(attribute);

		return result;
	}

	public List<String> buildList() {
		return buildList(QueryInfoAttributeUtils.ATTRIBUTE_TYPE_NOT_SPECIFIED);
	}

	public List<String> buildList(QueryInfoAttributeType leafAttributeType) {
		List<String> result;

		if (attributes == null || attributes.isEmpty()) {
			result = Collections.emptyList();
		} else {
			int attributeCount = attributes.size();
			result = new ArrayList<>(attributeCount);

			int joinCount = (attributeCount - 1);
			Class<?> nextAttributeHost = root;

			for (int attributeIndex = 0; attributeIndex < joinCount; attributeIndex++) {
				Attribute<?, ?> attribute = attributes.get(attributeIndex);

				String joinAttributeName = getJoinAttributeName(nextAttributeHost, attribute);
				result.add(joinAttributeName);

				nextAttributeHost = getAttributeJoinedType(attribute);
			}

			Attribute<?, ?> attribute = attributes.get(attributeCount - 1);

			String leafAttributeName = getLeafAttributeName(nextAttributeHost,
					attribute,
					leafAttributeType);

			result.add(leafAttributeName);
		}

		return result;
	}

	public String buildString() {
		return buildString(QueryInfoAttributeUtils.ATTRIBUTE_TYPE_NOT_SPECIFIED);
	}

	public String buildString(QueryInfoAttributeType leafAttributeType) {
		List<String> attributeNames = buildList(leafAttributeType);
		return StringUtils.join(attributeNames, ".");
	}

	public QueryInfoJPAAttributePathBuilder<R, R> clear() {
		return new QueryInfoJPAAttributePathBuilder<R, R>(entityContextRegistry,
				root);
	}

	public static RootBuilder create() {
		return create(ENTITY_CONTEXT_REGISTRY_NOT_USED);
	}

	public static RootBuilder create(QueryInfoEntityContextRegistry entityContextRegistry) {
		return new RootBuilder(entityContextRegistry);
	}

	protected Class<?> getAttributeJoinedType(Attribute<?, ?> attribute) {
		Class<?> result;

		if (attribute instanceof PluralAttribute<?, ?, ?>) {
			PluralAttribute<?, ?, ?> pluralAttribute = ((PluralAttribute<?, ?, ?>) attribute);
			result = pluralAttribute.getBindableJavaType();
		} else {
			result = attribute.getJavaType();
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	protected <H> QueryInfoFieldInfo getFieldInfo(Class<?> attributeHost, Attribute<?, ?> attribute) {
		return attributeUtils.getFieldInfo(entityContextRegistry,
				(Class<H>) attributeHost,
				(Attribute<? super H, ?>) attribute);
	}

	@SuppressWarnings("unchecked")
	protected <H> String getJoinAttributeName(Class<?> attributeHost,
			Attribute<?, ?> attribute) {
		return attributeUtils.getJoinAttributeName(entityContextRegistry,
				(Class<H>) attributeHost,
				(Attribute<? super H, ?>) attribute);
	}

	@SuppressWarnings("unchecked")
	protected <H> QueryInfoJoinInfo getJoinInfo(Class<?> attributeHost,
			Attribute<?, ?> attribute) {
		return attributeUtils.getJoinInfo(entityContextRegistry,
					(Class<H>) attributeHost,
					(Attribute<? super H, ?>) attribute);
	}

	@SuppressWarnings("unchecked")
	protected <H> String getLeafAttributeName(Class<?> attributeHost,
			Attribute<?, ?> attribute,
			QueryInfoAttributeType attributeType) {
		return attributeUtils.getAttributeName(entityContextRegistry,
				(Class<H>) attributeHost,
				(Attribute<? super H, ?>) attribute,
				attributeType);
	}

	public static class RootBuilder {

		private final QueryInfoEntityContextRegistry entityContextRegistry;

		protected RootBuilder(QueryInfoEntityContextRegistry entityContextRegistry) {
			this.entityContextRegistry = entityContextRegistry;
		}

		public <T> QueryInfoJPAAttributePathBuilder<T, T> root(Class<T> root) {
			return new QueryInfoJPAAttributePathBuilder<T, T>(entityContextRegistry, root);
		}

	}
}

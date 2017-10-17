package ua.com.fielden.platform.entity_centre.review;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchNone;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.util.Set;

import ua.com.fielden.platform.dynamictree.DynamicEntityTree;
import ua.com.fielden.platform.dynamictree.DynamicEntityTreeNode;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;

public class DynamicFetchBuilder {

	private static enum FetchStrategy {
		FETCH {
			<T extends AbstractEntity<?>> fetch<T> buildFetchModel(Class<T> entityType) {
				return fetch(entityType);
			}
		},
		FETCH_ONLY {
			<T extends AbstractEntity<?>> fetch<T> buildFetchModel(Class<T> entityType) {
				return fetchOnly(entityType);
			}
		},
		FETCH_NONE {
			<T extends AbstractEntity<?>> fetch<T> buildFetchModel(Class<T> entityType) {
				return fetchNone(entityType);
			}
		};

		abstract <T extends AbstractEntity<?>> fetch<T> buildFetchModel(final Class<T> entityType);
	};

	/**
	 * Creates "fetch property" model for entity query criteria.
	 *
	 * @return
	 */
	public static <T extends AbstractEntity<?>> fetch<T> createFetchOnlyModel(final Class<T> managedType, final Set<String> fetchProperties) {
		return createFetch(managedType, fetchProperties, FetchStrategy.FETCH_ONLY);
	}

	/**
	 * Creates "fetch property" model for entity query criteria.
	 *
	 * @return
	 */
	public static <T extends AbstractEntity<?>> fetch<T> createFetchModel(final Class<T> managedType, final Set<String> fetchProperties) {
		return createFetch(managedType, fetchProperties, FetchStrategy.FETCH);
	}

	/**
	 * Creates "fetch property" model for entity query criteria totals.
	 *
	 * @return
	 */
	public static <T extends AbstractEntity<?>> fetch<T> createTotalFetchModel(final Class<T> managedType, final Set<String> fetchProperties) {
		return createFetch(managedType, fetchProperties, FetchStrategy.FETCH_NONE);
	}

	/**
	 * Creates general fetch model for passed properties and type.
	 *
	 * @param managedType
	 * @param fetchProperties
	 */
	private static <T extends AbstractEntity<?>> fetch<T> createFetch(final Class<T> managedType, final Set<String> fetchProperties, final FetchStrategy fetchStrategy) {
		final DynamicEntityTree<T> fetchTree = new DynamicEntityTree<T>(fetchProperties, managedType);
		return buildFetchModels(managedType, fetchTree.getRoot(), fetchStrategy);
	}

	/**
	 * Builds the fetch model for subtree specified with treeNode parameter.
	 *
	 * @param entityType
	 *            - The type for fetch model.
	 * @param treeNode
	 *            - the root of subtree for which fetch model must be build.
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private static <T extends AbstractEntity<?>> fetch<T> buildFetchModels(final Class<T> entityType, final DynamicEntityTreeNode treeNode, final FetchStrategy fetchStrategy) {
		fetch<T> fetchModel = fetchStrategy.buildFetchModel(entityType);

		if (treeNode == null || treeNode.getChildCount() == 0) {
			return fetchModel;
		}

		for (final DynamicEntityTreeNode dynamicTreeNode : treeNode.getChildren()) {
			final Class<?> propertyType = dynamicTreeNode.getType();
			if (!isEntityType(propertyType)) {
				fetchModel = fetchModel.with(dynamicTreeNode.getName());
			} else {
				final fetch<? extends AbstractEntity<?>> fetchSubModel = buildFetchModels(
						(Class<? extends AbstractEntity<?>>) propertyType, dynamicTreeNode, fetchStrategy);
				fetchModel = fetchModel.with(dynamicTreeNode.getName(), fetchSubModel);
			}
		}
		return fetchModel;
	}
}
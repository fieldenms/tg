package ua.com.fielden.platform.swing.review;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isQueryBasedEntityType;

import java.util.Set;

import ua.com.fielden.platform.dynamictree.DynamicEntityTree;
import ua.com.fielden.platform.dynamictree.DynamicEntityTreeNode;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.entity.query.fluent.fetch;

public class DynamicFetchBuilder {

    /**
     * Creates "fetch property" model for entity query criteria.
     *
     * @return
     */
    public static <T extends AbstractEntity<?>> fetch<T> createFetchOnlyModel(final Class<T> managedType, final Set<String> fetchProperties) {
       return fetch(managedType, fetchProperties, true);
    }

    /**
     * Creates "fetch property" model for entity query criteria.
     *
     * @return
     */
    public static <T extends AbstractEntity<?>> fetch<T> createFetchModel(final Class<T> managedType, final Set<String> fetchProperties) {
       return fetch(managedType, fetchProperties, false);
    }

    /**
     * Creates "fetch property" model for entity query criteria totals.
     *
     * @return
     */
    public static <T extends AbstractEntity<?>> fetch<T> createTotalFetchModel(final Class<T> managedType, final Set<String> fetchProperties) {
	final fetch<T> result = fetch(managedType, fetchProperties, true);
	return isQueryBasedEntityType(managedType) ? result : result.without("id").without("version");
    }

    /**
     * Creates general fetch model for passed properties and type.
     *
     * @param managedType
     * @param fetchProperties
     */
    private static <T extends AbstractEntity<?>> fetch<T> fetch(final Class<T> managedType, final Set<String> fetchProperties, final boolean fetchOnly){
	try {
            final DynamicEntityTree<T> fetchTree = new DynamicEntityTree<T>(fetchProperties, managedType);
            final fetch<T> main = buildFetchModels(managedType, fetchTree.getRoot(), fetchOnly);
            return main;
        } catch (final Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    /**
     * Builds the fetch model for subtree specified with treeNode parameter.
     *
     * @param entityType - The type for fetch model.
     * @param treeNode - the root of subtree for which fetch model must be build.
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private static <T extends AbstractEntity<?>> fetch<T> buildFetchModels(final Class<T> entityType, final DynamicEntityTreeNode treeNode, final boolean fetchOnly) throws Exception {
        fetch<T> fetchModel = fetchOnly ? fetchOnly(entityType) : EntityQueryUtils.fetch(entityType);

        if (treeNode == null || treeNode.getChildCount() == 0) {
            return fetchModel;
        }

        for (final DynamicEntityTreeNode dynamicTreeNode : treeNode.getChildren()) {
            final Class<?> propertyType = dynamicTreeNode.getType();
            if (!isEntityType(propertyType)) {
        	fetchModel = fetchModel.with(dynamicTreeNode.getName());
            }else{
        	final fetch<? extends AbstractEntity<?>> fetchSubModel = buildFetchModels((Class<? extends AbstractEntity<?>>) propertyType, dynamicTreeNode, fetchOnly);
        	fetchModel = fetchModel.with(dynamicTreeNode.getName(), fetchSubModel);
            }
        }
        return fetchModel;
    }

}

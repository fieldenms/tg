package ua.com.fielden.platform.dynamictree;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;
import ua.com.fielden.platform.utils.EntityUtils;

import static ua.com.fielden.platform.utils.EntityUtils.laxSplitPropPath;

/**
 * Tree structure that holds the properties to fetch.
 *
 * @author TG Team
 *
 */
public class DynamicEntityTree<T extends AbstractEntity<?>> {

    private final DynamicEntityTreeNode root;

    /**
     * Builds the tree structure of fetch properties.
     *
     * @param fetchProperties
     *            - the set of properties those are used to build the tree of fetch properties.
     * @param rootType
     *            - the root type for all fetch properties.
     */
    public DynamicEntityTree(final Set<String> fetchProperties, final Class<T> rootType) {
        root = new DynamicEntityTreeNode(rootType.getSimpleName(), rootType);
        for (final String property : fetchProperties) {
            DynamicEntityTreeNode currentNode = root;
            for (final String splitString : laxSplitPropPath(property)) {
                if (!StringUtils.isEmpty(splitString)) {
                    DynamicEntityTreeNode childNode = currentNode.getChild(splitString);
                    if (childNode == null) {
                        childNode = new DynamicEntityTreeNode(splitString, PropertyTypeDeterminator.determinePropertyType(currentNode.getType(), splitString));
                        currentNode.addChild(splitString, childNode);
                    }
                    currentNode = childNode;
                }
            }
            if (EntityUtils.isEntityType(currentNode.getType())) {
                currentNode.addChild(AbstractEntity.KEY, new DynamicEntityTreeNode(AbstractEntity.KEY, AnnotationReflector.getKeyType(currentNode.getType())));
                if (EntityDescriptor.hasDesc(currentNode.getType())) {
                    currentNode.addChild(AbstractEntity.DESC, new DynamicEntityTreeNode(AbstractEntity.DESC, PropertyTypeDeterminator.determinePropertyType(currentNode.getType(), AbstractEntity.DESC)));
                }
            }
        }
    }

    public DynamicEntityTreeNode getRoot() {
        return root;
    }
}

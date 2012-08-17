package ua.com.fielden.platform.dynamictree;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 *
 *
 * @author oleh
 *
 */
public class DynamicEntityTree<T extends AbstractEntity> {

    private final DynamicEntityTreeNode root;

    public DynamicEntityTree(final Set<String> fetchProperties, final Class<T> rootType) {
	root = new DynamicEntityTreeNode(rootType.getSimpleName(), rootType);
	for (final String property : fetchProperties) {
	    final String[] splited = property.split(Reflector.DOT_SPLITTER);
	    DynamicEntityTreeNode currentNode = root;
	    for (final String splitString : splited) {
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

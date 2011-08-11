package ua.com.fielden.platform.dynamictree;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;

/**
 * 
 * 
 * @author oleh
 * 
 */
public class DynamicEntityTree<T extends AbstractEntity> {

    private final static String KEY = "key";

    private final DynamicEntityTreeNode root;

    public DynamicEntityTree(final List<String> fetchProperties, final Class<T> rootType) {
	root = new DynamicEntityTreeNode(rootType.getSimpleName(), rootType);
	for (final String property : fetchProperties) {
	    if (StringUtils.isEmpty(property)) {
		root.addChild(KEY, new DynamicEntityTreeNode(KEY, AnnotationReflector.getKeyType(rootType)));
	    } else {
		final String[] splited = property.split(Reflector.DOT_SPLITTER);
		DynamicEntityTreeNode currentNode = root;
		for (final String splitString : splited) {
		    DynamicEntityTreeNode childNode = currentNode.getChild(splitString);
		    if (childNode == null) {
			childNode = new DynamicEntityTreeNode(splitString, PropertyTypeDeterminator.determinePropertyType(currentNode.getType(), splitString));
			currentNode.addChild(splitString, childNode);
		    }
		    currentNode = childNode;
		}
	    }
	}
    }

    public DynamicEntityTreeNode getRoot() {
	return root;
    }
}

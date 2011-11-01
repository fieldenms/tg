package ua.com.fielden.platform.treemodel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * The tree model for the EntitiesTree. Creates nodes for the "entityClasses" existing in specified applicationModel.
 *
 * @author Jhou
 *
 */
public class EntitiesTreeModel extends DefaultTreeModel {
    private static final long serialVersionUID = 1L;
    public static final TitledObject DUMMY_TITLED_OBJECT = new TitledObject("dummy", "Dummy", "Dummy sub-property for collapsed hot properties.", String.class, false);
    public static final String ROOT_CAPTION = "Entities";

    private final IPropertyFilter propertyFilter;

    private final Logger logger = Logger.getLogger(this.getClass());

    public EntitiesTreeModel(final List<Class<? extends AbstractEntity>> entityClasses, final IPropertyFilter propertyFilter) {
	super(null);
	this.propertyFilter = propertyFilter;
	final DefaultMutableTreeNode root = new DefaultMutableTreeNode(new TitledObject(null, ROOT_CAPTION, "Available entities", null));
	setRoot(root);
	addEntities(entityClasses, null);
    }

    /**
     * Adds <code>entityClasses</code> hierarchy.
     *
     * @param entityClasses
     * @param entityWhichAggregatesCollectionalEntityClasses
     *            - if not null -- sub-trees would be modified to be "collectional properties" of specified type.
     */
    protected void addEntities(final List<Class<? extends AbstractEntity>> entityClasses, final Class<? extends AbstractEntity> entityWhichAggregatesCollectionalEntityClasses) {
	for (final Class<?> klass : entityClasses) {
	    final Pair<String, String> entityTitleAndDesc = (entityWhichAggregatesCollectionalEntityClasses == null) ? //
	    TitlesDescsGetter.getEntityTitleAndDesc(klass)
		    : //
		    TitlesDescsGetter.getEntityTitleAndDescInCollectionalPropertyContex(klass, entityWhichAggregatesCollectionalEntityClasses);
	    logger.debug("title + desc == " + entityTitleAndDesc);
	    final DefaultMutableTreeNode klassNode = new DefaultMutableTreeNode(new TitledObject(klass, entityTitleAndDesc.getKey(), TitlesDescsGetter.italic("<b>"
		    + entityTitleAndDesc.getValue() + "</b>"), klass));
	    ((DefaultMutableTreeNode) getRoot()).add(klassNode);
	    addHotNodeProperties(klassNode, klass, propertyFilter, true, null);
	}
    }

    /**
     * The object with specified title.
     *
     * @author Jhou
     *
     */
    public static class TitledObject implements Cloneable {
	// represents the property field: name title and description
	private final Object object;
	private final String title;
	private final String desc;

	// represents the type of the property or the type of element class (in case when property isCollectional)
	private final Class<?> type;
	private final boolean isCollectional;

	public TitledObject(final Object object, final String title, final String desc, final Class<?> type) {
	    this(object, title, desc, type, false);
	}

	public TitledObject(final Object object, final String title, final String desc, final Class<?> type, final boolean isCollectional) {
	    this.object = object;
	    this.title = "".equals(title) ? (object != null ? (object instanceof Class<?> ? ((Class<?>) object).getSimpleName() : object.toString()) : title) : title;
	    this.desc = desc;
	    this.type = type;
	    this.isCollectional = isCollectional;
	}

	public Object getObject() {
	    return object;
	}

	public String getTitle() {
	    return title;
	}

	public Class<?> getType() {
	    return type;
	}

	@Override
	public String toString() {
	    return title;
	}

	@Override
	public TitledObject clone() {
	    final TitledObject titledObject = new TitledObject(getObject(), getTitle(), getDesc(), getType(), isCollectional());
	    return titledObject;
	}

	public String getDesc() {
	    return desc;
	}

	@Override
	public boolean equals(final Object obj) {
	    if (this == obj) {
		return true;
	    }
	    if (obj == null || obj.getClass() != this.getClass()) {
		return false;
	    }
	    final TitledObject titledObject = (TitledObject) obj;
	    if ((getObject() == null && getObject() != titledObject.getObject()) || (getObject() != null && !getObject().equals(titledObject.getObject()))) {
		return false;
	    }
	    if ((getType() == null && getType() != titledObject.getType()) || (getType() != null && !getType().equals(titledObject.getType()))) {
		return false;
	    }
	    if ((getDesc() == null && getDesc() == titledObject.getDesc()) || (getDesc() != null && !getDesc().equals(titledObject.getDesc()))) {
		return false;
	    }
	    if ((toString() == null && toString() == titledObject.toString()) || (toString() != null && !toString().equals(titledObject.toString()))) {
		return false;
	    }
	    if (isCollectional() != titledObject.isCollectional()) {
		return false;
	    }
	    return true;
	}

	@Override
	public int hashCode() {
	    int result = 17;
	    result = 31 * result + (toString() != null ? toString().hashCode() : 0);
	    result = 31 * result + (getDesc() != null ? getDesc().hashCode() : 0);
	    result = 31 * result + (getType() != null ? getType().hashCode() : 0);
	    result = 31 * result + (getObject() != null ? getObject().hashCode() : 0);
	    result = 31 * result + (isCollectional() ? 1 : 0);
	    return result;
	}

	public boolean isCollectional() {
	    return isCollectional;
	}

	/**
	 * Returns {@link TitledObject} instance for specified object if it exists.
	 *
	 * @param treeNode
	 * @return
	 */
	public static TitledObject extractTitleFromTreeNode(final Object treeNode) {
	    final DefaultMutableTreeNode treeDNode = treeNode instanceof DefaultMutableTreeNode ? (DefaultMutableTreeNode) treeNode : null;
	    final TitledObject nodeTitle = (treeDNode != null && treeDNode.getUserObject() instanceof TitledObject) ? (TitledObject) treeDNode.getUserObject() : null;
	    return nodeTitle;
	}

    }

    /**
     * Constructs the list of <code>clazz</code>'s fields for specified "propertyNames".
     *
     * @param clazz
     * @param propertyNames
     * @param propertyFilter
     * @return
     */
    private static List<Field> constructKeysAndProperties(final Class<?> clazz, final List<String> propertyNames, final IPropertyFilter propertyFilter) {
	final List<Field> allProperties = constructAllKeysAndProperties(clazz, propertyFilter);
	final List<Field> properties = new ArrayList<Field>();
	for (final Field f : allProperties) {
	    if (propertyNames.contains(f.getName())) {
		properties.add(f);
	    }
	}
	return properties;
    }

    /**
     * Constructs the list of all <code>clazz</code>'s fields in order 1. "key" or key members 2. "desc" 2. other properties.
     *
     * @param clazz
     * @return
     */
    private static List<Field> constructAllKeysAndProperties(final Class<?> clazz, final IPropertyFilter propertyFilter) {
	final List<Field> properties = Finder.findProperties(clazz);
	properties.remove(Finder.getFieldByName(clazz, AbstractEntity.KEY));
	properties.remove(Finder.getFieldByName(clazz, AbstractEntity.DESC));
	final List<Field> keys = Finder.getKeyMembers(clazz);
	properties.removeAll(keys);

	final List<Field> fieldsAndKeys = new ArrayList<Field>();
	fieldsAndKeys.addAll(keys);
	fieldsAndKeys.add(Finder.getFieldByName(clazz, AbstractEntity.DESC));
	fieldsAndKeys.addAll(properties);
	final List<Field> result = new ArrayList<Field>();
	for (final Field f : fieldsAndKeys) {
	    if (propertyFilter != null && !propertyFilter.shouldExcludeProperty(clazz, f)) {
		result.add(f);
	    }
	}
	return result;
    }

    /**
     * Returns true if on the hierarchy of klassNode (VERY IMPORTANT : the hierarchy should be initialized before!) there is at least one node with type == "propertyType".
     *
     * @param propertyType
     * @param klassNode
     * @return
     */
    private boolean propertyTypeWasInHierarchyBefore(final Class<?> propertyType, final DefaultMutableTreeNode klassNode) {
	for (final TreeNode node : getPathToRoot(klassNode)) {
	    if (propertyType.equals(((TitledObject) ((DefaultMutableTreeNode) node).getUserObject()).getType())) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Adds nodes into {@code klassNode} corresponding to specified properties in {@code clazz}.
     *
     * @param klassNode
     * @param clazz
     * @param propertyNames
     * @param propertyFilter
     * @param initialization
     */
    public void addHotNodeForSpecificProperties(final DefaultMutableTreeNode klassNode, final Class<?> clazz, final List<Field> fieldsAndKeys, final IPropertyFilter propertyFilter, final boolean initialization, final String linkProperty) {
	for (final Field field : fieldsAndKeys) {
	    // Find title and description for the property.
	    final Pair<String, String> tad = TitlesDescsGetter.getTitleAndDesc(field.getName(), clazz);
	    final Pair<String, String> titleAndDesc = new Pair<String, String>(tad.getKey(), TitlesDescsGetter.italic("<b>" + tad.getValue() + "</b>")); // Reflector.getFullTitleAndDesc(field.getName(),
	    // clazz);
	    // determine correct property type.
	    final Class<?> propertyType = PropertyTypeDeterminator.determineClass(clazz, field.getName(), true, false);
	    // ignore all "desc" properties that have no "DescTitle" annotation associated. This is convenient method of indicating that "desc" is not mapped and have no sense in
	    // domain model. TODO maybe this convenience should be improved in more elegant way.
	    final boolean notDesc = !AbstractEntity.DESC.equals(field.getName());
	    final boolean notDescOrAnnotatedDesc = notDesc || (!notDesc && AnnotationReflector.isAnnotationPresent(DescTitle.class, clazz));
	    // propertyType should not be null, but it is null in case when property has parameterized type. (see RotableLocation class "key" property in platform examples)
	    //
	    final boolean shouldBuildChildren = propertyFilter.shouldBuildChildrenFor(clazz, field);
	    final boolean isLinkPropertyForCollection = field.getName().equals(linkProperty);
	    if (propertyType != null && notDescOrAnnotatedDesc && !isLinkPropertyForCollection) {
		if (Collection.class.isAssignableFrom(propertyType) && shouldBuildChildren) { // property is collectional :
		    final String linkPropertyNew = AnnotationReflector.getPropertyAnnotation(IsProperty.class, clazz, field.getName()).linkProperty();
		    addHotNodeWithChildren(field.getName(), PropertyTypeDeterminator.determineCollectionElementClass(field), true, klassNode, titleAndDesc, propertyFilter, false, initialization, linkPropertyNew);
		} else if (AbstractEntity.class.isAssignableFrom(propertyType) && shouldBuildChildren) { // property is of AbstractEntity descendant type:
		    final boolean isKey = Finder.getKeyMembers(clazz).contains(field) && AbstractEntity.KEY.equals(field.getName()); // indicates if field is the the "clazz"'s key.
		    addHotNodeWithChildren(field.getName(), propertyType, false, klassNode, titleAndDesc, propertyFilter, isKey, initialization, null);
		} else { // simple type
		    klassNode.add(new DefaultMutableTreeNode(new TitledObject(field.getName(), titleAndDesc.getKey(), titleAndDesc.getValue(), propertyType)));
		}
	    }
	}
    }

    /**
     * Adds nodes corresponding to property fields to the specified "klassNode".
     *
     * @param klassNode
     * @param clazz
     * @param fieldsAndKeys
     */
    public void addHotNodeProperties(final DefaultMutableTreeNode klassNode, final Class<?> clazz, final IPropertyFilter propertyFilter, final boolean initialization, final String linkProperty) {
	if (AbstractEntity.class.isAssignableFrom(clazz) && !AbstractUnionEntity.class.isAssignableFrom(clazz)) { // non "union" entity
	    final List<Field> children = new ArrayList<Field>();
	    final Class<?> parentClass = klassNode.getParent() == null ? null : ((TitledObject) ((DefaultMutableTreeNode) klassNode.getParent()).getUserObject()).getType();
	    if (parentClass != null && AbstractUnionEntity.class.isAssignableFrom(parentClass)) {
		final List<Field> fieldsWithoutCommonProperties = constructAllKeysAndProperties(clazz, propertyFilter);
		final List<Field> parentCommonProperties = constructKeysAndProperties(clazz, AbstractUnionEntity.commonProperties((Class<? extends AbstractUnionEntity>) parentClass), propertyFilter);
		fieldsWithoutCommonProperties.removeAll(parentCommonProperties);
		children.addAll(fieldsWithoutCommonProperties);
	    } else {
		children.addAll(constructAllKeysAndProperties(clazz, propertyFilter));
	    }
	    addHotNodeForSpecificProperties(klassNode, clazz, children, propertyFilter, initialization, linkProperty);
	} else if (AbstractUnionEntity.class.isAssignableFrom(clazz)) { // "union" entity
	    // "union" entity (parent is AbstractUnionEntity type):
	    final Class<? extends AbstractUnionEntity> unionClass = (Class<? extends AbstractUnionEntity>) clazz;
	    final Class<? extends AbstractEntity> concreteUnionClass = (Class<? extends AbstractEntity>) (AbstractUnionEntity.unionProperties(unionClass, propertyFilter).get(0).getType());
	    final List<Field> commonProperties = constructKeysAndProperties(concreteUnionClass, AbstractUnionEntity.commonProperties(unionClass), propertyFilter);
	    // the new node should be created for "common" properties (and they should be added).
	    final DefaultMutableTreeNode nodeForCommonProperties = addHotNode("common", null, false, klassNode, new Pair<String, String>("Common", TitlesDescsGetter.italic("<b>Common properties</b>")));
	    addHotNodeForSpecificProperties(nodeForCommonProperties, concreteUnionClass, commonProperties, propertyFilter, initialization, linkProperty);
	    // and also "union" properties should be added root node:
	    addHotNodeForSpecificProperties(klassNode, clazz, AbstractUnionEntity.unionProperties(unionClass, propertyFilter), propertyFilter, initialization, linkProperty);
	}
    }

    /**
     * Adds hot (collectional or not) node and its children to the specified "klassNode".
     *
     * @param propertyName
     * @param klass
     *            - the type of "hot". (or the type of element of "collectional-hot")
     * @param isCollectional
     *            - true if hot is collectional, otherwise false.
     * @param klassNode
     * @param titleAndDesc
     * @param propertyFilter
     */
    private void addHotNodeWithChildren(final String propertyName, final Class<?> klass, final boolean isCollectional, final DefaultMutableTreeNode klassNode, final Pair<String, String> titleAndDesc, final IPropertyFilter propertyFilter, final boolean isKey, final boolean initialization, final String linkProperty) {
	if (AbstractEntity.class.isAssignableFrom(klass)) { // supports only elements of AbstractEntity descendants
	    System.out.println(linkProperty + " = linkProperty, " + propertyName + " = propertyName");
	    final boolean isLinkPropertyForCollection = propertyName.equals(linkProperty);
	    if (!isLinkPropertyForCollection) { // exclude "link properties" for collections
		if (!propertyTypeWasInHierarchyBefore(klass, klassNode)) { // add hot node and its children
		    addHotNodeProperties(addHotNode(propertyName, klass, isCollectional, klassNode, titleAndDesc), klass, propertyFilter, initialization, linkProperty);
		} else if (!isKey) { // add the only hot node in case if it is not "key".
		    final DefaultMutableTreeNode hotPropertyNode = addHotNode(propertyName, klass, isCollectional, klassNode, titleAndDesc);
		    if (initialization) {
			hotPropertyNode.add(new DefaultMutableTreeNode(DUMMY_TITLED_OBJECT));
		    } else { // dynamic node expanding :
			hotPropertyNode.removeFromParent();
			addHotNodeWithChildren(propertyName, klass, isCollectional, klassNode, titleAndDesc, propertyFilter, isKey, true, linkProperty);
		    }
		}
	    }
	}
    }

    /**
     * Simply creates and adds single hot node for property.
     *
     * @param propertyName
     * @param klass
     * @param isCollectional
     * @param klassNode
     * @param titleAndDesc
     * @return
     */
    private DefaultMutableTreeNode addHotNode(final String propertyName, final Class<?> klass, final boolean isCollectional, final DefaultMutableTreeNode klassNode, final Pair<String, String> titleAndDesc) {
	final DefaultMutableTreeNode hotPropertyNode = new DefaultMutableTreeNode(new TitledObject(propertyName, titleAndDesc.getKey(), titleAndDesc.getValue(), klass, isCollectional));
	klassNode.add(hotPropertyNode);
	return hotPropertyNode;
    }

    /**
     * Loads properties for some node if properties were not loaded before.
     *
     * @param node
     *            - to be expanded (to make an attempt)
     * @return true - if properties were successfully loaded, false - if nothing happened.
     */
    public boolean loadProperties(final DefaultMutableTreeNode node) {
	if (node != null && node.getChildCount() == 1 && EntitiesTreeModel.DUMMY_TITLED_OBJECT.equals(((DefaultMutableTreeNode) node.getFirstChild()).getUserObject())) {
	    node.removeAllChildren();
	    addHotNodeProperties(node, ((TitledObject) node.getUserObject()).getType(), propertyFilter, false, null);
	    return true;
	}
	return false;
    }

    /**
     * Traverses through <code>propertyNamePath</code> and loads missing properties into entities tree model.
     *
     * Cycled properties/entities could not be loaded fully, but in some cases we should use them (e.g. "Vehicle.replacing.DUMMY_TITLED_OBJECT" was loaded, but
     * "Vehicle.replacing.replacedBy.status" should be selected). Use this method to load missing nodes.
     *
     * @param propertyNamePath
     *            convenience -> "Entities", clazz.getSimpleName(), firstPropertyName, ... lastPropertyName (first node "Entities" could be missed!)
     */
    public void load(final List<String> propertyNamePath) {
	logger.debug("Loads : " + propertyNamePath);
	DefaultMutableTreeNode current = (DefaultMutableTreeNode) getRoot();
	propertyNamePath.remove(0);
	for (final String propertyName : propertyNamePath) {
	    // if (ROOT_CAPTION.equals(propertyName)) {
	    // continue;
	    // }
	    current = findNode(current, propertyName);
	    loadProperties(current);
	}
    }

    private DefaultMutableTreeNode findNode(final DefaultMutableTreeNode current, final String propertyName) {
	for (int i = 0; i < current.getChildCount(); i++) {
	    final Object o = ((TitledObject) ((DefaultMutableTreeNode) current.getChildAt(i)).getUserObject()).getObject();
	    if (o instanceof String && propertyName.equals(o) || o instanceof Class && propertyName.equals(((Class) o).getSimpleName())) { // null ignored
		return (DefaultMutableTreeNode) current.getChildAt(i);
	    }
	}
	return null;
    }

    /**
     * Returns dot-notation property name that corresponds to the specified treeNode in the model.
     *
     * @param treeNode
     * @return
     */
    public String getPropertyNameFor(final DefaultMutableTreeNode treeNode) {
	final TreeNode[] nodePath = treeNode.getPath();
	String propertyName = "";
	for (final TreeNode node : nodePath) {
	    if (node instanceof DefaultMutableTreeNode) {
		final Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
		if (userObject instanceof TitledObject) {
		    final TitledObject userTitle = (TitledObject) userObject;
		    if ((userTitle.getObject() instanceof String) && !"dummy".equals(userTitle.getObject()) && !"common".equals(userTitle.getObject())) {
			propertyName += "." + userTitle.getObject().toString();
		    }
		}
	    }
	}
	return StringUtils.isEmpty(propertyName) ? propertyName : propertyName.substring(1);
    }

    /**
     *
     *
     * @param treeNode
     * @return
     */
    public Class<?> getPropertyType(final DefaultMutableTreeNode treeNode) {
	final TreeNode[] nodePath = treeNode.getPath();
	if (nodePath.length >= 2) {
	    final TitledObject titledObject = (TitledObject) ((DefaultMutableTreeNode) nodePath[1]).getUserObject();
	    return titledObject.getType();
	}
	return null;
    }

    /**
     * Returns tree path for the specified property name.
     *
     * @param dotNotationExp
     * @param treeModel
     * @return
     */
    public TreePath getPathFromPropertyName(final String dotNotationExp) {
	if (dotNotationExp == null) {
	    return null;
	}
	DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) getRoot();
	TitledObject currentTitled = (TitledObject) currentNode.getUserObject();
	while (currentTitled.getObject() == null) {
	    currentNode = (DefaultMutableTreeNode) currentNode.getChildAt(0);
	    currentTitled = (TitledObject) currentNode.getUserObject();
	}
	if (StringUtils.isEmpty(dotNotationExp)) {
	    return new TreePath(currentNode.getPath());
	}
	final String[] splittedProperties = dotNotationExp.split(Reflector.DOT_SPLITTER);
	int propertyIndex = 0;
	while (propertyIndex < splittedProperties.length) {
	    String property = splittedProperties[propertyIndex];
	    if (currentTitled.getType() != null && AbstractUnionEntity.class.isAssignableFrom(currentTitled.getType())) {
		final List<String> commonProperties = AbstractUnionEntity.commonProperties((Class<AbstractUnionEntity>) currentTitled.getType(), propertyFilter);
		if (commonProperties.contains(property)) {
		    property = "common";
		} else {
		    propertyIndex++;
		}
	    } else {
		propertyIndex++;
	    }
	    DefaultMutableTreeNode nextTreeNode = null;
	    for (int childCounter = 0; childCounter < currentNode.getChildCount(); childCounter++) {
		final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) currentNode.getChildAt(childCounter);
		currentTitled = (TitledObject) childNode.getUserObject();
		if (currentTitled.getObject().equals(property)) {
		    nextTreeNode = childNode;
		    break;
		}
	    }
	    if (nextTreeNode == null) {
		return null;
	    }
	    currentNode = nextTreeNode;
	}
	return new TreePath(currentNode.getPath());
    }

    public IPropertyFilter getPropertyFilter() {
	return propertyFilter;
    }

}

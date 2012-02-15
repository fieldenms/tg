package ua.com.fielden.platform.domaintree.impl;

import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isCollectionOrInCollectionHierarchy;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.parentCollection;

import java.nio.ByteBuffer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyKeyException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.expression.ExpressionText2ModelConverter;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.TaggingVisitor;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.serialisation.impl.serialisers.TgSimpleSerializer;

/**
 * An {@link ICalculatedProperty} default implementation.
 *
 * @author TG Team
 *
 */
public class CalculatedProperty implements ICalculatedProperty {
    private static final long serialVersionUID = -8413970385471726648L;

    // Required and immutable stuff
    private final Class<?> root;
    private final String contextPath;
    // Required and mutable stuff
    private String contextualExpression, title, desc;
    // Required contextually and mutable stuff
    private CalculatedPropertyAttribute attribute; // enabled only for COLLECTIONAL_EXPRESSION category of property and mutates category to ATTRIBUTED_COLLECTIONAL_EXPRESSION
    private String originationProperty; // required only for AGGREGATED_EXPRESSIONs which represent Totals and should be assigned to some "original" property
    // Inferred stuff
    private final transient Class<?> contextType;
    private transient CalculatedPropertyCategory category;
    private transient String name, path;
    private transient Class<?> parentType;
    private transient Class<?> resultType;

    private transient AstNode ast;

    /**
     * Full featured constructor with all required properties (root, contextPath, contextualExpression, title, desc) and some not required (attribute, originationProperty).
     *
     * @param root
     * @param contextPath
     * @param contextualExpression
     * @param title
     * @param desc
     * @param attribute
     * @param originationProperty
     */
    public CalculatedProperty(final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty) {
	DomainTreeEnhancer.validatePath(root, contextPath);

	this.root = root;
	this.contextPath = contextPath;
	this.contextType = determineType(this.contextPath);

	this.setTitle(title);
	this.setDesc(desc);
	this.setContextualExpression(contextualExpression);

	this.setAttribute(attribute);
	this.setOriginationProperty(originationProperty);
    }

    protected Class<?> determineType(final String path) {
	return StringUtils.isEmpty(path) ? this.root : PropertyTypeDeterminator.determinePropertyType(this.root, path);
    }

    @Override
    public Class<?> root() {
	return root;
    }

    @Override
    public String contextPath() {
        return contextPath;
    }

    @Override
    public String contextualExpression() {
	return contextualExpression;
    }

    @Override
    public ICalculatedProperty setContextualExpression(final String contextualExpression) {
	final ExpressionText2ModelConverter et2mc = new ExpressionText2ModelConverter((Class<? extends AbstractEntity>) this.contextType, contextualExpression /* TODO "root" and "contextPath" are essential to build correct model */);
	try {
	    ast = et2mc.convert();
	} catch (final Exception ex) {
	    ast = null;
	    ex.printStackTrace();
	    throw new Result(ex);
	}
	// TODO uncomment
//	if (TaggingVisitor.ABOVE.equals(ast.getTag())) {
//	    if (AbstractDomainTreeRepresentation.isCollectionOrInCollectionHierarchy(this.root, this.contextPath)) {
//		throw new Result(new IllegalStateException("Aggregated collections are currently unsupported. Please try to use simple expressions under collections (or with ALL / ANY attributes)."));
//	    }
//	}
	this.contextualExpression = contextualExpression;
	inferMetaInformationFromExpression();
	return this;
    }

    /**
     * Inferres all needed information from {@link #contextualExpression} and {@link #attribute}.
     */
    protected void inferMetaInformationFromExpression() {
	if (ast != null) {
	    this.resultType = ast.getType();
	    final boolean collectionOrInCollectionHierarchy = isCollectionOrInCollectionHierarchy(this.root, this.contextPath);
	    final String masterPath = collectionOrInCollectionHierarchy ? parentCollection(this.root, this.contextPath) : "";
	    if (collectionOrInCollectionHierarchy) { // collectional hierarchy
		// final String collectionPath = AbstractDomainTreeRepresentation.parentCollection(this.root, this.contextPath);
		if (TaggingVisitor.THIS.equals(ast.getTag())) {
		    if (CalculatedPropertyAttribute.ALL.equals(this.attribute) || CalculatedPropertyAttribute.ANY.equals(this.attribute)) { // ALL / ANY -- above
			this.category = CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION;
			this.path = above(masterPath); // the level above except for root level -- ""
		    } else {
			this.category = CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION;
			this.path = this.contextPath;
		    }
		} else if (TaggingVisitor.ABOVE.equals(ast.getTag())) {
		    this.category = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION;
		    this.path = above(masterPath); // the level above except for root level -- ""
		    resetAttribute();
		}
	    } else { // simple hierarchy
		// final String masterPath = ""; // simple hierarchy
		if (TaggingVisitor.THIS.equals(ast.getTag())) {
		    this.category = CalculatedPropertyCategory.EXPRESSION;
		    this.path = this.contextPath;
		} else if (TaggingVisitor.ABOVE.equals(ast.getTag())) {
		    this.category = CalculatedPropertyCategory.AGGREGATED_EXPRESSION;
		    this.path = above(masterPath); // the level above except for root level -- ""
		}
		resetAttribute();
	    }
	    this.parentType = determineType(this.path);
	    // resets title to ensure that title / name of property is unique in potentially another parentType
	    setTitle(title());
	}
    }

    /**
     * Resets attribute to default NO_ATTR value for expressions to which the attributes are not applicable (they are applicable only to COLLECTIONAL_EXPRESSIONs).
     */
    protected void resetAttribute() {
	this.attribute = CalculatedPropertyAttribute.NO_ATTR; //
    }

    /**
     * Provides an ABOVE path for the specified <code>contextPath</code>. For the root <code>contextPath</code> it returns the <code>contextPath</code> itself.
     *
     * @param contextPath
     * @return
     */
    protected static String above(final String contextPath) {
	return PropertyTypeDeterminator.isDotNotation(contextPath) ? PropertyTypeDeterminator.penultAndLast(contextPath).getKey() : "";
    }

    @Override
    public String title() {
	return title;
    }

    @Override
    public ICalculatedProperty setTitle(final String title) {
	if (StringUtils.isEmpty(title)) {
	    throw new IncorrectCalcPropertyKeyException("The title of calculated property can not be empty.");
	}
	this.title = title;
	this.name = generateNameFrom(this.title);
	return this;
    }

    /**
     * Generates a name of new property from a title by removing all non-word characters and capitalising all words except first.
     *
     * @param title
     * @return
     */
    protected static String generateNameFrom(final String title) {
	return StringUtils.uncapitalize(WordUtils.capitalize(title.trim()).replaceAll("\\W", ""));
    }

    @Override
    public String desc() {
	return desc;
    }

    @Override
    public ICalculatedProperty setDesc(final String desc) {
	this.desc = desc;
	return this;
    }

    @Override
    public CalculatedPropertyAttribute attribute() {
        return this.attribute;
    }

    @Override
    public ICalculatedProperty setAttribute(final CalculatedPropertyAttribute attribute) {
	if (attribute == null) {
	    throw new IllegalArgumentException("The attribute can not be null.");
	}
	if (!CalculatedPropertyAttribute.NO_ATTR.equals(attribute) && this.category != null && !CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION.equals(this.category)) {
	    throw new Result(new IllegalArgumentException("ALL / ANY attribute can not be applied to non-collectional sub-property [" + this.contextualExpression + "]."));
	}

	this.attribute = attribute;
	inferMetaInformationFromExpression();
        return this;
    }

    @Override
    public String originationProperty() {
	return originationProperty;
    }

    @Override
    public ICalculatedProperty setOriginationProperty(final String originationProperty) {
	// TODO check if the "originationProperty" is correct in context of "contextType"
	this.originationProperty = originationProperty;

        return this;
    }

    @Override
    public ICalculatedProperty.CalculatedPropertyCategory category() {
	return category;
    }

    @Override
    public String name() {
	return name;
    }

    @Override
    public String path() {
	return path;
    }

    @Override
    public String pathAndName() {
	return "".equals(path()) ? name() : path() + "." + name();
    }

    @Override
    public Class<?> contextType() {
	return contextType;
    }

    @Override
    public Class<?> parentType() {
        return parentType;
    }

    @Override
    public Class<?> resultType() {
	return resultType;
    }

    @Override
    public String toString() {
	return "CalculatedProperty [root=" + root.getSimpleName() + ", contextPath=" + contextPath + ", contextualExpression=" + contextualExpression + ", title=" + title + ", desc=" + desc
		+ ", attribute=" + attribute + ", originationProperty=" + originationProperty + ", contextType=" + contextType + ", category=" + category + ", name=" + name
		+ ", path=" + path + ", parentType=" + parentType + ", resultType=" + resultType + ", ast=" + ast + "]";
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
	result = prime * result + ((contextPath == null) ? 0 : contextPath.hashCode());
	result = prime * result + ((contextualExpression == null) ? 0 : contextualExpression.hashCode());
	result = prime * result + ((desc == null) ? 0 : desc.hashCode());
	result = prime * result + ((originationProperty == null) ? 0 : originationProperty.hashCode());
	result = prime * result + ((root == null) ? 0 : root.hashCode());
	result = prime * result + ((title == null) ? 0 : title.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	final CalculatedProperty other = (CalculatedProperty) obj;
	if (attribute != other.attribute)
	    return false;
	if (contextPath == null) {
	    if (other.contextPath != null)
		return false;
	} else if (!contextPath.equals(other.contextPath))
	    return false;
	if (contextualExpression == null) {
	    if (other.contextualExpression != null)
		return false;
	} else if (!contextualExpression.equals(other.contextualExpression))
	    return false;
	if (desc == null) {
	    if (other.desc != null)
		return false;
	} else if (!desc.equals(other.desc))
	    return false;
	if (originationProperty == null) {
	    if (other.originationProperty != null)
		return false;
	} else if (!originationProperty.equals(other.originationProperty))
	    return false;
	if (root == null) {
	    if (other.root != null)
		return false;
	} else if (!root.equals(other.root))
	    return false;
	if (title == null) {
	    if (other.title != null)
		return false;
	} else if (!title.equals(other.title))
	    return false;
	return true;
    }

    /**
     * A specific Kryo serialiser for {@link CalculatedProperty}.
     *
     * @author TG Team
     *
     */
    public static class CalculatedPropertySerialiser extends TgSimpleSerializer<CalculatedProperty> {
	public CalculatedPropertySerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public CalculatedProperty read(final ByteBuffer buffer) {
	    final Class<?> root = readValue(buffer, Class.class);
	    final String contextPath = readValue(buffer, String.class);
	    final String contextualExpression = readValue(buffer, String.class);
	    final String title = readValue(buffer, String.class);
	    final String desc = readValue(buffer, String.class);
	    final CalculatedPropertyAttribute attribute = readValue(buffer, CalculatedPropertyAttribute.class);
	    final String originationProperty = readValue(buffer, String.class);
	    return new CalculatedProperty(root, contextPath, contextualExpression, title, desc, attribute, originationProperty);
	}

	@Override
	public void write(final ByteBuffer buffer, final CalculatedProperty calculatedProperty) {
	    writeValue(buffer, calculatedProperty.root);
	    writeValue(buffer, calculatedProperty.contextPath);
	    writeValue(buffer, calculatedProperty.contextualExpression);
	    writeValue(buffer, calculatedProperty.title);
	    writeValue(buffer, calculatedProperty.desc);
	    writeValue(buffer, calculatedProperty.attribute);
	    writeValue(buffer, calculatedProperty.originationProperty);
	}
    }
}
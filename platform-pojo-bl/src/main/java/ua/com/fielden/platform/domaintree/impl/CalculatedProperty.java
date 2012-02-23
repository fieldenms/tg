package ua.com.fielden.platform.domaintree.impl;

import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isCollectionOrInCollectionHierarchy;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.parentCollection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.expression.ExpressionText2ModelConverter;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.LevelAllocatingVisitor;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.ClassComparator;

/**
 * An {@link ICalculatedProperty} default implementation, which can be binded to Expression Editor.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@EntityTitle(value = "Calculated property", desc = "<i>Calculated property</i> entity")
@DescTitle(value = "Description", desc= "Calculated property description")
public class CalculatedProperty extends AbstractEntity<DynamicEntityKey> implements ICalculatedProperty {
    private static final long serialVersionUID = -8413970385471726648L;

    // Required and immutable stuff
    @IsProperty
    @CompositeKeyMember(1)
    @Title(value = "Root type", desc = "A higher order type")
    @Readonly
    @Invisible
    private /* final */ Class<?> root;

    @IsProperty
    @CompositeKeyMember(2)
    @Title(value = "Context path", desc = "A path to the calculated property context")
    @Readonly
    @Invisible
    @AfterChange(AceCalculatedPropertyContextTypePopulation.class)
    private /* final */ String contextPath;

    // Required and mutable stuff
    @IsProperty
    @CompositeKeyMember(3)
    @Title(value = "Expression", desc = "Property evaluation formula")
    @BeforeChange(@Handler(BceContextualExpressionValidation.class))
    @AfterChange(AceCalculatedPropertyMetaInformationPopulation.class)
    private String contextualExpression;

    @IsProperty
    @CompositeKeyMember(4)
    @Title(value = "Title", desc = "Calculated property title")
    @BeforeChange(@Handler(BceTitleValidation.class))
    @AfterChange(AceCalculatedPropertyNamePopulation.class)
    private String title;

    // Required contextually and mutable stuff
    @IsProperty
    @CompositeKeyMember(5)
    @Title(value = "Attribute", desc = "Calculated property attribute (ALL or ANY for collectional expressions)")
    @BeforeChange(@Handler(BceAttributeValidation.class))
    @AfterChange(AceCalculatedPropertyMetaInformationPopulation.class)
    private CalculatedPropertyAttribute attribute = CalculatedPropertyAttribute.NO_ATTR; // enabled only for COLLECTIONAL_EXPRESSION category of property and mutates category to ATTRIBUTED_COLLECTIONAL_EXPRESSION

    @IsProperty
    @CompositeKeyMember(6)
    @Title(value = "Origination property", desc = "A property from which this calculated property has been originated.")
    @BeforeChange(@Handler(BceOriginationPropertyValidation.class))
    private String originationProperty; // required only for AGGREGATED_EXPRESSIONs which represent Totals and should be assigned to some "original" property

    // Inferred stuff
    private /* final */ transient Class<?> contextType;
    private transient CalculatedPropertyCategory category;
    private transient String name, path;
    private transient Class<?> parentType;
    private transient Class<?> resultType;

    private transient AstNode ast;

    private /* final */ transient IDomainTreeEnhancer enhancer;

    /**
     * Default constructor.
     */
    protected CalculatedProperty() {
	final DynamicEntityKey key = new DynamicEntityKey(this);
	key.addKeyMemberComparator(1, new ClassComparator());
	setKey(key);
    }

    protected Class<?> determineType(final String path) {
	return StringUtils.isEmpty(path) ? this.root : PropertyTypeDeterminator.determinePropertyType(this.root, path);
    }

    @Override
    public Class<?> getRoot() {
	return root;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public String getContextualExpression() {
	return contextualExpression;
    }

    @Override
    @Observable
    public CalculatedProperty setDesc(final String desc) {
        return (CalculatedProperty) super.setDesc(desc);
    }

    @Override
    @Observable
    public ICalculatedProperty setContextualExpression(final String contextualExpression) {
	this.contextualExpression = contextualExpression;
	return this;
    }

    /**
     * Inferres all needed information from {@link #contextualExpression} and {@link #attribute}.
     */
    protected void inferMetaInformationFromExpression() {
	if (ast == null) {
	    try {
		initAst(contextualExpression);
	    } catch (final Exception e) {
		e.printStackTrace();
		throw new Result("The calculated property correctness should be verified earlier! Please check validation logic..", e);
	    }
	}
	this.resultType = ast.getType();

	final int contextPathLevel = StringUtils.isEmpty(contextPath) ? 1 : new LevelAllocatingVisitor((Class<? extends AbstractEntity>) getRoot(), contextPath).determineLevelForProperty(""); // new ExpressionText2ModelConverter((Class<? extends AbstractEntity>) getRoot(), "", contextPath).convert().getLevel();
	final int level = ast.getLevel();
	final int levelsToRaiseTheProperty = contextPathLevel - level;

	final boolean collectionOrInCollectionHierarchy = isCollectionOrInCollectionHierarchy(this.root, this.contextPath);
	final String masterPath = collectionOrInCollectionHierarchy ? parentCollection(this.root, this.contextPath) : "";
	if (collectionOrInCollectionHierarchy) { // collectional hierarchy
	    if (levelsToRaiseTheProperty == 0) {
		if (isAttributed()) { // 0 level attributed
		    this.category = CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION;
		    this.path = above(masterPath); // the level above except for root level -- ""
		} else { // 0 level
		    this.category = CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION;
		    this.path = this.contextPath;
		}
	    } else if (levelsToRaiseTheProperty == 1) {
		this.category = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION;
		this.path = above(masterPath); // the level above except for root level -- ""
	    } else {
		throw new Result(new Exception("Currently raising to level > 1 not supported!!! levelsToRaiseTheProperty == " + levelsToRaiseTheProperty + " level == " + level + " contextPathLevel == " + contextPathLevel));
	    }
	} else { // simple hierarchy
	    if (levelsToRaiseTheProperty == 0) {
		this.category = CalculatedPropertyCategory.EXPRESSION;
		this.path = this.contextPath;
	    } else if (levelsToRaiseTheProperty == 1) {
		this.category = CalculatedPropertyCategory.AGGREGATED_EXPRESSION;
		this.path = above(masterPath); // the level above except for root level -- ""
	    } else {
		throw new Result(new Exception("The level above root does not exist!!! levelsToRaiseTheProperty == " + levelsToRaiseTheProperty + " level == " + level + " contextPathLevel == " + contextPathLevel));
	    }
	}
	this.parentType = determineType(this.path);

	// reset attribute non-ATTRIBUTED_COLLECTIONAL_EXPRESSIONs
	if (!CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION.equals(this.category)) {
	    resetAttribute();
	}
	// make attribute enabled for COLLECTIONAL_EXPRESSIONs and ATTRIBUTED_COLLECTIONAL_EXPRESSIONs (TODO make a test)
	getProperty("attribute").setEditable(CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION.equals(this.category) || CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION.equals(this.category));

	// resets title to ensure that title / name of property is unique in potentially another parentType
	setTitle(getTitle()); // TODO re-implement through "dependent properties"
	// make originationProperty required for AGGREGATION_EXPRESSIONs
	getProperty("originationProperty").setRequired(CalculatedPropertyCategory.AGGREGATED_EXPRESSION.equals(this.category));
    }

    protected boolean isAttributed() {
	return CalculatedPropertyAttribute.ALL.equals(this.attribute) || CalculatedPropertyAttribute.ANY.equals(this.attribute);
    }

    /**
     * Resets attribute to default NO_ATTR value for expressions to which the attributes are not applicable (they are applicable only to COLLECTIONAL_EXPRESSIONs).
     */
    protected void resetAttribute() {
	if (!CalculatedPropertyAttribute.NO_ATTR.equals(getAttribute())) {
	    setAttribute(CalculatedPropertyAttribute.NO_ATTR);
	}
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
    public String getTitle() {
	return title;
    }

    @Override
    @Observable
    public ICalculatedProperty setTitle(final String title) {
	this.title = title;
	return this;
    }

    /**
     * Generates a name of new property from a title by removing all non-word characters, removing starting digits (if exist) and capitalising all words except first.
     *
     * @param title
     * @return
     */
    protected static String generateNameFrom(final String title) {
	return StringUtils.uncapitalize(WordUtils.capitalize(title.trim()).replaceAll("\\W", "").replaceFirst("\\d*", ""));
    }

    @Override
    public CalculatedPropertyAttribute getAttribute() {
        return this.attribute;
    }

    @Override
    @Observable
    public ICalculatedProperty setAttribute(final CalculatedPropertyAttribute attribute) {
	this.attribute = attribute;
        return this;
    }

    @Override
    public String getOriginationProperty() {
	return originationProperty;
    }

    @Override
    @Observable
    public ICalculatedProperty setOriginationProperty(final String originationProperty) {
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

//
//    @Override
//    public int hashCode() {
//	final int prime = 31;
//	int result = 1;
//	result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
//	result = prime * result + ((contextPath == null) ? 0 : contextPath.hashCode());
//	result = prime * result + ((contextualExpression == null) ? 0 : contextualExpression.hashCode());
//	result = prime * result + ((desc == null) ? 0 : desc.hashCode());
//	result = prime * result + ((originationProperty == null) ? 0 : originationProperty.hashCode());
//	result = prime * result + ((root == null) ? 0 : root.hashCode());
//	result = prime * result + ((title == null) ? 0 : title.hashCode());
//	return result;
//    }
//
//    @Override
//    public boolean equals(final Object obj) {
//	if (this == obj)
//	    return true;
//	if (obj == null)
//	    return false;
//	if (getClass() != obj.getClass())
//	    return false;
//	final CalculatedProperty other = (CalculatedProperty) obj;
//	if (attribute != other.attribute)
//	    return false;
//	if (contextPath == null) {
//	    if (other.contextPath != null)
//		return false;
//	} else if (!contextPath.equals(other.contextPath))
//	    return false;
//	if (contextualExpression == null) {
//	    if (other.contextualExpression != null)
//		return false;
//	} else if (!contextualExpression.equals(other.contextualExpression))
//	    return false;
//	if (desc == null) {
//	    if (other.desc != null)
//		return false;
//	} else if (!desc.equals(other.desc))
//	    return false;
//	if (originationProperty == null) {
//	    if (other.originationProperty != null)
//		return false;
//	} else if (!originationProperty.equals(other.originationProperty))
//	    return false;
//	if (root == null) {
//	    if (other.root != null)
//		return false;
//	} else if (!root.equals(other.root))
//	    return false;
//	if (title == null) {
//	    if (other.title != null)
//		return false;
//	} else if (!title.equals(other.title))
//	    return false;
//	return true;
//    }

//    /**
//     * A specific Kryo serialiser for {@link CalculatedProperty}.
//     *
//     * @author TG Team
//     *
//     */
//    public static class CalculatedPropertySerialiser extends TgSimpleSerializer<CalculatedProperty> {
//	public CalculatedPropertySerialiser(final TgKryo kryo) {
//	    super(kryo);
//	}
//
//	@Override
//	public CalculatedProperty read(final ByteBuffer buffer) {
//	    final Class<?> root = readValue(buffer, Class.class);
//	    final String contextPath = readValue(buffer, String.class);
//	    final String contextualExpression = readValue(buffer, String.class);
//	    final String title = readValue(buffer, String.class);
//	    final String desc = readValue(buffer, String.class);
//	    final CalculatedPropertyAttribute attribute = readValue(buffer, CalculatedPropertyAttribute.class);
//	    final String originationProperty = readValue(buffer, String.class);
//	    return new CalculatedProperty(root, contextPath, contextualExpression, title, desc, attribute, originationProperty, dtm().getEnhancer());
//	}
//
//	@Override
//	public void write(final ByteBuffer buffer, final CalculatedProperty calculatedProperty) {
//	    writeValue(buffer, calculatedProperty.root);
//	    writeValue(buffer, calculatedProperty.contextPath);
//	    writeValue(buffer, calculatedProperty.contextualExpression);
//	    writeValue(buffer, calculatedProperty.title);
//	    writeValue(buffer, calculatedProperty.getDesc());
//	    writeValue(buffer, calculatedProperty.attribute);
//	    writeValue(buffer, calculatedProperty.originationProperty);
//	}
//    }

    // Fictive setters for actually "immutable" root and contextPath
    @Observable
    public void setRoot(final Class<?> root) {
        this.root = root;
    }

    @Observable
    public void setContextPath(final String contextPath) {
        this.contextPath = contextPath;
        // moved to definer => this.contextType = determineType(this.contextPath);
    }

    public static CalculatedProperty create(final EntityFactory factory, final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty, final IDomainTreeEnhancer domainTreeEnhancer) {
	DomainTreeEnhancer.validatePath(root, contextPath, "The context path [" + contextPath + "] in type [" + root.getSimpleName() + "] of calculated property does not exist.");
        final CalculatedProperty calc = factory.newByKey(CalculatedProperty.class, root, contextPath, contextualExpression, title, attribute, originationProperty);
        calc.setDesc(desc);
        calc.setEnhancer(domainTreeEnhancer);
        return calc;
    }

    public IDomainTreeEnhancer getEnhancer() {
        return enhancer;
    }

    public void setEnhancer(final IDomainTreeEnhancer enhancer) {
        this.enhancer = enhancer;
    }

    public void setName(final String name) {
	this.name = name;
    }

    public void setContextType(final Class<?> contextType) {
	this.contextType = contextType;
    }

    public AstNode getAst() {
	return ast;
    }

    public void initAst(final String newContextualExpression) throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter et2mc = new ExpressionText2ModelConverter((Class<? extends AbstractEntity>) getRoot(), contextPath, newContextualExpression);
	this.ast = et2mc.convert();
    }
}
package ua.com.fielden.platform.domaintree.impl;

import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isCollectionOrInCollectionHierarchy;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.parentCollection;

import java.nio.ByteBuffer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyKeyException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.Dependent;
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
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.serialisation.impl.serialisers.TgSimpleSerializer;
import ua.com.fielden.platform.utils.ClassComparator;
import ua.com.fielden.platform.utils.EntityUtils;

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
    protected static final String BULLSHIT = "BULLSHIT";

    // Required and immutable stuff
    @IsProperty
    @CompositeKeyMember(1)
    @Title(value = "Root type", desc = "A higher order type")
    @Readonly
    @Invisible
    @BeforeChange(@Handler(BceRootValidation.class))
    private /* final */ Class<?> root = Class.class;

    @IsProperty
    @CompositeKeyMember(2)
    @Title(value = "Context path", desc = "A path to the calculated property context")
    @Readonly
    @Invisible
    @BeforeChange(@Handler(BceContextPathValidation.class))
    @AfterChange(AceCalculatedPropertyContextTypePopulation.class)
    private /* final */ String contextPath = BULLSHIT;

    // Required and mutable stuff
    @IsProperty
    @CompositeKeyMember(3)
    @Title(value = "Expression", desc = "Property evaluation formula")
    @Dependent({"title", "originationProperty"}) // revalidates "title" to ensure that title / name of property is unique in potentially another parentType (after "contextualExpression" has been changed)
    // revalidates "originationProperty" to ensure that it is correct after "contextualExpression" has been changed
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
    @Dependent("title") // revalidates "title" to ensure that title / name of property is unique in potentially another parentType (after "attribute" has been changed)
    @BeforeChange(@Handler(BceAttributeValidation.class))
    @AfterChange(AceCalculatedPropertyMetaInformationPopulation.class)
    private CalculatedPropertyAttribute attribute = CalculatedPropertyAttribute.NO_ATTR; // enabled only for COLLECTIONAL_EXPRESSION category of property and mutates category to ATTRIBUTED_COLLECTIONAL_EXPRESSION

    @IsProperty
    @CompositeKeyMember(6)
    @Title(value = "Origination property", desc = "A property from which this calculated property has been originated.")
    @BeforeChange(@Handler(BceOriginationPropertyValidation.class))
    private String originationProperty = ""; // required only for AGGREGATED_EXPRESSIONs which represent Totals and should be assigned to some "original" property

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

	final int contextPathLevel = StringUtils.isEmpty(getContextPath()) ? 1 : new LevelAllocatingVisitor((Class<? extends AbstractEntity>) getRoot(), getContextPath()).determineLevelForProperty(""); // new ExpressionText2ModelConverter((Class<? extends AbstractEntity>) getRoot(), "", contextPath).convert().getLevel();
	final int level = ast.getLevel() == null ? contextPathLevel : ast.getLevel();
	final int levelsToRaiseTheProperty = contextPathLevel - level;

	final boolean collectionOrInCollectionHierarchy = isCollectionOrInCollectionHierarchy(this.root, this.getContextPath());
	final String masterPath = collectionOrInCollectionHierarchy ? parentCollection(this.root, this.getContextPath()) : "";
	if (collectionOrInCollectionHierarchy) { // collectional hierarchy
	    if (levelsToRaiseTheProperty == 0) {
		if (isAttributed()) { // 0 level attributed
		    this.category = CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION;
		} else { // 0 level
		    this.category = CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION;
		}
		this.path = this.getContextPath();
	    } else if (levelsToRaiseTheProperty == 1) {
		this.category = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION;
		this.path = above(masterPath); // the level above except for root level -- ""
	    } else {
		// TODO
		throw new Result(new Exception("Currently raising to level > 1 not supported!!! levelsToRaiseTheProperty == " + levelsToRaiseTheProperty + " level == " + level + " contextPathLevel == " + contextPathLevel));
	    }
	} else { // simple hierarchy
	    if (levelsToRaiseTheProperty == 0) {
		this.category = CalculatedPropertyCategory.EXPRESSION;
		this.path = this.getContextPath();
	    } else if (levelsToRaiseTheProperty == 1) {
		this.category = CalculatedPropertyCategory.AGGREGATED_EXPRESSION;
		this.path = above(masterPath); // the level above except for root level -- ""
	    } else {
		// TODO
		throw new Result(new Exception("The level above root does not exist!!! levelsToRaiseTheProperty == " + levelsToRaiseTheProperty + " level == " + level + " contextPathLevel == " + contextPathLevel));
	    }
	}
	this.parentType = determineType(this.path);

	// reset attribute for non-ATTRIBUTED_COLLECTIONAL_EXPRESSIONs
	final boolean isAttributedCategory = CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION.equals(this.category);
	if (!isAttributedCategory) {
	    resetAttribute();
	}
	// make attribute enabled for COLLECTIONAL_EXPRESSIONs and ATTRIBUTED_COLLECTIONAL_EXPRESSIONs
	getProperty("attribute").setEditable(CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION.equals(this.category) || isAttributedCategory);

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
	return pathWith(name());
    }

    public String pathWith(final String name) {
	return path() == null ? null : "".equals(path()) ? name : path() + "." + name;
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

    // Fictive setters for actually "immutable" root and contextPath
    @Observable
    public void setRoot(final Class<?> root) {
        this.root = root;
    }

    @Observable
    public void setContextPath(final String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Creates empty {@link CalculatedProperty} with only necessary {@link #getRoot()} and {@link #getContextPath()} initialised.
     * It is obvious that other required stuff will not be initialised. immediate validation intentionally will not be performed.
     *
     * @param factory
     * @param root
     * @param contextPath
     * @param domainTreeEnhancer
     * @return
     */
    public static CalculatedProperty createEmpty(final EntityFactory factory, final Class<?> root, final String contextPath, final IDomainTreeEnhancer domainTreeEnhancer) {
        final CalculatedProperty calc = factory.newEntity(CalculatedProperty.class);

        // make Root and ContextPath not required -- their non-empty logic has been moved to corresponding Before Change Events
        calc.getProperty("root").setRequired(false);
        calc.getProperty("contextPath").setRequired(false);
	// after the property has fully defined its correct context, the requiredness of originationProperty should be relaxed
	// (originationProperty is required because it is key member). The requiredness of originationProperty depends on the
	// expression category (e.g. for AGGREGATION_PROPERTY it should be required)
	calc.getProperty("originationProperty").setRequired(false);
	// Attribute property should be disabled at beginning. Its enablement will change after CalculatedProperty category changes.
	calc.getProperty("attribute").setEditable(false);

        calc.setRoot(root);
        calc.setContextPath(contextPath);

        calc.setEnhancer(domainTreeEnhancer);
        return calc;
    }

    /**
     * Creates full {@link CalculatedProperty} with all keys initialised and validates it.
     *
     * @param factory
     * @param root
     * @param contextPath
     * @param contextualExpression
     * @param title
     * @param desc
     * @param attribute
     * @param originationProperty
     * @param domainTreeEnhancer
     * @return
     */
    protected static CalculatedProperty create(final EntityFactory factory, final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty, final IDomainTreeEnhancer domainTreeEnhancer) {
        return setImportantStuff(contextualExpression, title, desc, attribute, originationProperty, createEmpty(factory, root, contextPath, domainTreeEnhancer));
    }

    /**
     * Creates full {@link CalculatedProperty} with all keys initialised, validates it and throws validation exception if any.
     *
     * @param factory
     * @param root
     * @param contextPath
     * @param contextualExpression
     * @param title
     * @param desc
     * @param attribute
     * @param originationProperty
     * @param domainTreeEnhancer
     * @return
     */
    public static CalculatedProperty createAndValidate(final EntityFactory factory, final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty, final IDomainTreeEnhancer domainTreeEnhancer) {
	final CalculatedProperty calc = create(factory, root, contextPath, contextualExpression, title, desc, attribute, originationProperty, domainTreeEnhancer);

	if (!calc.isValid().isSuccessful()) {
	    throw calc.isValid();
	}
	if (calc.category() == null) {
	    throw new IncorrectCalcPropertyKeyException("The category of calculated property cannot be null.");
	}

        return calc;
    }

    /**
     * Creates full {@link CalculatedProperty} with all keys initialised without validation.
     * It should be used only when the {@link CalculatedProperty} is surely correct, e.g. after 1) deserialisation 2) extracting from enhanced domain.
     *
     * @param factory
     * @param root
     * @param contextPath
     * @param contextualExpression
     * @param title
     * @param desc
     * @param attribute
     * @param originationProperty
     * @param domainTreeEnhancer
     * @return
     */
    protected static CalculatedProperty createWithoutValidation(final EntityFactory factory, final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty, final IDomainTreeEnhancer domainTreeEnhancer) {
	final CalculatedProperty calc = createEmpty(factory, root, contextPath, domainTreeEnhancer);

        calc.setInitialising(true);
	setImportantStuff(contextualExpression, title, desc, attribute, originationProperty, calc);
        calc.setInitialising(false);
        EntityUtils.handleMetaProperties(calc); // it is important to invoke definers after raw setters (without validation) have been invoked.

        return calc;
    }

    private static CalculatedProperty setImportantStuff(final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty, final CalculatedProperty calc) {
	calc.setContextualExpression(contextualExpression);
        calc.setTitle(title);
        calc.setDesc(desc);
        calc.setAttribute(attribute);
        calc.setOriginationProperty(originationProperty);
        return calc;
    }

    public IDomainTreeEnhancer getEnhancer() {
        return enhancer;
    }

    public void setEnhancer(final IDomainTreeEnhancer enhancer) {
//	if (enhancer == null) {
//	    throw new IncorrectCalcPropertyKeyException("An enhancer of the calculated property cannot be 'null'.");
//	}
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
	final ExpressionText2ModelConverter et2mc = new ExpressionText2ModelConverter((Class<? extends AbstractEntity>) getRoot(), getContextPath(), newContextualExpression);
	this.ast = et2mc.convert();
    }

    /**
     * Root and context path are essential for all other parts of {@link CalculatedProperty}.
     * So this method returns a first failure for these properties or <code>null</code> if there is no failure.
     *
     * @return
     */
    protected IncorrectCalcPropertyKeyException validateRootAndContext() {
	if (!getProperty("root").isValid()) {
	    return (IncorrectCalcPropertyKeyException) getProperty("root").getFirstFailure();
	} else if (!getProperty("contextPath").isValid()) {
	    return (IncorrectCalcPropertyKeyException) getProperty("contextPath").getFirstFailure();
	} else {
	    return null;
	}
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
	    final CalculatedPropertyAttribute attribute = readValue(buffer, CalculatedPropertyAttribute.class);
	    final String originationProperty = readValue(buffer, String.class);
	    final String desc = readValue(buffer, String.class);
	    return CalculatedProperty.create(kryo.factory(), root, contextPath, contextualExpression, title, desc, attribute, originationProperty, /* domainTreeEnhancer should be initialised later!*/ null);
	}

	@Override
	public void write(final ByteBuffer buffer, final CalculatedProperty cp) {
	    writeValue(buffer, cp.root);
	    writeValue(buffer, cp.contextPath);
	    writeValue(buffer, cp.contextualExpression);
	    writeValue(buffer, cp.title);
	    writeValue(buffer, cp.attribute);
	    writeValue(buffer, cp.originationProperty);
	    writeValue(buffer, cp.getDesc());
	}
    }

    protected CalculatedProperty copy(final ISerialiser serialiser) {
	final CalculatedProperty copy = EntityUtils.deepCopy(this, serialiser);
	copy.setEnhancer(enhancer);
	copy.isValid();
	return copy;
    }
}
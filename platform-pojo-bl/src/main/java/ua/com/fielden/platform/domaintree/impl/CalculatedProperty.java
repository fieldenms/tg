package ua.com.fielden.platform.domaintree.impl;

import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.ALL;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.ANY;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.NO_ATTR;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.AGGREGATED_EXPRESSION;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.COLLECTIONAL_EXPRESSION;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.EXPRESSION;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isCollectionOrInCollectionHierarchy;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.parentCollection;

import java.lang.reflect.Field;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.CalcPropertyWarning;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer.IncorrectCalcPropertyException;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManagerAndEnhancer.DomainTreeEnhancerWithPropertiesPopulation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
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
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.expression.ExpressionText2ModelConverter;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.LevelAllocatingVisitor;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.utils.ClassComparator;

/**
 * The only {@link ICalculatedProperty} implementation, which can be binded to Expression Editor.
 * 
 * @author TG Team
 * 
 */
@KeyType(DynamicEntityKey.class)
@EntityTitle(value = "Calculated property", desc = "<i>Calculated property</i> entity")
@DescTitle(value = "Description", desc = "Calculated property description")
public/* final */class CalculatedProperty extends AbstractEntity<DynamicEntityKey> implements ICalculatedProperty {
    private static final long serialVersionUID = -8413970385471726648L;

    // Required and immutable stuff
    @IsProperty
    @CompositeKeyMember(1)
    @Title(value = "Root type", desc = "A higher order type that contains calculated property (not enhanced)")
    @Readonly
    @Invisible
    private/* final */Class<?> root;

    @IsProperty
    @CompositeKeyMember(2)
    @Title(value = "Context path", desc = "A path to the calculated property context")
    @Readonly
    @Invisible
    @AfterChange(AceCalculatedPropertyContextTypePopulation.class)
    private/* final */String contextPath;

    // Required and mutable stuff
    @IsProperty
    @CompositeKeyMember(3)
    @Title(value = "Expression", desc = "Property evaluation formula")
    @Dependent({ "title", "originationProperty" })
    // revalidates "title" to ensure that title / name of property is unique in potentially another parentType (after "contextualExpression" has been changed)
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
    @Dependent("title")
    // revalidates "title" to ensure that title / name of property is unique in potentially another parentType (after "attribute" has been changed)
    @BeforeChange(@Handler(BceAttributeValidation.class))
    @AfterChange(AceCalculatedPropertyMetaInformationPopulation.class)
    private CalculatedPropertyAttribute attribute; // enabled only for COLLECTIONAL_EXPRESSION category of property and mutates category to ATTRIBUTED_COLLECTIONAL_EXPRESSION

    @IsProperty
    @CompositeKeyMember(6)
    @Title(value = "Origination property", desc = "A property from which this calculated property has been originated.")
    @BeforeChange(@Handler(BceOriginationPropertyValidation.class))
    private String originationProperty; // required only for AGGREGATED_EXPRESSIONs which represent Totals and should be assigned to some "original" property

    // Inferred stuff
    private/* final */transient Class<?> contextType;
    private transient CalculatedPropertyCategory category;
    private transient String name, path;
    private transient Class<?> parentType;
    private transient Class<?> resultType;

    private transient AstNode ast;

    private/* final */transient IDomainTreeEnhancer enhancer;
    private transient boolean validateTitleContextOfExtractedProperties;

    /**
     * Default constructor.
     */
    protected CalculatedProperty() {
        final DynamicEntityKey key = new DynamicEntityKey(this);
        key.addKeyMemberComparator(1, new ClassComparator());
        setKey(key);
    }

    private Class<?> determineType(final String path) {
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
     * Inferred all needed information from {@link #contextualExpression} and {@link #attribute}.
     */
    protected void inferMetaInformation() {
        try {
            this.ast = createAst(contextualExpression);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new Result("The calculated property correctness should be verified earlier! Please check validation logic..", e);
        }
        this.resultType = ast.getType();

        final int levelsToRaiseTheProperty = levelsToRaiseTheProperty(getRoot(), getContextPath(), ast.getLevel());

        final boolean collectionOrInCollectionHierarchy = isCollectionOrInCollectionHierarchy(this.root, this.getContextPath());
        final String masterPath = collectionOrInCollectionHierarchy ? parentCollection(this.root, this.getContextPath()) : "";
        if (collectionOrInCollectionHierarchy) { // collectional hierarchy
            if (levelsToRaiseTheProperty == 0) {
                if (isAttributed()) { // 0 level attributed
                    this.category = ATTRIBUTED_COLLECTIONAL_EXPRESSION;
                } else { // 0 level
                    this.category = COLLECTIONAL_EXPRESSION;
                }
                this.path = this.getContextPath();
            } else if (levelsToRaiseTheProperty == 1) {
                this.category = AGGREGATED_COLLECTIONAL_EXPRESSION;
                this.path = above(masterPath); // the level above except for root level -- ""
            } else {
                // TODO
                throw new Result(new Exception("Currently raising to level > 1 not supported!!! levelsToRaiseTheProperty == " + levelsToRaiseTheProperty)); // + " level == " + level + " contextPathLevel == " + contextPathLevel));
            }
        } else { // simple hierarchy
            if (levelsToRaiseTheProperty == 0) {
                this.category = EXPRESSION;
                this.path = this.getContextPath();
            } else if (levelsToRaiseTheProperty == 1) {
                this.category = AGGREGATED_EXPRESSION;
                this.path = above(masterPath); // the level above except for root level -- ""
            } else {
                // TODO
                throw new Result(new Exception("The level above root does not exist!!! levelsToRaiseTheProperty == " + levelsToRaiseTheProperty)); // + " level == " + level + " contextPathLevel == " + contextPathLevel));
            }
        }
        this.parentType = determineType(this.path);

        // reset attribute for non-ATTRIBUTED_COLLECTIONAL_EXPRESSIONs
        final boolean isAttributedCategory = ATTRIBUTED_COLLECTIONAL_EXPRESSION.equals(this.category);
        if (!isAttributedCategory) {
            resetAttribute();
        }
        // make attribute enabled for COLLECTIONAL_EXPRESSIONs and ATTRIBUTED_COLLECTIONAL_EXPRESSIONs
        getProperty("attribute").setEditable(COLLECTIONAL_EXPRESSION.equals(this.category) || isAttributedCategory);

        // make originationProperty required for AGGREGATION_EXPRESSIONs
        getProperty("originationProperty").setRequired(AGGREGATED_EXPRESSION.equals(this.category));
    }

    @Override
    public ExpressionModel getExpressionModel() {
        return ast == null ? null : ast.getModel();
    }

    protected static int levelsToRaiseTheProperty(final Class<?> root, final String contextPath, final Integer expressionLevel) {
        final int contextPathLevel = new LevelAllocatingVisitor((Class<? extends AbstractEntity>) root, contextPath).getContextLevel();
        final int level = expressionLevel == null ? contextPathLevel : expressionLevel;
        return contextPathLevel - level;
    }

    protected boolean isAttributed() {
        return ALL.equals(this.attribute) || ANY.equals(this.attribute);
    }

    /**
     * Resets attribute to default NO_ATTR value for expressions to which the attributes are not applicable (they are applicable only to COLLECTIONAL_EXPRESSIONs).
     */
    protected void resetAttribute() {
        if (!NO_ATTR.equals(getAttribute())) {
            setAttribute(NO_ATTR);
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
    public static String generateNameFrom(final String title) {
        return StringUtils.uncapitalize(WordUtils.capitalize(title.trim()).
        /*remove non-words, but keep digits and underscore*/replaceAll("[^\\p{L}\\d_]", "").
        /*remove the digit at the beginning of the word*/replaceFirst("\\d*", ""));
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
        return path() == null ? null : name == null ? null : "".equals(path()) ? name : path() + "." + name;
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
    public CalculatedProperty setRoot(final Class<?> root) {
        this.root = root;
        return this;
    }

    @Observable
    public CalculatedProperty setContextPath(final String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    /**
     * Creates empty {@link CalculatedProperty} with only necessary {@link #getRoot()} and {@link #getContextPath()} initialised. It is obvious that other required stuff will not
     * be initialised. Immediate validation intentionally will not be performed.
     * 
     * @param factory
     * @param root
     * @param contextPath
     * @param domainTreeEnhancer
     * @return
     */
    public static CalculatedProperty createEmpty(final EntityFactory factory, final Class<?> root, final String contextPath, final IDomainTreeEnhancer domainTreeEnhancer) {
        return createEmpty(factory, root, contextPath, domainTreeEnhancer, true);
    }

    /**
     * Creates empty {@link CalculatedProperty} with only necessary {@link #getRoot()} and {@link #getContextPath()} initialised. It is obvious that other required stuff will not
     * be initialised. Immediate validation intentionally will not be performed.
     * 
     * @param factory
     * @param root
     * @param contextPath
     * @param domainTreeEnhancer
     * @return
     */
    private static CalculatedProperty createEmpty(final EntityFactory factory, final Class<?> root, final String contextPath, final IDomainTreeEnhancer domainTreeEnhancer, final boolean validateTitleContextOfExtractedProperties) {
        if (factory == null) {
            throw new IncorrectCalcPropertyException("Calculated property cannot be created using 'null' entity factory.");
        }
        final CalculatedProperty calc = factory.newEntity(CalculatedProperty.class);
        calc.validateTitleContextOfExtractedProperties = validateTitleContextOfExtractedProperties;

        // Enhancer:
        if (domainTreeEnhancer == null) {
            throw new IncorrectCalcPropertyException("Domain Tree Enhancer is essential part of calculated property. It can not be 'null'.");
        }
        calc.setEnhancer(domainTreeEnhancer);

        // Root:
        validateRoot(calc.getEnhancer(), root); // here immediate validation will be performed and exception will be thrown if smth. goes wrong
        calc.setRoot(root).validateAndThrow("root");

        // ContextPath:
        validateContextPath(calc, contextPath);
        calc.getProperty("contextPath").setRequired(false); // make ContextPath not required -- due to legality of "" contextPath, however 'null' is not correct
        calc.setContextPath(contextPath).validateAndThrow("contextPath");

        // Attribute:
        calc.attribute = NO_ATTR; // no validation and Ace kicking will be performed
        calc.validateAndThrow("attribute");
        calc.getProperty("attribute").setRequired(false); // the requiredness should be relaxed after proper check of non-emptiness for "attribute"
        calc.getProperty("attribute").setEditable(false); // attribute should be disabled at beginning. Its enablement will change after CalculatedProperty category changes.

        // after the property has fully defined its correct context, the requiredness of originationProperty should be relaxed
        // (originationProperty is required because it is key member). The requiredness of originationProperty depends on the
        // expression category (e.g. for AGGREGATION_PROPERTY it should be required)
        calc.getProperty("originationProperty").setRequired(false);
        return calc;
    }

    private void validateAndThrow(final String property) {
        if (!getProperty(property).isValidWithRequiredCheck()) {
            throw getProperty(property).getFirstFailure();
        }
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
    private static CalculatedProperty create(final EntityFactory factory, final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty, final IDomainTreeEnhancer domainTreeEnhancer, final boolean validateTitleContextOfExtractedProperties) {
        return setImportantStuff(contextualExpression, title, desc, attribute, originationProperty, createEmpty(factory, root, contextPath, domainTreeEnhancer, validateTitleContextOfExtractedProperties));
    }

    /**
     * Creates full {@link CalculatedProperty} with all keys initialised and validates it.
     * 
     * @return
     */
    private static CalculatedProperty create(final EntityFactory factory, final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty, final IDomainTreeEnhancer domainTreeEnhancer) {
        return create(factory, root, contextPath, contextualExpression, title, desc, attribute, originationProperty, domainTreeEnhancer, true);
    }

    /**
     * Creates full {@link CalculatedProperty} with all keys initialised, validates it and throws validation exception if any.
     */
    protected final static CalculatedProperty createCorrect(final EntityFactory factory, final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty, final IDomainTreeEnhancer domainTreeEnhancer) {
        return createCorrect(factory, root, contextPath, contextualExpression, title, desc, attribute, originationProperty, domainTreeEnhancer, true);
    }

    /**
     * Creates full {@link CalculatedProperty} with all keys initialised, validates it and throws validation exception if any.
     */
    protected final static CalculatedProperty createCorrect(final EntityFactory factory, final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty, final IDomainTreeEnhancer domainTreeEnhancer, final boolean validateTitleContextOfExtractedProperties) {
        final CalculatedProperty calc = create(factory, root, contextPath, contextualExpression, title, desc, attribute, originationProperty, domainTreeEnhancer, validateTitleContextOfExtractedProperties);

        if (!calc.isValid().isSuccessful()) {
            throw calc.isValid();
        }
        if (calc.category() == null) {
            throw new IncorrectCalcPropertyException("The category of calculated property cannot be null.");
        }
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

    protected IDomainTreeEnhancer getEnhancer() {
        return enhancer;
    }

    private void setEnhancer(final IDomainTreeEnhancer enhancer) {
        if (enhancer == null) {
            throw new IncorrectCalcPropertyException("An enhancer of the calculated property cannot be 'null'.");
        }
        this.enhancer = enhancer;
    }

    protected Object enhancer() {
        if (enhancer instanceof DomainTreeEnhancer0) {
            return enhancer;
        } else if (enhancer instanceof DomainTreeEnhancer) {
            return enhancer;
        } else if (enhancer instanceof DomainTreeEnhancerWithPropertiesPopulation) {
            return ((DomainTreeEnhancerWithPropertiesPopulation) enhancer).baseEnhancer();
        } else {
            throw new IncorrectCalcPropertyException("Unknown enhancer type [" + enhancer.getClass() + "].");
        }
    }

    protected void inferName() {
        this.name = generateNameFrom(title);
    }

    protected void inferContextType() {
        this.contextType = determineType(contextPath);
    }

    protected AstNode createAst(final String newContextualExpression) throws RecognitionException, SemanticException {
        return new ExpressionText2ModelConverter((Class<? extends AbstractEntity<?>>) getEnhancer().getManagedType(getRoot()), getContextPath(), newContextualExpression).convert();
    }

    protected CalculatedProperty copy() {
        try {
            getEnhancer().getCalculatedProperty(getRoot(), pathAndName());
        } catch (final IncorrectCalcPropertyException e) {
            throw new IncorrectCalcPropertyException("Cannot copy calculated property [" + this + "], that was not added to its enhancer.");
        }
        if (!isValid().isSuccessful()) {
            throw new IncorrectCalcPropertyException("Cannot copy invalid calculated property [" + this + "]. " + isValid().getMessage());
        }
        // the copy will be surely incorrect due to the same "title"
        return create(getEntityFactory(), root, contextPath, contextualExpression, title, getDesc(), attribute, originationProperty, getEnhancer());
    }

    protected boolean isValidateTitleContextOfExtractedProperties() {
        return validateTitleContextOfExtractedProperties;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////// VALIDATION UTILITIES ////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    protected static void validateRootWithoutRootTypeEnforcement(final IDomainTreeEnhancer enhancer, final Class<?> root) {
        try {
            AbstractDomainTree.validateRootType(root);
        } catch (final IllegalArgumentException e) {
            throw new IncorrectCalcPropertyException(e.getMessage());
        }
    }

    protected static void validateRoot(final IDomainTreeEnhancer enhancer, final Class<?> root) {
        validateRootWithoutRootTypeEnforcement(enhancer, root);
        if (!enhancer.rootTypes().contains(root)) {
            throw new IncorrectCalcPropertyException("The type [" + root.getSimpleName()
                    + "] is not a root type of enhancer. So the calculated property for that enhancer cannot be created.");
        }
    }

    private static void validateContextPath(final CalculatedProperty cp, final String newContextPath) {
        validatePath(cp.getRoot(), newContextPath, "The path [" + newContextPath + "] in type [" + cp.getRoot()
                + "] does not exist. It cannot be used as context path for calculated property.");
        try {
            AbstractDomainTree.illegalType(cp.getRoot(), newContextPath, "Could not use 'non-AE' property [" + newContextPath + "] in type [" + cp.getRoot().getSimpleName()
                    + "] for 'contextPath' of calculated property.", AbstractEntity.class);
        } catch (final IllegalArgumentException e) {
            throw new IncorrectCalcPropertyException(e.getMessage());
        }
    }

    private static void validatePath(final Class<?> root, final String path, final String message) {
        if (path == null) {
            throw new IncorrectCalcPropertyException(message);
        }
        if (!"".equals(path)) { // validate path
            try {
                PropertyTypeDeterminator.determinePropertyType(root, path); // throw exception when the place does not exist
            } catch (final Exception e) {
                throw new IncorrectCalcPropertyException(message);
            }
        }
    }

    protected static void validateContextualExpression(final CalculatedProperty cp, final String newContextualExpression) {
        //	if (StringUtils.isEmpty(newContextualExpression)) {
        //	    throw new IncorrectCalcPropertyException("The expression cannot be empty.");
        //	} -- should be handled by requiredness
        final AstNode ast;
        try {
            ast = cp.createAst(newContextualExpression);
        } catch (final Exception ex) {
            throw new IncorrectCalcPropertyException(ex.getMessage());
        }

        final int levelsToRaiseTheProperty = CalculatedProperty.levelsToRaiseTheProperty(cp.getRoot(), cp.getContextPath(), ast.getLevel());
        if (isCollectionOrInCollectionHierarchy(cp.getRoot(), cp.getContextPath())) { // collectional hierarchy
            if (levelsToRaiseTheProperty >= 1) {
                throw new IncorrectCalcPropertyException("Aggregated collections are currently unsupported. Please try to use simple expressions under collections (or with ALL / ANY attributes).");
            }
        } else {
            if (levelsToRaiseTheProperty > 1) {
                throw new IncorrectCalcPropertyException("The aggregation cannot be applied twice or more for simple non-collectional entity hirerarchy (\"Total\" values cannot be aggregated).");
            }
        }
    }

    protected static void validateTitle(final CalculatedProperty cp, final String newTitle) {
        //	if (StringUtils.isEmpty(newTitle)) {
        //	    throw new IncorrectCalcPropertyException("A title of the calculated property cannot be empty.");
        //	}
        final String name = CalculatedProperty.generateNameFrom(newTitle);
        if (StringUtils.isEmpty(name)) {
            throw new IncorrectCalcPropertyException("Please specify more appropriate title with some characters (and perhaps digits).");
        }

        if (cp.path() != null) {
            // validate if calculated property is correct in context of other calculated properties inside Domain Tree Enhancer
            validateCalculatedPropertyKey1(cp, cp.pathWith(name));
        }
    }

    /**
     * Validates the calculated property key (see {@link #validatePropertyKey0(Class, String, Map)}) and checks whether another property with the same name exists (calculated or
     * not). If exists -- throws {@link IncorrectCalcPropertyException}.
     * 
     * @param calculatedPropertyToCheck
     * @param newPathAndName
     * @param calculatedProperties
     * @param originalAndEnhancedRootTypes
     * @return
     */
    private static void validateCalculatedPropertyKey1(final CalculatedProperty calculatedPropertyToCheck, final String newPathAndName) {
        final Class<?> root = calculatedPropertyToCheck.getRoot();

        final ICalculatedProperty calculatedProperty = (calculatedPropertyToCheck.enhancer() instanceof DomainTreeEnhancer) ? (((DomainTreeEnhancer) calculatedPropertyToCheck.enhancer()).calculatedProperty(root, newPathAndName))
                : ((DomainTreeEnhancer0) calculatedPropertyToCheck.enhancer()).calculatedProperty(root, newPathAndName);
        if (calculatedProperty != null) {
            if (calculatedProperty == calculatedPropertyToCheck) {
                // this is the same property!
            } else {
                if (calculatedPropertyToCheck.isValidateTitleContextOfExtractedProperties()) {
                    throw new IncorrectCalcPropertyException("The calculated property with name [" + newPathAndName + "] already exists.");
                }
            }
        }
        try {
            PropertyTypeDeterminator.determinePropertyType(root, newPathAndName);
            // if (AbstractDomainTreeRepresentation.isCalculated(root, pathAndName)) {
            //     return null; // the property with a suggested name exists in original domain, but it is "calculated", which is correct
            // }
        } catch (final Exception e) {
            return; // the property with a suggested name does not exist in original domain, which is correct
        }
        throw new IncorrectCalcPropertyException("The property with the name [" + newPathAndName + "] already exists in original domain (inside " + root.getSimpleName()
                + " root). Please try another name for calculated property.");
    }

    protected static void validateAttribute(final CalculatedProperty cp, final CalculatedPropertyAttribute newAttribute) {
        if (newAttribute == null) {
            throw new IncorrectCalcPropertyException("The attribute cannot be null.");
        }
        final boolean any_or_all = !NO_ATTR.equals(newAttribute);
        if (any_or_all && cp.category() == null) {
            throw new IncorrectCalcPropertyException("ALL / ANY attribute cannot be applied without a category which was not defined due to invalid expression.");
        }
        if (any_or_all && cp.category() != null && !COLLECTIONAL_EXPRESSION.equals(cp.category()) && !ATTRIBUTED_COLLECTIONAL_EXPRESSION.equals(cp.category())) {
            throw new IncorrectCalcPropertyException("ALL / ANY attribute cannot be applied to property with category [" + cp.category() + "].");
        }
    }

    protected static void validateOriginationProperty(final CalculatedProperty cp, final String newOriginationProperty) {
        //	if (newOriginationProperty == null) {
        //	    return new IncorrectCalcPropertyException("The origination property cannot be null.");
        //	}
        //	// check if the "originationProperty" is correct in context of "contextType":
        //	if (property.isRequired() && StringUtils.isEmpty(newOriginationProperty)) {
        //	    return new IncorrectCalcPropertyException("The origination property cannot be empty for Aggregated Expressions.");
        //	}

        final Class<?> managedType = cp.getEnhancer().getManagedType(cp.getRoot());
        final String realOriginationProperty = Reflector.fromRelative2AbsotulePath(cp.getContextPath(), newOriginationProperty);
        validatePath(managedType, realOriginationProperty, "The origination property [" + newOriginationProperty + "] does not exist in type [" + managedType + "].");

        final Field field = StringUtils.isEmpty(realOriginationProperty) ? null : Finder.findFieldByName(managedType, realOriginationProperty);
        final String originationPropExpression = field != null && AnnotationReflector.isAnnotationPresent(field, Calculated.class) ? AnnotationReflector.getAnnotation(field, Calculated.class).value()
                : newOriginationProperty;
        if (cp.getContextualExpression() == null || !cp.getContextualExpression().contains(originationPropExpression)) {
            throw new CalcPropertyWarning("The origination property does not take a part in the expression. Is that correct?");
        }
    }

    /**
     * TODO This very tricky setter is used for mutating a property name of calc prop not to match it with 'generateNameFrom(title)'.
     * 
     * @param name
     */
    public void setNameVeryTricky(final String name) {
        this.name = name;
    }
}
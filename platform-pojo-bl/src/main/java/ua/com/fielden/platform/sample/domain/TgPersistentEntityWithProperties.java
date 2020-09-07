package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.annotation.CritOnly.Type.SINGLE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.DisplayDescription;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.UpperCase;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.validation.annotation.Max;
import ua.com.fielden.platform.sample.domain.definers.CosWithACEDefiner;
import ua.com.fielden.platform.sample.domain.definers.RequirednessDefiner;
import ua.com.fielden.platform.sample.domain.validators.CosConcreteValueProhibitedValidator;
import ua.com.fielden.platform.sample.domain.validators.CosEmptyValueProhibitedValidator;
import ua.com.fielden.platform.sample.domain.validators.CosWithValidatorValidator;
import ua.com.fielden.platform.sample.domain.validators.CosWithWarnerValidator;
import ua.com.fielden.platform.sample.domain.validators.DateValidator;
import ua.com.fielden.platform.sample.domain.validators.EntityValidator;
import ua.com.fielden.platform.sample.domain.validators.RequiredValidatedPropValidator;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgPersistentEntityWithProperties.class)
@MapEntityTo
@DescTitle(value = "Desc", desc = "Some desc description")
@DisplayDescription
public class TgPersistentEntityWithProperties extends AbstractFunctionalEntityWithCentreContext<String> { // yet persistent (@MapEntityTo); has been marked as functional to be able to add it as the action of editing itself
    @IsProperty
    @MapTo
    @Title(value = "Integer prop", desc = "Integer prop desc")
    private Integer integerProp;

    @IsProperty
    @MapTo
    @Title(value = "Entity prop", desc = "Entity prop desc")
    @BeforeChange(@Handler(EntityValidator.class))
    private TgPersistentEntityWithProperties entityProp;

    @IsProperty(precision = 18, scale = 5)
    @MapTo
    @Title(value = "BigDecimal prop", desc = "BigDecimal prop desc")
    private BigDecimal bigDecimalProp;

    @IsProperty
    @MapTo
    @Title(value = "String prop", desc = "String prop desc")
    @UpperCase
    private String stringProp;

    @IsProperty
    @MapTo
    @Title(value = "Boolean prop", desc = "Boolean prop desc")
    private boolean booleanProp;

    @IsProperty
    @MapTo
    @Title(value = "Date prop", desc = "Date prop desc")
    @BeforeChange(@Handler(DateValidator.class))
    private Date dateProp;

    @IsProperty
    @MapTo
    @Title(value = "Producer initialised prop", desc = "Producer initialised prop desc")
    private TgPersistentEntityWithProperties producerInitProp;

    @IsProperty
    @MapTo
    @Title(value = "Domain initialised prop", desc = "The property that was initialised directly inside Entity type definition Java class")
    private String domainInitProp = "ok";

    @IsProperty
    @MapTo
    @Title(value = "Non-conflicting prop", desc = "Non-conflicting prop desc")
    private String nonConflictingProp;

    @IsProperty
    @MapTo
    @Title(value = "Conflicting prop", desc = "Conflicting prop desc")
    private String conflictingProp;

    @IsProperty(displayAs="#2vs#1v")
    @MapTo
    @Title(value = "Composite prop", desc = "Composite prop desc")
    private TgPersistentCompositeEntity compositeProp;

    @IsProperty
    @MapTo
    @Title(value = "Money prop", desc = "Money prop desc")
    // @PersistedType(userType = IMoneyUserType.class)
    private Money moneyProp;

    @IsProperty
    @CritOnly(Type.SINGLE)
    @Title(value = "Crit-only single entity prop", desc = "Crit-only single entity prop desc")
    private TgPersistentEntityWithProperties critOnlyEntityProp;

    @IsProperty
    @MapTo
    @Required
    @BeforeChange(@Handler(RequiredValidatedPropValidator.class))
    @AfterChange(RequirednessDefiner.class)
    @Title(value = "Required validated prop", desc = "Required validated prop desc")
    private Integer requiredValidatedProp;

    @IsProperty
    @Readonly
    @Calculated
    @Title(value = "Number of Attachments", desc = "Desc")
    private Integer numberOfAttachments = 0;
    protected static final ExpressionModel numberOfAttachments_ = expr().model(
                    select(TgPersistentEntityWithPropertiesAttachment.class).as("attachment").where()
                    .prop("attachment.master").eq().extProp("id").groupBy().prop("attachment.master").yield().countAll().modelAsPrimitive()).model();

    @IsProperty
    @CritOnly(Type.SINGLE)
    @Title(value = "Crit-only single date prop", desc = "Crit-only single date prop desc")
    private Date critOnlyDateProp;

    @IsProperty
    @CritOnly(Type.SINGLE)
    @Title(value = "Crit-only single user prop", desc = "Crit-only single user prop desc")
    private User userParam;

    @IsProperty
    @CritOnly(Type.SINGLE)
    @Title(value = "Crit-only single integer prop", desc = "Crit-only single integer prop desc")
    private Integer critOnlyIntegerProp;

    @IsProperty
    @CritOnly(value = Type.SINGLE, scale = 5, precision = 18)
    @Title(value = "Crit-only single bigDecimal prop", desc = "Crit-only single bigDecimal prop desc")
    private BigDecimal critOnlyBigDecimalProp;

    @IsProperty
    @CritOnly(Type.SINGLE)
    @Title(value = "Crit-only single boolean prop", desc = "Crit-only single boolean prop desc")
    private boolean critOnlyBooleanProp = false;

    @IsProperty
    @CritOnly(Type.SINGLE)
    @Title(value = "Crit-only single string prop", desc = "Crit-only single string prop desc")
    private String critOnlyStringProp;

    @IsProperty
    @MapTo
    @Title(value = "Status", desc = "The current status of this entity")
    private TgPersistentStatus status;

    @IsProperty
    @Title(value = "Colour prop W", desc = "Colour prop description")
    @MapTo
    private Colour colourProp;

    @IsProperty
    @MapTo
    @Title(value = "Hyperlink", desc = "A property of type Hyperlink.")
    private Hyperlink hyperlinkProp;

    @IsProperty
    @MapTo
    @Title(value = "Proxy prop", desc = "Property to test proxiness (not added to fetch provider)")
    private TgPersistentEntityWithProperties proxyProp;

    @IsProperty
    @MapTo
    @Title(value = "Id-only proxy prop", desc = "Property to test id-only proxiness (added to fetch provider but provided with id-only proxy instance)")
    private TgPersistentEntityWithProperties idOnlyProxyProp;

    ////////////////////////////////////////// CRIT-ONLY SINGLE PROPERTIES (SELECTION CRITERIA VALIDATION / DEFINING #979) //////////////////////////////////////////
    // INITIAL
    @IsProperty
    @Title("cosStaticallyRequired")
    @CritOnly(SINGLE)
    @Required
    private TgPersistentEntityWithProperties cosStaticallyRequired;

    @IsProperty
    @Title("cosStaticallyReadonly")
    @CritOnly(SINGLE)
    @Readonly
    private TgPersistentEntityWithProperties cosStaticallyReadonly;

    @IsProperty
    @Title("cosEmptyValueProhibited")
    @CritOnly(SINGLE)
    @BeforeChange(@Handler(CosEmptyValueProhibitedValidator.class))
    private TgPersistentEntityWithProperties cosEmptyValueProhibited;

    @IsProperty
    @Title("cosConcreteValueProhibited")
    @CritOnly(SINGLE)
    @BeforeChange(@Handler(CosConcreteValueProhibitedValidator.class))
    private TgPersistentEntityWithProperties cosConcreteValueProhibited;

    @IsProperty
    @Title("cosStaticallyRequiredWithDefaultValue")
    @CritOnly(SINGLE)
    @Required
    private TgPersistentEntityWithProperties cosStaticallyRequiredWithDefaultValue;

    @IsProperty
    @Title("cosStaticallyReadonlyWithDefaultValue")
    @CritOnly(SINGLE)
    @Readonly
    private TgPersistentEntityWithProperties cosStaticallyReadonlyWithDefaultValue;

    // CHANGE
    @IsProperty
    @Title("cosWithValidator")
    @CritOnly(SINGLE)
    @BeforeChange(@Handler(CosWithValidatorValidator.class))
    private TgPersistentEntityWithProperties cosWithValidator;

    @IsProperty
    @Title("cosWithDependency")
    @CritOnly(SINGLE)
    @Dependent("cosWithValidator")
    private TgPersistentEntityWithProperties cosWithDependency;

    @IsProperty
    @Title("cosWithWarner")
    @CritOnly(SINGLE)
    @BeforeChange(@Handler(CosWithWarnerValidator.class))
    private TgPersistentEntityWithProperties cosWithWarner;

    @IsProperty
    @Title("cosStaticallyRequiredWithNonEmptyDefaultValue")
    @CritOnly(SINGLE)
    @Required
    private TgPersistentEntityWithProperties cosStaticallyRequiredWithNonEmptyDefaultValue;

    @IsProperty
    @Title("cosWithACE1")
    @CritOnly(SINGLE)
    @AfterChange(CosWithACEDefiner.class)
    private TgPersistentEntityWithProperties cosWithACE1;

    @IsProperty
    @Title("cosWithACE1Child1")
    @CritOnly(SINGLE)
    private TgPersistentEntityWithProperties cosWithACE1Child1;

    @IsProperty
    @Title("cosWithACE1Child2")
    @CritOnly(SINGLE)
    private TgPersistentEntityWithProperties cosWithACE1Child2;

    @IsProperty
    @Title("cosWithACE1WithDefaultValue")
    @CritOnly(SINGLE)
    @AfterChange(CosWithACEDefiner.class)
    private TgPersistentEntityWithProperties cosWithACE1WithDefaultValue;

    @IsProperty
    @Title("cosWithACE1WithDefaultValueChild1")
    @CritOnly(SINGLE)
    private TgPersistentEntityWithProperties cosWithACE1WithDefaultValueChild1;

    @IsProperty
    @Title("cosWithACE1WithDefaultValueChild2")
    @CritOnly(SINGLE)
    private TgPersistentEntityWithProperties cosWithACE1WithDefaultValueChild2;

    @IsProperty
    @Title("cosWithACE2")
    @CritOnly(SINGLE)
    @AfterChange(CosWithACEDefiner.class)
    private TgPersistentEntityWithProperties cosWithACE2;

    @IsProperty
    @Title("cosWithACE2Child1")
    @CritOnly(SINGLE)
    private TgPersistentEntityWithProperties cosWithACE2Child1;

    @IsProperty
    @Title("cosWithACE2Child2")
    @CritOnly(SINGLE)
    private TgPersistentEntityWithProperties cosWithACE2Child2;

    @IsProperty
    @Title("cosWithACE2WithDefaultValue")
    @CritOnly(SINGLE)
    @AfterChange(CosWithACEDefiner.class)
    private TgPersistentEntityWithProperties cosWithACE2WithDefaultValue;

    @IsProperty
    @Title("cosWithACE2WithDefaultValueChild1")
    @CritOnly(SINGLE)
    private TgPersistentEntityWithProperties cosWithACE2WithDefaultValueChild1;

    @IsProperty
    @Title("cosWithACE2WithDefaultValueChild2")
    @CritOnly(SINGLE)
    private TgPersistentEntityWithProperties cosWithACE2WithDefaultValueChild2;

    @IsProperty(TgPersistentEntityWithProperties.class)
    @MapTo
    private PropertyDescriptor<TgPersistentEntityWithProperties> propertyDescriptorProp;

    @Observable
    public TgPersistentEntityWithProperties setPropertyDescriptorProp(final PropertyDescriptor<TgPersistentEntityWithProperties> propertyDescriptorProp) {
        this.propertyDescriptorProp = propertyDescriptorProp;
        return this;
    }

    public PropertyDescriptor<TgPersistentEntityWithProperties> getPropertyDescriptorProp() {
        return propertyDescriptorProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosWithACE2WithDefaultValueChild2(final TgPersistentEntityWithProperties cosWithACE2WithDefaultValueChild2) {
        this.cosWithACE2WithDefaultValueChild2 = cosWithACE2WithDefaultValueChild2;
        return this;
    }

    public TgPersistentEntityWithProperties getCosWithACE2WithDefaultValueChild2() {
        return cosWithACE2WithDefaultValueChild2;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosWithACE2WithDefaultValueChild1(final TgPersistentEntityWithProperties cosWithACE2WithDefaultValueChild1) {
        this.cosWithACE2WithDefaultValueChild1 = cosWithACE2WithDefaultValueChild1;
        return this;
    }

    public TgPersistentEntityWithProperties getCosWithACE2WithDefaultValueChild1() {
        return cosWithACE2WithDefaultValueChild1;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosWithACE2WithDefaultValue(final TgPersistentEntityWithProperties cosWithACE2WithDefaultValue) {
        this.cosWithACE2WithDefaultValue = cosWithACE2WithDefaultValue;
        return this;
    }

    public TgPersistentEntityWithProperties getCosWithACE2WithDefaultValue() {
        return cosWithACE2WithDefaultValue;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosWithACE2Child2(final TgPersistentEntityWithProperties cosWithACE2Child2) {
        this.cosWithACE2Child2 = cosWithACE2Child2;
        return this;
    }

    public TgPersistentEntityWithProperties getCosWithACE2Child2() {
        return cosWithACE2Child2;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosWithACE2Child1(final TgPersistentEntityWithProperties cosWithACE2Child1) {
        this.cosWithACE2Child1 = cosWithACE2Child1;
        return this;
    }

    public TgPersistentEntityWithProperties getCosWithACE2Child1() {
        return cosWithACE2Child1;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosWithACE2(final TgPersistentEntityWithProperties cosWithACE2) {
        this.cosWithACE2 = cosWithACE2;
        return this;
    }

    public TgPersistentEntityWithProperties getCosWithACE2() {
        return cosWithACE2;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosWithACE1WithDefaultValueChild2(final TgPersistentEntityWithProperties cosWithACE1WithDefaultValueChild2) {
        this.cosWithACE1WithDefaultValueChild2 = cosWithACE1WithDefaultValueChild2;
        return this;
    }

    public TgPersistentEntityWithProperties getCosWithACE1WithDefaultValueChild2() {
        return cosWithACE1WithDefaultValueChild2;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosWithACE1WithDefaultValueChild1(final TgPersistentEntityWithProperties cosWithACE1WithDefaultValueChild1) {
        this.cosWithACE1WithDefaultValueChild1 = cosWithACE1WithDefaultValueChild1;
        return this;
    }

    public TgPersistentEntityWithProperties getCosWithACE1WithDefaultValueChild1() {
        return cosWithACE1WithDefaultValueChild1;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosWithACE1WithDefaultValue(final TgPersistentEntityWithProperties cosWithACE1WithDefaultValue) {
        this.cosWithACE1WithDefaultValue = cosWithACE1WithDefaultValue;
        return this;
    }

    public TgPersistentEntityWithProperties getCosWithACE1WithDefaultValue() {
        return cosWithACE1WithDefaultValue;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosWithACE1Child2(final TgPersistentEntityWithProperties cosWithACE1Child2) {
        this.cosWithACE1Child2 = cosWithACE1Child2;
        return this;
    }

    public TgPersistentEntityWithProperties getCosWithACE1Child2() {
        return cosWithACE1Child2;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosWithACE1Child1(final TgPersistentEntityWithProperties cosWithACE1Child1) {
        this.cosWithACE1Child1 = cosWithACE1Child1;
        return this;
    }

    public TgPersistentEntityWithProperties getCosWithACE1Child1() {
        return cosWithACE1Child1;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosWithACE1(final TgPersistentEntityWithProperties cosWithACE1) {
        this.cosWithACE1 = cosWithACE1;
        return this;
    }

    public TgPersistentEntityWithProperties getCosWithACE1() {
        return cosWithACE1;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosStaticallyRequiredWithNonEmptyDefaultValue(final TgPersistentEntityWithProperties cosStaticallyRequiredWithNonEmptyDefaultValue) {
        this.cosStaticallyRequiredWithNonEmptyDefaultValue = cosStaticallyRequiredWithNonEmptyDefaultValue;
        return this;
    }

    public TgPersistentEntityWithProperties getCosStaticallyRequiredWithNonEmptyDefaultValue() {
        return cosStaticallyRequiredWithNonEmptyDefaultValue;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosWithWarner(final TgPersistentEntityWithProperties cosWithWarner) {
        this.cosWithWarner = cosWithWarner;
        return this;
    }

    public TgPersistentEntityWithProperties getCosWithWarner() {
        return cosWithWarner;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosWithDependency(final TgPersistentEntityWithProperties cosWithDependency) {
        this.cosWithDependency = cosWithDependency;
        return this;
    }

    public TgPersistentEntityWithProperties getCosWithDependency() {
        return cosWithDependency;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosWithValidator(final TgPersistentEntityWithProperties cosWithValidator) {
        this.cosWithValidator = cosWithValidator;
        return this;
    }

    public TgPersistentEntityWithProperties getCosWithValidator() {
        return cosWithValidator;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosStaticallyReadonlyWithDefaultValue(final TgPersistentEntityWithProperties cosStaticallyReadonlyWithDefaultValue) {
        this.cosStaticallyReadonlyWithDefaultValue = cosStaticallyReadonlyWithDefaultValue;
        return this;
    }

    public TgPersistentEntityWithProperties getCosStaticallyReadonlyWithDefaultValue() {
        return cosStaticallyReadonlyWithDefaultValue;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosStaticallyRequiredWithDefaultValue(final TgPersistentEntityWithProperties cosStaticallyRequiredWithDefaultValue) {
        this.cosStaticallyRequiredWithDefaultValue = cosStaticallyRequiredWithDefaultValue;
        return this;
    }

    public TgPersistentEntityWithProperties getCosStaticallyRequiredWithDefaultValue() {
        return cosStaticallyRequiredWithDefaultValue;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosConcreteValueProhibited(final TgPersistentEntityWithProperties cosConcreteValueProhibited) {
        this.cosConcreteValueProhibited = cosConcreteValueProhibited;
        return this;
    }

    public TgPersistentEntityWithProperties getCosConcreteValueProhibited() {
        return cosConcreteValueProhibited;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosEmptyValueProhibited(final TgPersistentEntityWithProperties cosEmptyValueProhibited) {
        this.cosEmptyValueProhibited = cosEmptyValueProhibited;
        return this;
    }

    public TgPersistentEntityWithProperties getCosEmptyValueProhibited() {
        return cosEmptyValueProhibited;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosStaticallyReadonly(final TgPersistentEntityWithProperties cosStaticallyReadonly) {
        this.cosStaticallyReadonly = cosStaticallyReadonly;
        return this;
    }

    public TgPersistentEntityWithProperties getCosStaticallyReadonly() {
        return cosStaticallyReadonly;
    }

    @Observable
    public TgPersistentEntityWithProperties setCosStaticallyRequired(final TgPersistentEntityWithProperties cosStaticallyRequired) {
        this.cosStaticallyRequired = cosStaticallyRequired;
        return this;
    }

    public TgPersistentEntityWithProperties getCosStaticallyRequired() {
        return cosStaticallyRequired;
    }

    @Observable
    public TgPersistentEntityWithProperties setIdOnlyProxyProp(final TgPersistentEntityWithProperties idOnlyProxyProp) {
        this.idOnlyProxyProp = idOnlyProxyProp;
        return this;
    }

    public TgPersistentEntityWithProperties getIdOnlyProxyProp() {
        return idOnlyProxyProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setProxyProp(final TgPersistentEntityWithProperties proxyProp) {
        this.proxyProp = proxyProp;
        return this;
    }

    public TgPersistentEntityWithProperties getProxyProp() {
        return proxyProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setHyperlinkProp(final Hyperlink hyperlinkProp) {
        this.hyperlinkProp = hyperlinkProp;
        return this;
    }

    public Hyperlink getHyperlinkProp() {
        return hyperlinkProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setColourProp(final Colour prop) {
        this.colourProp = prop;
        return this;
    }

    public Colour getColourProp() {
        return colourProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setStatus(final TgPersistentStatus status) {
        this.status = status;
        return this;
    }

    public TgPersistentStatus getStatus() {
        return status;
    }

    @Observable
    public TgPersistentEntityWithProperties setCritOnlyStringProp(final String critOnlyStringProp) {
        this.critOnlyStringProp = critOnlyStringProp;
        return this;
    }

    public String getCritOnlyStringProp() {
        return critOnlyStringProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setCritOnlyBooleanProp(final boolean critOnlyBooleanProp) {
        this.critOnlyBooleanProp = critOnlyBooleanProp;
        return this;
    }

    public boolean getCritOnlyBooleanProp() {
        return critOnlyBooleanProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setCritOnlyBigDecimalProp(final BigDecimal critOnlyBigDecimalProp) {
        this.critOnlyBigDecimalProp = critOnlyBigDecimalProp;
        return this;
    }

    public BigDecimal getCritOnlyBigDecimalProp() {
        return critOnlyBigDecimalProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setCritOnlyIntegerProp(final Integer critOnlyIntegerProp) {
        this.critOnlyIntegerProp = critOnlyIntegerProp;
        return this;
    }

    public Integer getCritOnlyIntegerProp() {
        return critOnlyIntegerProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setUserParam(final User userParam) {
        this.userParam = userParam;
        return this;
    }

    public User getUserParam() {
        return userParam;
    }

    @Observable
    public TgPersistentEntityWithProperties setCritOnlyDateProp(final Date critOnlyDateProp) {
        this.critOnlyDateProp = critOnlyDateProp;
        return this;
    }

    public Date getCritOnlyDateProp() {
        return critOnlyDateProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setRequiredValidatedProp(final Integer requiredValidatedProp) {
        this.requiredValidatedProp = requiredValidatedProp;
        return this;
    }

    public Integer getRequiredValidatedProp() {
        return requiredValidatedProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setCritOnlyEntityProp(final TgPersistentEntityWithProperties critOnlyEntityProp) {
        this.critOnlyEntityProp = critOnlyEntityProp;
        return this;
    }

    public TgPersistentEntityWithProperties getCritOnlyEntityProp() {
        return critOnlyEntityProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setCompositeProp(final TgPersistentCompositeEntity compositeProp) {
        this.compositeProp = compositeProp;
        return this;
    }

    @Override
    @Observable
    public TgPersistentEntityWithProperties setDesc(final String desc) {
        super.setDesc(desc);
        return this;
    }

    public TgPersistentCompositeEntity getCompositeProp() {
        return compositeProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setConflictingProp(final String conflictingProp) {
        this.conflictingProp = conflictingProp;
        return this;
    }

    public String getConflictingProp() {
        return conflictingProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setNonConflictingProp(final String nonConflictingProp) {
        this.nonConflictingProp = nonConflictingProp;
        return this;
    }

    public String getNonConflictingProp() {
        return nonConflictingProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setDomainInitProp(final String domainInitProp) {
        this.domainInitProp = domainInitProp;
        return this;
    }

    public String getDomainInitProp() {
        return domainInitProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setProducerInitProp(final TgPersistentEntityWithProperties producerInitProp) {
        this.producerInitProp = producerInitProp;
        return this;
    }

    public TgPersistentEntityWithProperties getProducerInitProp() {
        return producerInitProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setDateProp(final Date dateProp) {
        this.dateProp = dateProp;
        return this;
    }

    public Date getDateProp() {
        return dateProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setBooleanProp(final boolean booleanProp) {
        this.booleanProp = booleanProp;
        return this;
    }

    public boolean getBooleanProp() {
        return booleanProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setStringProp(final String stringProp) {
        this.stringProp = stringProp;
        return this;
    }

    public String getStringProp() {
        return stringProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setBigDecimalProp(final BigDecimal bigDecimalProp) {
        this.bigDecimalProp = bigDecimalProp;
        return this;
    }

    public BigDecimal getBigDecimalProp() {
        return bigDecimalProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setMoneyProp(final Money moneyProp) {
        this.moneyProp = moneyProp;
        return this;
    }

    public Money getMoneyProp() {
        return moneyProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setEntityProp(final TgPersistentEntityWithProperties entityProp) {
        this.entityProp = entityProp;
        return this;
    }

    public TgPersistentEntityWithProperties getEntityProp() {
        return entityProp;
    }

    @Observable
    @Max(9999)
    // @GreaterOrEqual(-600)
    public TgPersistentEntityWithProperties setIntegerProp(final Integer integerProp) {
        this.integerProp = integerProp;
        return this;
    }

    public Integer getIntegerProp() {
        return integerProp;
    }

    @Observable
    protected TgPersistentEntityWithProperties setNumberOfAttachments(final Integer numberOfAttachments) {
        this.numberOfAttachments = numberOfAttachments;
        return this;
    }

    public Integer getNumberOfAttachments() {
        return numberOfAttachments;
    }

}
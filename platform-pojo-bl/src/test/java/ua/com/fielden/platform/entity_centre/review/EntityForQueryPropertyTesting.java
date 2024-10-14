package ua.com.fielden.platform.entity_centre.review;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.createConditionProperty;

/**
 * Entity class used for {@link QueryProperty} testing.
 *
 * @author 01es
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Entity No", desc = "Key Property")
@DescTitle(value = "Description", desc = "Description Property")
@DescRequired
public class EntityForQueryPropertyTesting extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    public static final String NOT_NULL_MSG = "Missing value";

    private enum EnumType {
        E1, E2
    }

    ////////// Unsupported types //////////
    @IsProperty
    private Boolean unsupportedProp1 = null;
    @IsProperty
    private EnumType unsupportedProp2 = EnumType.E1;

    @IsProperty
    private EntityForQueryPropertyTesting entity1;
    @IsProperty
    @CritOnly(Type.RANGE)
    private EntityForQueryPropertyTesting entity2;
    @IsProperty
    @CritOnly(Type.SINGLE)
    private EntityForQueryPropertyTesting entity3;

    @IsProperty
    @Title(value = "First Property", desc = "used for testing")
    private Integer firstProperty = null;

    @IsProperty
    @Title("Observable Property")
    private BigDecimal observableProperty = BigDecimal.ZERO;

    @IsProperty
    private Date date;

    @IsProperty
    private Money money;

    @IsProperty
    @Title("Boolean Property")
    private boolean bool = false;

    @IsProperty
    @Required
    private String strProp;

    @IsProperty(EntityForQueryPropertyTesting.class)
    private final List<EntityForQueryPropertyTesting> entities = new ArrayList<EntityForQueryPropertyTesting>();

    @IsProperty(CollectionParentEntity.class)
    private final List<CollectionParentEntity> coll = new ArrayList<CollectionParentEntity>();

    @IsProperty
    private EntityForQueryPropertyTesting alternativeEntity;

    @Observable
    public EntityForQueryPropertyTesting setAlternativeEntity(final EntityForQueryPropertyTesting value) {
        this.alternativeEntity = value;
        return this;
    }

    public EntityForQueryPropertyTesting getAlternativeEntity() {
        return alternativeEntity;
    }

    @IsProperty
    @CritOnly(value = Type.MULTI, entityUnderCondition = EntityForQueryPropertyTesting.class, propUnderCondition = "alternativeEntity.key")
    @Title(value = "Alternatives", desc = "These are the Alternatives related to an EntityForQueryPropertyTesting. Mnemonic \"missing\" returns true only for those EntityForQueryPropertyTesting items that have no related Alternatives.")
    private EntityForQueryPropertyTesting alternativeEntityCrit;
    protected static final ICompoundCondition0<EntityForQueryPropertyTesting> alternativeEntityCrit_ = select(EntityForQueryPropertyTesting.class).where()
            .prop("alternativeEntity").eq().prop(createConditionProperty("id"));

    @Observable
    public EntityForQueryPropertyTesting setAlternativeEntityCrit(final EntityForQueryPropertyTesting value) {
        this.alternativeEntityCrit = value;
        return this;
    }

    public EntityForQueryPropertyTesting getAlternativeEntityCrit() {
        return alternativeEntityCrit;
    }

    protected EntityForQueryPropertyTesting() {
    }

    public Integer getFirstProperty() {
        return firstProperty;
    }

    @Observable
    public EntityForQueryPropertyTesting setFirstProperty(final Integer property) {
        this.firstProperty = property;
        return this;
    }

    public BigDecimal getObservableProperty() {
        return observableProperty;
    }

    @Observable
    public EntityForQueryPropertyTesting setObservableProperty(final BigDecimal observableProperty) {
        this.observableProperty = observableProperty;
        return this;
    }

    public List<EntityForQueryPropertyTesting> getEntities() {
        return unmodifiableList(entities);
    }

    @Observable
    public EntityForQueryPropertyTesting setEntities(final List<EntityForQueryPropertyTesting> entities) {
        this.entities.clear();
        this.entities.addAll(entities);
        return this;
    }

    public List<CollectionParentEntity> getColl() {
        return unmodifiableList(coll);
    }

    @Observable
    public EntityForQueryPropertyTesting setColl(final List<CollectionParentEntity> coll) {
        this.coll.clear();
        this.coll.addAll(coll);
        return this;
    }

    public EntityForQueryPropertyTesting getEntity1() {
        return entity1;
    }

    @Observable
    public void setEntity1(final EntityForQueryPropertyTesting entity1) {
        this.entity1 = entity1;
    }

    public Date getDate() {
        return date;
    }

    @Observable
    public void setDate(final Date date) {
        this.date = date;
    }

    public Money getMoney() {
        return money;
    }

    @Observable
    public void setMoney(final Money money) {
        this.money = money;
    }

    @Observable
    public void setStrProp(final String strProp) {
        this.strProp = strProp;
    }

    public String getStrProp() {
        return strProp;
    }

    public boolean isBool() {
        return bool;
    }

    @Observable
    public void setBool(final boolean bool) {
        this.bool = bool;
    }

    public EntityForQueryPropertyTesting getEntity2() {
        return entity2;
    }

    @Observable
    public void setEntity2(final EntityForQueryPropertyTesting entity2) {
        this.entity2 = entity2;
    }

    public EntityForQueryPropertyTesting getEntity3() {
        return entity3;
    }

    @Observable
    public void setEntity3(final EntityForQueryPropertyTesting entity3) {
        this.entity3 = entity3;
    }

    public Boolean getUnsupportedProp1() {
        return unsupportedProp1;
    }

    @Observable
    public void setUnsupportedProp1(final Boolean unsupportedProp1) {
        this.unsupportedProp1 = unsupportedProp1;
    }

    public EnumType getUnsupportedProp2() {
        return unsupportedProp2;
    }

    @Observable
    public void setUnsupportedProp2(final EnumType unsupportedProp2) {
        this.unsupportedProp2 = unsupportedProp2;
    }
}

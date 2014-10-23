package ua.com.fielden.platform.swing.review;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.types.Money;

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
    private Double observableProperty = 0.0;

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
    private List<EntityForQueryPropertyTesting> entities = new ArrayList<EntityForQueryPropertyTesting>();

    @IsProperty(CollectionParentEntity.class)
    private List<CollectionParentEntity> coll = new ArrayList<CollectionParentEntity>();

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

    public Double getObservableProperty() {
        return observableProperty;
    }

    @Observable
    public EntityForQueryPropertyTesting setObservableProperty(final Double observableProperty) {
        this.observableProperty = observableProperty;
        return this;
    }

    public List<EntityForQueryPropertyTesting> getEntities() {
        return entities;
    }

    @Observable
    public void setEntities(final List<EntityForQueryPropertyTesting> entities) {
        this.entities.clear();
        this.entities.addAll(entities);
    }

    public List<CollectionParentEntity> getColl() {
        return coll;
    }

    @Observable
    public void setColl(final List<CollectionParentEntity> coll) {
        this.coll.clear();
        this.coll.addAll(coll);
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

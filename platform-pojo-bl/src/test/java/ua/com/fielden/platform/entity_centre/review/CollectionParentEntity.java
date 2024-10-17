package ua.com.fielden.platform.entity_centre.review;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.types.Money;

import static java.util.Collections.unmodifiableList;

/**
 * Entity class used for {@link QueryProperty} testing.
 *
 * @author 01es
 */
@KeyType(String.class)
@KeyTitle(value = "Entity No", desc = "Key Property")
@DescTitle(value = "Description", desc = "Description Property")
@DescRequired
public class CollectionParentEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = 1L;

    public static final String NOT_NULL_MSG = "Missing value";

    @IsProperty
    private CollectionParentEntity entity1;
    @IsProperty
    @CritOnly(Type.RANGE)
    private CollectionParentEntity entity2;
    @IsProperty
    @CritOnly(Type.SINGLE)
    private CollectionParentEntity entity3;

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

    @IsProperty(CollectionParentEntity.class)
    private final List<CollectionParentEntity> entities = new ArrayList<>();

    @IsProperty(CollectionParentEntity.class)
    private final List<CollectionParentEntity> coll = new ArrayList<>();

    protected CollectionParentEntity() {
    }

    public Integer getFirstProperty() {
        return firstProperty;
    }

    @Observable
    public CollectionParentEntity setFirstProperty(final Integer property) {
        this.firstProperty = property;
        return this;
    }

    public BigDecimal getObservableProperty() {
        return observableProperty;
    }

    @Observable
    public CollectionParentEntity setObservableProperty(final BigDecimal observableProperty) {
        this.observableProperty = observableProperty;
        return this;
    }

    public List<CollectionParentEntity> getEntities() {
        return unmodifiableList(entities);
    }

    @Observable
    public CollectionParentEntity setEntities(final List<CollectionParentEntity> entities) {
        this.entities.clear();
        this.entities.addAll(entities);
        return this;
    }

    public List<CollectionParentEntity> getColl() {
        return unmodifiableList(coll);
    }

    @Observable
    public CollectionParentEntity setColl(final List<CollectionParentEntity> coll) {
        this.coll.clear();
        this.coll.addAll(coll);
        return this;
    }

    public CollectionParentEntity getEntity1() {
        return entity1;
    }

    @Observable
    public void setEntity1(final CollectionParentEntity entity1) {
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

    public CollectionParentEntity getEntity2() {
        return entity2;
    }

    @Observable
    public void setEntity2(final CollectionParentEntity entity2) {
        this.entity2 = entity2;
    }

    public CollectionParentEntity getEntity3() {
        return entity3;
    }

    @Observable
    public void setEntity3(final CollectionParentEntity entity3) {
        this.entity3 = entity3;
    }

}

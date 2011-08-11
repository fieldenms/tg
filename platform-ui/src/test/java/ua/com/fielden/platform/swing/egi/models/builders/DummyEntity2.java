/**
 *
 */
package ua.com.fielden.platform.swing.egi.models.builders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.DomainValidation;
import ua.com.fielden.platform.types.Money;

/**
 * Dummy entity for example/testing
 * 
 * @author Yura
 */
@KeyType(Long.class)
public class DummyEntity2 extends AbstractEntity<Long> {
    private static final long serialVersionUID = 1L;

    private Integer nonProperty;

    @IsProperty
    @Title(value = "Money field", desc = "Money field")
    private Money moneyField;

    @IsProperty
    @Title(value = "Boolean field", desc = "Boolean field")
    private boolean boolField = true;

    @IsProperty
    @Title(value = "Date Field", desc = "Date Field")
    private Date dateField;

    @IsProperty
    @Title(value = "Int. field", desc = "Integer field")
    private Integer intField;

    private final List<DummyEntity> dummyEntities = new ArrayList<DummyEntity>();

    protected DummyEntity2() {
    }

    public DummyEntity2(final Long key, final String desc) {
	super(null, key, desc);
    }

    public Money getMoneyField() {
	return moneyField;
    }

    @Observable
    public DummyEntity2 setMoneyField(final Money moneyField) {
	this.moneyField = moneyField;
	return this;
    }

    public boolean isBoolField() {
	return boolField;
    }

    @Observable
    @DomainValidation
    public DummyEntity2 setBoolField(final boolean boolField) {
	this.boolField = boolField;
	return this;
    }

    public Date getDateField() {
	return dateField;
    }

    @Observable
    public DummyEntity2 setDateField(final Date dateField) {
	this.dateField = dateField;
	return this;
    }

    public Integer getIntField() {
	return intField;
    }

    @Observable
    @DomainValidation
    public DummyEntity2 setIntField(final Integer intField) {
	this.intField = intField;
	return this;
    }

    public List<DummyEntity> getDummyEntities() {
	return dummyEntities;
    }

    /**
     * Getter for non-existing property "dummyEntitiesCount"
     * 
     * @return
     */
    public Integer getDummyEntitiesCount() {
	return dummyEntities.size();
    }
}

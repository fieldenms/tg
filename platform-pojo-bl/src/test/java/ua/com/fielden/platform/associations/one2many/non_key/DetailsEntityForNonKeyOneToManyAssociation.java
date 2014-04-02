package ua.com.fielden.platform.associations.one2many.non_key;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.types.Money;

/**
 * Type representing the details side of One-to-Many association.
 * 
 * @author TG Team
 * 
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class DetailsEntityForNonKeyOneToManyAssociation extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Key 1", desc = "Desc")
    @CompositeKeyMember(1)
    private Date key1;

    @IsProperty
    @MapTo
    @Title(value = "Key 2", desc = "Desc")
    @CompositeKeyMember(2)
    private Integer key2;

    @IsProperty
    @MapTo
    @Title(value = "Master property", desc = "Desc")
    private MasterEntityWithNonKeyOneToManyAssociation many2oneProp;

    @Observable
    public DetailsEntityForNonKeyOneToManyAssociation setMany2oneProp(final MasterEntityWithNonKeyOneToManyAssociation many2oneProp) {
        this.many2oneProp = many2oneProp;
        return this;
    }

    public MasterEntityWithNonKeyOneToManyAssociation getMany2oneProp() {
        return many2oneProp;
    }

    @Observable
    public DetailsEntityForNonKeyOneToManyAssociation setKey2(final Integer key2) {
        this.key2 = key2;
        return this;
    }

    public Integer getKey2() {
        return key2;
    }

    @Observable
    public DetailsEntityForNonKeyOneToManyAssociation setKey1(final Date key1) {
        this.key1 = key1;
        return this;
    }

    public Date getKey1() {
        return key1;
    }

    @IsProperty
    @MapTo
    @Title(value = "Property 1", desc = "Desc")
    private String strProp;

    @IsProperty
    @MapTo
    @Title(value = "Property 2", desc = "Desc")
    private Integer intProp;

    @IsProperty
    @MapTo
    @Title(value = "Property 3", desc = "Desc")
    private Money moneyProp;

    @Observable
    public DetailsEntityForNonKeyOneToManyAssociation setMoneyProp(final Money moneyProp) {
        this.moneyProp = moneyProp;
        return this;
    }

    public Money getMoneyProp() {
        return moneyProp;
    }

    @Observable
    public DetailsEntityForNonKeyOneToManyAssociation setIntProp(final Integer intProp) {
        this.intProp = intProp;
        return this;
    }

    public Integer getIntProp() {
        return intProp;
    }

    @Observable
    public DetailsEntityForNonKeyOneToManyAssociation setStrProp(final String strProp) {
        this.strProp = strProp;
        return this;
    }

    public String getStrProp() {
        return strProp;
    }

}

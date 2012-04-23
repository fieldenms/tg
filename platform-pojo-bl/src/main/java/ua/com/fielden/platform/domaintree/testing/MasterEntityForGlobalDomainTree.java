package ua.com.fielden.platform.domaintree.testing;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Entity for "included properties logic" testing.
 *
 * @author TG Team
 *
 */
@KeyTitle(value = "Key title", desc = "Key desc")
@DescTitle(value = "Desc title", desc = "Desc desc")
@KeyType(String.class)
public class MasterEntityForGlobalDomainTree extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    protected MasterEntityForGlobalDomainTree() {
    }

    ////////// Range types //////////
    @IsProperty
    private Integer integerProp = null;

    public Integer getIntegerProp() {
        return integerProp;
    }
    @Observable
    public void setIntegerProp(final Integer integerProp) {
        this.integerProp = integerProp;
    }

    @IsProperty
    private BigDecimal bigDecimalProp = new BigDecimal(0.0);

    public BigDecimal getBigDecimalProp() {
	return bigDecimalProp;
    }
    @Observable
    public void setBigDecimalProp(final BigDecimal bigDecimalProp) {
	this.bigDecimalProp = bigDecimalProp;
    }

    @IsProperty
    private EntityWithStringKeyType simpleEntityProp;

    public EntityWithStringKeyType getSimpleEntityProp() {
        return simpleEntityProp;
    }
    @Observable
    public void setSimpleEntityProp(final EntityWithStringKeyType simpleEntityProp) {
        this.simpleEntityProp = simpleEntityProp;
    }
}

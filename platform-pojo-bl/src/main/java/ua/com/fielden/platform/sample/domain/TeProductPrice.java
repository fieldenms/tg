package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.types.Money;

@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(TeProductPriceCo.class)
public class TeProductPrice extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private String product;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    private Money price;

    public TeProductPrice setProduct(final String product) {
        this.product = product;
        return this;
    }

    public String getProduct() {
        return product;
    }

    public TeProductPrice setPrice(final Money price) {
        this.price = price;
        return this;
    }

    public Money getPrice() {
        return price;
    }
}
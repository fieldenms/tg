/**
 *
 */
package ua.com.fielden.platform.entity.validation.test_entities;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.DomainValidation;

import java.math.BigDecimal;

/**
 * Class for testing of domain validators registration.
 * 
 * @author Yura
 */
public class SubClass2 extends AbstractBaseClass {

    private static final long serialVersionUID = 8910516814682831599L;

    @IsProperty
    private BigDecimal bigDecimalProp;

    public BigDecimal getBigDecimalProp() {
        return bigDecimalProp;
    }

    @Observable
    @DomainValidation
    public void setBigDecimalProp(final BigDecimal bigDecimalProp) {
        this.bigDecimalProp = bigDecimalProp;
    }

}

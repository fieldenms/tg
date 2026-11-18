package ua.com.fielden.platform.types.markers;

import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.types.Money;

/// Represents a Hibernate type mapping for [Money] that includes properties [Money#taxAmount] and [Money#exTaxAmount].
///
public interface ISimplyMoneyWithTaxAndExTaxAmountType extends ICompositeUserTypeInstantiate {

}

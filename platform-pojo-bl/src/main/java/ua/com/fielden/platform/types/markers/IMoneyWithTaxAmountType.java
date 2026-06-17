package ua.com.fielden.platform.types.markers;

import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.types.Money;

/// Represents a Hibernate type mapping for [Money] that includes properties [Money#amount], [Money#currency] and [Money#taxAmount].
///
public interface IMoneyWithTaxAmountType extends ICompositeUserTypeInstantiate {

}

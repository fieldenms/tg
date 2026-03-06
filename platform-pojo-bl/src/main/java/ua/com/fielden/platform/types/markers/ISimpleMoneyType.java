package ua.com.fielden.platform.types.markers;

import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.types.Money;

/// Represents a Hibernate type mapping for [Money] that includes sole property [Money#amount].
///
public interface ISimpleMoneyType extends ICompositeUserTypeInstantiate {

}

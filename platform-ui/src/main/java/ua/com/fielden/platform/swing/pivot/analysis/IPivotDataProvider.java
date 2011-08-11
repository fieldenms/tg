package ua.com.fielden.platform.swing.pivot.analysis;

import ua.com.fielden.platform.reportquery.IAggregatedProperty;
import ua.com.fielden.platform.reportquery.IDistributedProperty;

public interface IPivotDataProvider {

    GroupItem getData();

    String getAliasFor(IDistributedProperty property);

    Class<?> getReturnTypeFor(IAggregatedProperty aggregationFunction);

}

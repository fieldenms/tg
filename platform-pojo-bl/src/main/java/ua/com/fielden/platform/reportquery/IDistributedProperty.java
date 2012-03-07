package ua.com.fielden.platform.reportquery;

import ua.com.fielden.platform.basic.IPropertyEnum;

public interface IDistributedProperty extends IPropertyEnum, Comparable<IPropertyEnum> {

    void setTableAlias(final String alias);

    String getTableAlias();

    String getParsedValue();

    String getActualProperty();

    String getName();

    String getDesc();

    boolean isExpression();
}

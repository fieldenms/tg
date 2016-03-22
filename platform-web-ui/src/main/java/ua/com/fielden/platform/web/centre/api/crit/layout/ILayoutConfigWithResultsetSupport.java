package ua.com.fielden.platform.web.centre.api.crit.layout;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder0Checkbox;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder2Properties;

public interface ILayoutConfigWithResultsetSupport<T extends AbstractEntity<?>> extends ILayoutConfig<T>, IResultSetBuilder0Checkbox<T> {

}

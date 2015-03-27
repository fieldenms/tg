package ua.com.fielden.platform.web.centre.api.crit.layout;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder;
import ua.com.fielden.platform.web.view.master.api.helpers.ILayoutConfig;

public interface ILayoutConfigWithResultsetSupport<T extends AbstractEntity<?>> extends ILayoutConfig<T>, IResultSetBuilder<T> {

}

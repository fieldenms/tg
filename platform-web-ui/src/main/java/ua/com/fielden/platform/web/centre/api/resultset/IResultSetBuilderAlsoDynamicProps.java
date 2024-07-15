package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.layout.ICollapsedCardLayoutConfig;

public interface IResultSetBuilderAlsoDynamicProps<T extends AbstractEntity<?>> extends ICollapsedCardLayoutConfig<T>{

    IResultSetBuilderDynamicProps<T> also();
}

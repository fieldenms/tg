package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IResultSetBuilderAlsoDynamicProps<T extends AbstractEntity<?>> extends IResultSetBuilder5aTitleBar<T>{

    IResultSetBuilderDynamicProps<T> also();
}

package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IAlsoProp<T extends AbstractEntity<?>> {
    IResultSetBuilder<T> also();
}
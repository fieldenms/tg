package ua.com.fielden.platform.equery.interfaces;

import ua.com.fielden.platform.equery.RootEntityMapper;

public interface IQueryToken {
    String getSql(RootEntityMapper alias);
}

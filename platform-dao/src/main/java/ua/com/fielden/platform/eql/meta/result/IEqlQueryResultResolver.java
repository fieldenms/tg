package ua.com.fielden.platform.eql.meta.result;

import java.util.List;

public interface IEqlQueryResultResolver {
    IEqlQueryResultItem resolve(List<String> path);
}

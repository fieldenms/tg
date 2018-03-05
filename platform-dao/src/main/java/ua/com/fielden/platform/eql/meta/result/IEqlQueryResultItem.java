package ua.com.fielden.platform.eql.meta.result;

public interface IEqlQueryResultItem<T> extends IEqlQueryResultResolver {
    String getName();
    
    Class<T> getJavaType();
}
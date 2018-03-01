package ua.com.fielden.platform.eql.meta.result;

public interface IEqlQueryResultItem extends IEqlQueryResultResolver {
    IEqlQueryResultParent getParent();
    String getName();

}

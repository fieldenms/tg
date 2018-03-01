package ua.com.fielden.platform.eql.meta.result;

import java.util.SortedMap;

public interface IEqlQueryResultParent extends IEqlQueryResultResolver{
    
    SortedMap<String, IEqlQueryResultItem> getItems();
    
}

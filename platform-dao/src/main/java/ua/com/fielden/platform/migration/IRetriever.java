package ua.com.fielden.platform.migration;

import java.util.List;
import java.util.SortedMap;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Specifies a contract for implementing SQL statement retrieving an entity items of certain type.
 * 
 * @author TG Team
 * 
 */
public interface IRetriever<T extends AbstractEntity<?>> {

    SortedMap<String, String> resultFields();

    String fromSql();

    String whereSql();

    List<String> groupSql();

    List<String> orderSql();

    /**
     * Returns entity type information.
     * 
     * @return
     */
    Class<T> type();

    boolean isUpdater();
}
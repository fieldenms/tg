package ua.com.fielden.platform.equery.interfaces;

import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.ColumnInfo;

/**
 * Contract for hibernate mapping information retriever.
 *
 * @author TG Team
 *
 */
public interface IMappingExtractor {
    /**
     * Retrieves the name of the table to which given entity type is mapped.
     *
     * @param entityType
     * @return
     */
    String getTableClause(Class<? extends AbstractEntity> entityType);

    Map<String, ColumnInfo> getColumns(Class<? extends AbstractEntity> entityType, final IEntityMapper parent);
}

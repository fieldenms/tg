package ua.com.fielden.platform.eql.meta.result;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;

public class EqlQueryResultItemForUnionEntityType<T extends AbstractUnionEntity> extends AbstractEqlQueryResultItem<T> implements IEqlQueryResultParent, IEqlQueryResultItem<T> {
    private final SortedMap<String, IEqlQueryResultItem<? extends AbstractEntity<?>>> items = new TreeMap<>();
    
    public EqlQueryResultItemForUnionEntityType(final String name, final Class<T> javaType, final Set<IEqlQueryResultItem<? extends AbstractEntity<?>>> items) {
        super(name, javaType);
        for (final IEqlQueryResultItem<? extends AbstractEntity<?>> item : items) {
            this.items.put(item.getName(), item);
        }
    }

    @Override
    public EqlPropResolutionProgress resolve(final EqlPropResolutionProgress resolutionProgress) {
        // TODO Auto-generated method stub
        return resolutionProgress;
    }
}
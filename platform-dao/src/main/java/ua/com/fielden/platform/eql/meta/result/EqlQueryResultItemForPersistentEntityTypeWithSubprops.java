package ua.com.fielden.platform.eql.meta.result;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractEntity;

public class EqlQueryResultItemForPersistentEntityTypeWithSubprops<T extends AbstractEntity<?>> extends AbstractEqlQueryResultItem<T> implements IEqlQueryResultParent, IEqlQueryResultItem<T> {
    private final SortedMap<String, IEqlQueryResultItem<?>> items = new TreeMap<>();
    
    public EqlQueryResultItemForPersistentEntityTypeWithSubprops(final String name, final Class<T> javaType, final Set<IEqlQueryResultItem<?>> items) {
        super(name, javaType);
        for (final IEqlQueryResultItem<?> item : items) {
            this.items.put(item.getName(), item);
        }
    }

    @Override
    public EqlPropResolutionProgress resolve(final EqlPropResolutionProgress resolutionProgress) {
        // TODO Auto-generated method stub
        return null;
    }
}
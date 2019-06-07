package ua.com.fielden.platform.eql.meta.result;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgPersonName;

public class EqlQueryResultForPersistentEntityTable<T extends AbstractEntity<?>> implements IEqlQueryResult {

    private final Class<T> javaType;
    private final SortedMap<String, IEqlQueryResultItem<?>> items = new TreeMap<>();
    
    public EqlQueryResultForPersistentEntityTable(final Class<T> javaType, final Set<IEqlQueryResultItem<?>> items) {
        this.javaType = javaType;
        for (final IEqlQueryResultItem<?> item : items) {
            this.items.put(item.getName(), item);
        }
    }

    @Override
    public String getSqlIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends AbstractEntity<?>> getJavaType() {
        return javaType;
    }

    @Override
    public EqlPropResolutionProgress resolve(final EqlPropResolutionProgress resolutionProgress) {
        final String propName = resolutionProgress.getPathToResolve().get(0);
        final IEqlQueryResultItem<?> item = items.get(propName);
        if (item != null) {
            final List<IEqlQueryResultItem<?>> newResolved = new ArrayList<>(resolutionProgress.getResolved());
            newResolved.add(item);
            return item.resolve(new EqlPropResolutionProgress(resolutionProgress.getPathToResolve().subList(0, resolutionProgress.getPathToResolve().size() - 1), newResolved));
        }
        
        return resolutionProgress;
    }
    
    public static void main(final String[] args) {
        final Set<IEqlQueryResultItem<?>> items = new HashSet<>();
        items.add(new EqlQueryResultItemForPrimitiveType<>("id", Long.class));
        items.add(new EqlQueryResultItemForPrimitiveType<>("surname", String.class));
        items.add(new EqlQueryResultItemForPrimitiveType<>("patronymic", String.class));
        items.add(new EqlQueryResultItemForPersistentEntityType<TgPersonName>("name", TgPersonName.class));
        
        
        
        final EqlQueryResultForPersistentEntityTable<TgAuthor> author = new EqlQueryResultForPersistentEntityTable<TgAuthor>(TgAuthor.class, items);
        System.out.println(author.resolve( new EqlPropResolutionProgress(asList(new String[] {"surname"}), emptyList())));
        System.out.println(author.resolve( new EqlPropResolutionProgress(asList(new String[] {"patronymic"}), emptyList())));
        System.out.println(author.resolve( new EqlPropResolutionProgress(asList(new String[] {"desc"}), emptyList())));
        System.out.println(author.resolve( new EqlPropResolutionProgress(asList(new String[] {"name"}), emptyList())));
        System.out.println(author.resolve( new EqlPropResolutionProgress(asList(new String[] {"id"}), emptyList())));



        
    }
}

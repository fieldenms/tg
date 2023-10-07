package ua.com.fielden.platform.eql.meta;

import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.eql.meta.EntityCategory.PERSISTENT;
import static ua.com.fielden.platform.eql.meta.EntityCategory.PURE;
import static ua.com.fielden.platform.eql.meta.EntityTypeInfo.getEntityTypeInfo;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.generateTable;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.generateTableWithPropColumnInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation.TableStructForBatchInsertion;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.eql.stage3.Table;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;

public class EqlEntityMetadataHolder {
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, EqlEntityMetadata<?>> entityPropsMetadata;
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, Table> tables = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, TableStructForBatchInsertion> tableStructsForBatchInsertion = new ConcurrentHashMap<>();

    private final EqlEntityMetadataGenerator eemg;

    public EqlEntityMetadataHolder(final List<Class<? extends AbstractEntity<?>>> entityTypes, final EqlEntityMetadataGenerator eemg) {
        this.entityPropsMetadata = new ConcurrentHashMap<>(entityTypes.size());
        this.eemg = eemg;
        entityTypes.parallelStream().forEach(entityType -> {
            try {
                final EntityTypeInfo<? extends AbstractEntity<?>> parentInfo = getEntityTypeInfo(entityType);
                if (parentInfo.category != PURE) {
                    final EqlEntityMetadataPair<? extends AbstractEntity<?>> pd = eemg.generate(getEntityTypeInfo(entityType), entityType);
                    entityPropsMetadata.put(pd.entityType(), pd.eqlEntityMetadata());

                    if (parentInfo.category == PERSISTENT) {
                        tables.put(entityType, generateTable(parentInfo.tableName, pd.eqlEntityMetadata().props()));
                        tableStructsForBatchInsertion.put(entityType.getName(), generateTableWithPropColumnInfo(parentInfo.tableName, pd.eqlEntityMetadata().props()));
                    }
                }
            } catch (final Exception ex) {
                throw new EqlMetadataGenerationException("Couldn't generate persistence metadata for entity [" + entityType + "].", ex);
            }
        });
    }
    
    public Map<Class<? extends AbstractEntity<?>>, EqlEntityMetadata<?>> entityPropsMetadata() {
        return unmodifiableMap(entityPropsMetadata);
    }
    
    public Table getTableForEntityType(final Class<? extends AbstractEntity<?>> entityType) {
        return tables.get(DynamicEntityClassLoader.getOriginalType(entityType));
    }

    public TableStructForBatchInsertion getTableStructsForBatchInsertion(final Class<? extends AbstractEntity<?>> entityType) {
        return tableStructsForBatchInsertion.get(entityType.getName());
    }
    
    public <ET extends AbstractEntity<?>> EqlEntityMetadata<ET> obtainEqlEntityMetadata(final Class<ET> entityType) {
        @SuppressWarnings("unchecked")
        final EqlEntityMetadata<ET> existing = (EqlEntityMetadata<ET>) entityPropsMetadata.get(entityType);
        return existing != null ? existing : eemg.generate(getEntityTypeInfo(entityType), entityType).eqlEntityMetadata();
    }
}
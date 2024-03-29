package ua.com.fielden.platform.eql.meta;

import java.util.List;
import java.util.Map;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation.TableStructForBatchInsertion;

public class EqlDomainMetadata {

    public final EqlEntityMetadataHolder entityMetadataHolder;
    public final QuerySourceInfoProvider querySourceInfoProvider;
    public final DbVersion dbVersion;

    public EqlDomainMetadata(//
            final Map<Class<?>, Class<?>> hibTypesDefaults, //
            final Injector hibTypesInjector, //
            final List<Class<? extends AbstractEntity<?>>> entityTypes, //
            final DbVersion dbVersion) {
        this.dbVersion = dbVersion;
        this.entityMetadataHolder = new EqlEntityMetadataHolder(entityTypes, new EqlEntityMetadataGenerator(hibTypesDefaults, hibTypesInjector, dbVersion));
        this.querySourceInfoProvider = new QuerySourceInfoProvider(entityMetadataHolder);
    }
    
    public TableStructForBatchInsertion getTableForEntityType(final Class<? extends AbstractEntity<?>> entityType) {
        return entityMetadataHolder.getTableStructsForBatchInsertion(entityType);
    }

    public Map<Class<? extends AbstractEntity<?>>, EqlEntityMetadata<?>> entityPropsMetadata() {
        return entityMetadataHolder.entityPropsMetadata();
    }
}
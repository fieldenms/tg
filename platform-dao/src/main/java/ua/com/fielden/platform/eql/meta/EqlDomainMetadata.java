package ua.com.fielden.platform.eql.meta;

import com.google.inject.Injector;
import org.hibernate.dialect.Dialect;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation.TableStructForBatchInsertion;
import ua.com.fielden.platform.persistence.HibernateHelpers;

import java.util.List;
import java.util.Map;

public class EqlDomainMetadata {

    public final EqlEntityMetadataHolder entityMetadataHolder;
    public final QuerySourceInfoProvider querySourceInfoProvider;
    public final DbVersion dbVersion;
    public final Dialect dialect;

    public EqlDomainMetadata(
            final Map<Class<?>, Class<?>> hibTypesDefaults,
            final Injector hibTypesInjector,
            final List<Class<? extends AbstractEntity<?>>> entityTypes,
            final Dialect dialect)
    {
        this.dbVersion = HibernateHelpers.getDbVersion(dialect);
        this.dialect = dialect;
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

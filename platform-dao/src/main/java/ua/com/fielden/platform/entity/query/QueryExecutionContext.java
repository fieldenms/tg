package ua.com.fielden.platform.entity.query;

import org.hibernate.Session;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadata;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.utils.IDates;

public class QueryExecutionContext {
    private final Session session;
    private final EntityFactory entityFactory;
    private final ICompanionObjectFinder coFinder;

    private final DomainMetadata domainMetadata;
    private final EqlDomainMetadata eqlDomainMetadata;
    private final IFilter filter;
    private final String username;
    private final IDates dates;
    private final IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache;
    
    public QueryExecutionContext(final Session session, final EntityFactory entityFactory, final ICompanionObjectFinder coFinder, final DomainMetadata domainMetadata, final EqlDomainMetadata eqlDomainMetadata, final IFilter filter, final String username, final IDates dates, final IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache) {
        this.session = session;
        this.entityFactory = entityFactory;
        this.coFinder = coFinder;
        this.domainMetadata = domainMetadata;
        this.eqlDomainMetadata = eqlDomainMetadata;
        this.filter = filter;
        this.username = username;
        this.dates = dates;
        this.idOnlyProxiedEntityTypeCache = idOnlyProxiedEntityTypeCache;
    }

    public Session getSession() {
        return session;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public ICompanionObjectFinder getCoFinder() {
        return coFinder;
    }

    public DomainMetadataAnalyser produceDomainMetadataAnalyser() {
        return new DomainMetadataAnalyser(domainMetadata);
    }

    public EqlDomainMetadata getEqlDomainMetadata() {
        return eqlDomainMetadata;
    }

    public IFilter getFilter() {
        return filter;
    }

    public String getUsername() {
        return username;
    }

    public IDates dates() {
        return dates;
    }

    public IdOnlyProxiedEntityTypeCache getIdOnlyProxiedEntityTypeCache() {
        return idOnlyProxiedEntityTypeCache;
    }
}
package ua.com.fielden.platform.entity.query;

import org.hibernate.Session;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class QueryExecutionContext {
    private final Session session;
    private final EntityFactory entityFactory;
    private final ICompanionObjectFinder coFinder;

    private final DomainMetadata domainMetadata;
    private final IFilter filter;
    private final String username;
    private final IUniversalConstants universalConstants;
    private final IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache;
    
    public QueryExecutionContext(Session session, EntityFactory entityFactory, ICompanionObjectFinder coFinder, DomainMetadata domainMetadata, IFilter filter, String username, IUniversalConstants universalConstants, final IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache) {
        super();
        this.session = session;
        this.entityFactory = entityFactory;
        this.coFinder = coFinder;
        this.domainMetadata = domainMetadata;
        this.filter = filter;
        this.username = username;
        this.universalConstants = universalConstants;
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

    public DomainMetadata getDomainMetadata() {
        return domainMetadata;
    }

    public IFilter getFilter() {
        return filter;
    }

    public String getUsername() {
        return username;
    }

    public IUniversalConstants getUniversalConstants() {
        return universalConstants;
    }

    public IdOnlyProxiedEntityTypeCache getIdOnlyProxiedEntityTypeCache() {
        return idOnlyProxiedEntityTypeCache;
    }
}
package ua.com.fielden.platform.web.resources;

import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.google.inject.Injector;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.file_reports.IReportDaoFactory;
import ua.com.fielden.platform.web.factories.AttachmentDownloadResourceFactory;
import ua.com.fielden.platform.web.factories.AttachmentInstanceResourceFactory;
import ua.com.fielden.platform.web.factories.AttachmentQueryResourceFactory;
import ua.com.fielden.platform.web.factories.AttachmentTypeResourceFactory;
import ua.com.fielden.platform.web.factories.EntityAggregatesQueryExportResourceFactory;
import ua.com.fielden.platform.web.factories.EntityAggregatesQueryResourceFactory;
import ua.com.fielden.platform.web.factories.EntityInstanceResourceFactory;
import ua.com.fielden.platform.web.factories.EntityLifecycleResourceFactory;
import ua.com.fielden.platform.web.factories.EntityQueryExportResourceFactory;
import ua.com.fielden.platform.web.factories.EntityTypeResourceFactory;
import ua.com.fielden.platform.web.factories.FileUploadResourceFactory;
import ua.com.fielden.platform.web.factories.GeneratedEntityQueryExportResourceFactory;
import ua.com.fielden.platform.web.factories.GeneratedEntityQueryResourceFactory;
import ua.com.fielden.platform.web.factories.ReportResourceFactory;
import ua.com.fielden.platform.web.factories.SnappyQueryRestlet;

/**
 * Provides convenient methods for routing standard entity resources.
 *
 * @author TG Team
 *
 */
public final class RouterHelper {
    private final Injector injector;
    private final EntityFactory factory;

    public RouterHelper(final Injector injector, final EntityFactory factory) {
        this.injector = injector;
        this.factory = factory;
    }

    public <T extends AbstractEntity<?>, DAO extends IEntityDao<T>> void register(final Router router, final Class<DAO> daoType) {
        registerInstanceResource(router, daoType);
        registerTypeResource(router, daoType);
        registerExportResource(router, daoType);
        registerLifecycleResource(router, daoType);
    }

    public void registerSnappyQueryResource(final Router router) {
        router.attach("/users/{username}/snappyquery", new SnappyQueryRestlet(injector));
    }

    public void registerAggregates(final Router router) {
        router.attach("/users/{username}/query/" + EntityAggregates.class.getSimpleName(), new EntityAggregatesQueryResourceFactory(injector));
        router.attach("/users/{username}/export/" + EntityAggregates.class.getSimpleName(), new EntityAggregatesQueryExportResourceFactory(injector, factory));
    }

    public void registerGeneratedTypeResources(final Router router) {
        router.attach("/users/{username}/query/generated-type/{type}", new GeneratedEntityQueryResourceFactory(injector, router));
        router.attach("/users/{username}/export/generated-type/{type}", new GeneratedEntityQueryExportResourceFactory(injector, router));
    }

    /**
     * Registers all the necessary resources for {@link Attachment} with the router.
     *
     * @param router
     * @param location
     */
    public void registerAttachment(final Router router, final String location) {
        final Restlet typeResource = new AttachmentTypeResourceFactory(location, injector, factory);
        router.attach("/users/{username}/" + Attachment.class.getSimpleName(), typeResource);

        final Restlet queryResource = new AttachmentQueryResourceFactory(injector, router);
        router.attach("/users/{username}/query/" + Attachment.class.getSimpleName(), queryResource);

        final Restlet queryExportResource = new EntityQueryExportResourceFactory<Attachment, IAttachment>(router, IAttachment.class, injector);
        router.attach("/users/{username}/export/" + Attachment.class.getSimpleName(), queryExportResource);

        final Restlet downloadResource = new AttachmentDownloadResourceFactory(location, injector);
        router.attach("/users/{username}/download/" + Attachment.class.getSimpleName() + "/{entity-id}", downloadResource);

        final Restlet instanceResource = new AttachmentInstanceResourceFactory(location, injector, factory);
        router.attach("/users/{username}/" + Attachment.class.getSimpleName() + "/{entity-id}", instanceResource);
    }
    
    public void registerFileUploadResource(final Router router) {
        final FileUploadResourceFactory factory = new FileUploadResourceFactory(injector);
        router.attach("/file-processing/{processor-type}", factory);
    }

    public <T extends AbstractEntity<?>, DAO extends IEntityDao<T>> void registerInstanceResource(final Router router, final Class<DAO> daoType) {
        final DAO dao = injector.getInstance(daoType); // needed just to get entity type... might need to optimise it
        final Restlet instanceResource = new EntityInstanceResourceFactory<T, DAO>(daoType, injector, factory);
        router.attach("/users/{username}/" + dao.getEntityType().getSimpleName() + "/{entity-id}", instanceResource);
    }

    public <T extends AbstractEntity<?>, DAO extends IEntityDao<T>> void registerTypeResource(final Router router, final Class<DAO> daoType) {
        final DAO dao = injector.getInstance(daoType); // needed just to get entity type... might need to optimize it
        final Restlet typeResource = new EntityTypeResourceFactory<T, DAO>(daoType, injector, factory, router);
        router.attach("/users/{username}/" + dao.getEntityType().getSimpleName(), typeResource);
        router.attach("/users/{username}/query/" + dao.getEntityType().getSimpleName(), typeResource);
    }

    public <T extends AbstractEntity<?>, DAO extends IEntityDao<T>> void registerExportResource(final Router router, final Class<DAO> daoType) {
        final DAO dao = injector.getInstance(daoType); // needed just to get entity type... might need to optimize it
        final Restlet queryExportResource = new EntityQueryExportResourceFactory<T, DAO>(router, daoType, injector);
        router.attach("/users/{username}/export/" + dao.getEntityType().getSimpleName(), queryExportResource);
    }

    public <T extends AbstractEntity<?>, DAO extends IEntityDao<T>> void registerLifecycleResource(final Router router, final Class<DAO> daoType) {
        final DAO dao = injector.getInstance(daoType); // needed just to get entity type... might need to optimize it
        final Restlet lifecycleResource = new EntityLifecycleResourceFactory<T, DAO>(daoType, injector);
        router.attach("/users/{username}/lifecycle/" + dao.getEntityType().getSimpleName(), lifecycleResource);
    }

    public void registerReportResource(final Router router, final RestServerUtil rsu) {
        router.attach("/users/{username}/report", new ReportResourceFactory(injector.getInstance(IReportDaoFactory.class), rsu, injector));
    }

    public Injector getInjector() {
        return injector;
    }

    public EntityFactory getFactory() {
        return factory;
    }

}

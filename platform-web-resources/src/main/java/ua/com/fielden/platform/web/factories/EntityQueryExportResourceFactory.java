package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.routing.Router;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.resources.EntityQueryExportResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * This is {@link Restlet} implementation that provides logic for correct entity export resource instantiation. Specifically, it should be used to instantiate
 * {@link EntityQueryExportResource} for specific entity types.
 *
 * @author TG Team
 *
 */
public class EntityQueryExportResourceFactory<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends Restlet {
    private final Class<DAO> daoType;
    private final Injector injector;
    private final RestServerUtil restUtil;
    private final Router router;

    /**
     * Instances of DAO and factory should be thread-safe as they are used by multiple instances of resources serving concurrent requests.
     */
    public EntityQueryExportResourceFactory(final Router router, final Class<DAO> daoType, final Injector injector) {
        this.daoType = daoType;
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.router = router;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.POST.equals(request.getMethod())) {
            final DAO dao = injector.getInstance(daoType);

            final String username = (String) request.getAttributes().get("username");
            injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserController.class));

            new EntityQueryExportResource<T>(router, injector, dao, restUtil, getContext(), request, response).handle();
        }
    }
}

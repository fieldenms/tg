package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.ILifecycleDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.resources.EntityLifecycleResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * This is {@link Restlet} implementation that provides logic for correct entity lifecycle resource instantiation. Specifically, it should be used to instantiate
 * {@link EntityLifecycleResource} for specific entity types.
 *
 * @author TG Team
 *
 */
@SuppressWarnings("unchecked")
public class EntityLifecycleResourceFactory<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends Restlet {
    private final Class<DAO> daoType;
    private final Injector injector;
    private final RestServerUtil restUtil;

    /**
     * Instances of DAO and factory should be thread-safe as they are used by multiple instances of resources serving concurrent requests.
     *
     * @param dao
     * @param factory
     */
    public EntityLifecycleResourceFactory(final Class<DAO> daoType, final Injector injector) {
        this.daoType = daoType;
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        final DAO dao = injector.getInstance(daoType);
        if (dao instanceof ILifecycleDao) {
            if (Method.POST.equals(request.getMethod())) {
                final String username = (String) request.getAttributes().get("username");
                injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserController.class));

                new EntityLifecycleResource<T>((ILifecycleDao<T>) dao, restUtil, getContext(), request, response).handle();
            }
        }
    }
}
package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.security.provider.IUserEx;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.resources.EntityInstanceResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * This is {@link Restlet} implementation that provides logic for correct entity oriented resource instantiation. Specifically, it should be used to instantiate
 * {@link EntityInstanceResource} for specific entity types.
 *
 * @author TG Team
 *
 */
public class EntityInstanceResourceFactory<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends Restlet {
    private final Class<DAO> daoType;
    private final Injector injector;
    private final EntityFactory factory;
    private final RestServerUtil restUtil;

    /**
     * Instances of DAO and factory should be thread-safe as they are used by multiple instances of resources serving concurrent requests.
     */
    public EntityInstanceResourceFactory(final Class<DAO> daoType, final Injector injector, final EntityFactory factory) {
        this.daoType = daoType;
        this.injector = injector;
        this.factory = factory;
        this.restUtil = injector.getInstance(RestServerUtil.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        final DAO dao = injector.getInstance(daoType);

        final String username = (String) request.getAttributes().get("username");
        injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserEx.class));

        final EntityInstanceResource<T> resource = new EntityInstanceResource<T>(dao, factory, restUtil, getContext(), request, response);
        if (Method.GET == request.getMethod() || Method.HEAD == request.getMethod() || Method.POST == request.getMethod() || Method.DELETE == request.getMethod()) {
            resource.handle();
        }
    }
}

package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.security.provider.IUserEx;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.FunctionalEntityResource;

import com.google.inject.Injector;

public class FunctionalEntityResourceFactory<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends Restlet {

    private final Injector injector;
    private final RestServerUtil restUtil;
    private final Class<DAO> daoType;

    public FunctionalEntityResourceFactory(final Class<DAO> daoType, final Injector injector) {
        this.daoType = daoType;
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        final String username = (String) request.getAttributes().get("username");
        injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserEx.class));

        if (Method.POST == request.getMethod()) {
            final DAO dao = injector.getInstance(daoType);
            final FunctionalEntityResource<T, DAO> resource = new FunctionalEntityResource<>(dao, injector.getInstance(EntityFactory.class), restUtil, getContext(), request, response);
            resource.handle();
        }
    }
}

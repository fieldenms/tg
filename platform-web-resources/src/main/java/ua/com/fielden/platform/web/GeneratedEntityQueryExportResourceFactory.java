package ua.com.fielden.platform.web;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.routing.Router;

import ua.com.fielden.platform.dao.DynamicEntityDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.resources.GeneratedEntityQueryExportResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * This is {@link Restlet} implementation that provides logic for correct entity export resource instantiation. Specifically, it should be used to instantiate
 * {@link GeneratedEntityQueryExportResource}.
 *
 * @author TG Team
 *
 */
public class GeneratedEntityQueryExportResourceFactory extends Restlet {
    private final Injector injector;
    private final RestServerUtil restUtil;
    private final Router router;

    public GeneratedEntityQueryExportResourceFactory(final Injector injector, final Router router) {
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.router = router;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.POST.equals(request.getMethod())) {
            final DynamicEntityDao dao = injector.getInstance(DynamicEntityDao.class);

            final String username = (String) request.getAttributes().get("username");
            injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserController.class));

            try {
                final String origEntityTypeName = (String) request.getAttributes().get("type");
                final Class<? extends AbstractEntity<?>> origEntityType = (Class<? extends AbstractEntity<?>>) Class.forName(origEntityTypeName);
                final IEntityDao companion = injector.getInstance(ICompanionObjectFinder.class).find(origEntityType);
                new GeneratedEntityQueryExportResource(router, injector, companion, restUtil, getContext(), request, response).handle();

            } catch (final ClassNotFoundException ex) {
                ex.printStackTrace();
                response.setEntity(restUtil.errorRepresentation(ex));
            }

            //new GeneratedEntityQueryExportResource(router, injector, dao, restUtil, getContext(), request, response).handle();
        }
    }
}

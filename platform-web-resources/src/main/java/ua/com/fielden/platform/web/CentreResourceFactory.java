package ua.com.fielden.platform.web;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.ui.config.IEntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfigController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.resources.CentreResource;

import com.google.inject.Injector;

/**
 * The server resource factory for entity centres;
 *
 * @author TG Team
 *
 */
public class CentreResourceFactory extends Restlet {
    private final Map<String, EntityCentre> centres;
    private final IGlobalDomainTreeManager gdtm;

    /**
     * Creates the {@link CentreResourceFactory} instance with map of available entity centres and {@link GlobalDomainTreeManager} instance (will be removed or enhanced later.)
     *
     * @param centres
     * @param username
     * @param injector
     */
    public CentreResourceFactory(final Map<String, EntityCentre> centres, final String username, final Injector injector) {
        this.centres = centres;

        this.gdtm = new GlobalDomainTreeManager(injector.getInstance(ISerialiser.class), injector.getInstance(ISerialiser0.class), injector.getInstance(EntityFactory.class), createUserProvider(username, injector.getInstance(IUserDao.class)), injector.getInstance(IMainMenuItemController.class), injector.getInstance(IEntityCentreConfigController.class), injector.getInstance(IEntityCentreAnalysisConfig.class), injector.getInstance(IEntityMasterConfigController.class), injector.getInstance(IEntityLocatorConfigController.class));
    }

    /**
     * A stub for user provider TODO should be removed after the authentication mechanism will be implemented.
     *
     * @param userName
     * @param userController
     * @return
     */
    private static IUserProvider createUserProvider(final String userName, final IUserDao userController) {
	final User user = userController.findByKeyAndFetch(fetchAll(User.class), userName);
        final IUserProvider userProvider = new IUserProvider() {
            @Override
            public User getUser() {
                return user;
            }

            @Override
            public void setUsername(final String username, final IUserController controller) {
            }
        };
        return userProvider;
    }

    @Override
    /**
     * Invokes on GET request from client.
     */
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new CentreResource(centres.get(request.getAttributes().get("centreName")), getContext(), request, response, gdtm).handle();
        }
    }
}

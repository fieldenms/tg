package ua.com.fielden.platform.web.factories.webui;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import java.util.Optional;

import org.restlet.Request;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * Contains a set of utilities for Web UI resource factories.
 *
 * @author TG Team
 *
 */
public class ResourceFactoryUtils {

    /**
     * Finds the entity master using 'entityType' request attribute inside 'webUiConfig'.
     *
     * @param request
     * @param webUiConfig
     * @return
     */
    static EntityMaster<? extends AbstractEntity<?>> getEntityMaster(final Request request, final IWebUiConfig webUiConfig) {
        return getEntityMaster((String) request.getAttributes().get("entityType"), webUiConfig);
    }

    /**
     * Finds the entity master using 'entityTypeString' inside 'webUiConfig'.
     *
     * @param entityTypeString
     * @param webUiConfig
     * @return
     */
    public static EntityMaster<? extends AbstractEntity<?>> getEntityMaster(final String entityTypeString, final IWebUiConfig webUiConfig) {
        final Class<? extends AbstractEntity<?>> entityType = (Class<? extends AbstractEntity<?>>) ClassesRetriever.findClass(entityTypeString);
        return webUiConfig.getMasters().get(entityType);
    }

    /**
     * Finds the entity centre using 'miType' request attribute inside 'webUiConfig'.
     *
     * @param request
     * @param webUiConfig
     * @return
     */
    static EntityCentre<AbstractEntity<?>> getEntityCentre(final Request request, final IWebUiConfig webUiConfig) {
        return getEntityCentre((String) request.getAttributes().get("mitype"), webUiConfig);
    }
    
    /**
     * Determines 'saveAsName' from corresponding centre's request attribute.
     *
     * @param request
     * @return
     */
    static Optional<String> saveAsName(final Request request) {
        final String saveAsName = ((String) request.getAttributes().get("saveAsName")).replaceFirst("default", "").replace("%20", " ");
        return "".equals(saveAsName) ? empty() : of(saveAsName);
    }
    
    /**
     * Finds the entity centre using 'mitypeString' inside 'webUiConfig'.
     *
     * @param mitypeString
     * @param webUiConfig
     * @return
     */
    public static EntityCentre<AbstractEntity<?>> getEntityCentre(final String mitypeString, final IWebUiConfig webUiConfig) {
        final Class<MiWithConfigurationSupport<AbstractEntity<?>>> miType = (Class<MiWithConfigurationSupport<AbstractEntity<?>>>) ClassesRetriever.findClass(mitypeString);
        return (EntityCentre<AbstractEntity<?>>) webUiConfig.getCentres().get(miType);
    }

    public static AbstractCustomView getCustomView(final String viewName, final IWebUiConfig webUiConfig) {
        return webUiConfig.getCustomViews().get(viewName);
    }
}

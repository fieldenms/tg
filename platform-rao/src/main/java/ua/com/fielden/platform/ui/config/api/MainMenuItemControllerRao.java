package ua.com.fielden.platform.ui.config.api;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.rao.WebResourceType;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.MainMenuItem;

import com.google.inject.Inject;

/**
 * RAO implementation of {@link IMainMenuItemController}.
 * 
 * @author TG Team
 * 
 */
@EntityType(MainMenuItem.class)
public class MainMenuItemControllerRao extends CommonEntityRao<MainMenuItem> implements IMainMenuItemController {

    @Inject
    public MainMenuItemControllerRao(final RestClientUtil restUtil, final IEntityCentreConfig eccController) {
        super(restUtil);
    }

    @Override
    protected WebResourceType getDefaultWebResourceType() {
        return WebResourceType.SYSTEM;
    }

}

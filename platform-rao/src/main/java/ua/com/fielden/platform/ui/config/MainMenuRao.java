package ua.com.fielden.platform.ui.config;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link IMainMenu}.
 * 
 * @author Developers
 *
 */
@EntityType(MainMenu.class)
public class MainMenuRao extends CommonEntityRao<MainMenu> implements IMainMenu {

    @Inject
    public MainMenuRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

}
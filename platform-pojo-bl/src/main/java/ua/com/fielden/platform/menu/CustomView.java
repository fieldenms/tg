package ua.com.fielden.platform.menu;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyTitle;

/**
 * Represents the custom view.
 *
 * @author TG Team
 *
 */
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ICustomView.class)
public class CustomView extends AbstractView {
    private static final long serialVersionUID = 1L;
}
package ua.com.fielden.wizard;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.view.BasePanel;

/**
 * A base class to be used for implementing wizard pages.
 * 
 * @author TG Team
 * 
 */
public abstract class AbstractWizPage<T extends AbstractEntity<?>> extends BasePanel {

    private static final long serialVersionUID = 1L;

    /**
     * Should be implemented by subtypes in order to provide custom page UI.
     * 
     * @param editors
     */
    public abstract void buildUi(final Wizard<T> wizard);

}

package ua.com.fielden.platform.swing.analysis;

import java.awt.Dimension;

import javax.swing.JComponent;

import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.ICloseHook;
import ua.com.fielden.platform.utils.ResourceLoader;

public class DetailsFrame extends BaseFrame {

    private static final long serialVersionUID = -31309231903438043L;

    private final Object associatedEntity;

    public DetailsFrame(final Object associatedEntity, final String title, final JComponent component, final ICloseHook<DetailsFrame> hook) {
        super(hook);
        this.associatedEntity = associatedEntity;
        setTitle(title);
        setIconImage(ResourceLoader.getImage("images/tg-icon.png"));
        setPreferredSize(new Dimension(1000, 500));
        pack();
        RefineryUtilities.centerFrameOnScreen(this);

        add(component);
    }

    public Object getAssociatedEntity() {
        return associatedEntity;
    }
}

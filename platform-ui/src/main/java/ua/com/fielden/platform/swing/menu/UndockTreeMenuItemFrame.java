package ua.com.fielden.platform.swing.menu;

import java.awt.LayoutManager;
import java.util.Map;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.BasePanel;
import ua.com.fielden.platform.swing.view.ICloseHook;
import ua.com.fielden.platform.swing.view.IEntityMasterCache;
import ua.com.fielden.platform.utils.ResourceLoader;

public class UndockTreeMenuItemFrame extends BaseFrame {

    private static final long serialVersionUID = -7668607671657970722L;

    private final BlockingIndefiniteProgressPane blockingPane;

    private final BasePanel view;

    public UndockTreeMenuItemFrame(final String title, final ICloseHook<UndockTreeMenuItemFrame> hook, final BasePanel view, final Map<Class<? extends AbstractEntity<?>>, IEntityMasterCache> cache) {
        this(title, new MigLayout("fill, insets 0", "[fill,grow]", "[fill, grow]"), hook, view, cache);
    }

    public UndockTreeMenuItemFrame(final String title, final LayoutManager layoutManager, final ICloseHook<UndockTreeMenuItemFrame> hook, final BasePanel view, final Map<Class<? extends AbstractEntity<?>>, IEntityMasterCache> cache) {
        super(title, layoutManager, hook, cache);
        blockingPane = new BlockingIndefiniteProgressPane(this);
        setIconImage(ResourceLoader.getImage("images/tg-icon.png"));
        this.view = view;
    }

    public BlockingIndefiniteProgressPane getBlockingPane() {
        return blockingPane;
    }

    public BasePanel getView() {
        return view;
    }
}

package ua.com.fielden.platform.client.ui.menu.dumpper;

import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItem;

public interface ITreeNodeVisitor {

    MainMenuItem visit(TreeMenuItem item, MainMenuItem parent, int order);
}

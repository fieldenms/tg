package ua.com.fielden.platform.swing.view;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.model.UModel;

public enum ViewToolbars {

    NO_DELETE_ACTION {

	@Override
	public JPanel toolbar(final UModel model) {
	    final JPanel actionPanel = new JPanel(new MigLayout("fill, insets 0", "[fill,:100:][fill,:100:]20[fill,:100:][fill,:100:]20:push[fill,:100:]", "[c]"));
	    actionPanel.add(new JButton(model.getNewAction()));
	    actionPanel.add(new JButton(model.getEditAction()));
	    actionPanel.add(new JButton(model.getSaveAction()));
	    actionPanel.add(new JButton(model.getCancelAction()));
	    actionPanel.add(new JButton(model.getRefreshAction()));
	    return actionPanel;
	}

    },

    NO_NEW_DELETE_ACTION {

	@Override
	public JPanel toolbar(final UModel model) {
	    final JPanel actionPanel = new JPanel(new MigLayout("fill, insets 0", "[fill,:100:]20[fill,:100:][fill,:100:]20:push[fill,:100:]", "[c]"));
	    actionPanel.add(new JButton(model.getEditAction()));
	    actionPanel.add(new JButton(model.getSaveAction()));
	    actionPanel.add(new JButton(model.getCancelAction()));
	    actionPanel.add(new JButton(model.getRefreshAction()));
	    return actionPanel;
	}

    },

    NO_NEW_ACTION {

	@Override
	public JPanel toolbar(final UModel model) {
	    final JPanel actionPanel = new JPanel(new MigLayout("fill, insets 0", "[fill,:100:]20[fill,:100:][fill,:100:]20[fill,:100:]20:push[fill,:100:]", "[c]"));
	    actionPanel.add(new JButton(model.getEditAction()));
	    actionPanel.add(new JButton(model.getSaveAction()));
	    actionPanel.add(new JButton(model.getCancelAction()));
	    actionPanel.add(new JButton(model.getDeleteAction()));
	    actionPanel.add(new JButton(model.getRefreshAction()));
	    return actionPanel;
	}

    },

    ALL_ACTIONS {

	@Override
	public JPanel toolbar(final UModel model) {
	    final JPanel actionPanel = new JPanel(new MigLayout("fill, insets 0", "[fill,:100:][fill,:100:]20[fill,:100:][fill,:100:]20[fill,:100:]20:push[fill,:100:]", "[c]"));
	    actionPanel.add(new JButton(model.getNewAction()));
	    actionPanel.add(new JButton(model.getEditAction()));
	    actionPanel.add(new JButton(model.getSaveAction()));
	    actionPanel.add(new JButton(model.getCancelAction()));
	    actionPanel.add(new JButton(model.getDeleteAction()));
	    actionPanel.add(new JButton(model.getRefreshAction()));
	    return actionPanel;
	}

    };

    public abstract JPanel toolbar(final UModel model);
}

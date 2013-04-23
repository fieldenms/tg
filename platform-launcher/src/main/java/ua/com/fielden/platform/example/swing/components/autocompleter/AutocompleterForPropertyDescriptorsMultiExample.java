package ua.com.fielden.platform.example.swing.components.autocompleter;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.components.smart.autocompleter.renderer.development.MultiplePropertiesListCellRenderer;
import ua.com.fielden.platform.swing.components.textfield.caption.CaptionTextFieldLayer;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.utils.Pair;

/**
 * <code>AutocompleterMultiExample</> demonstrates how to use {@link CaptionTextFieldLayer} for selection of multiple values.
 *
 * @author 01es
 *
 */
public class AutocompleterForPropertyDescriptorsMultiExample {
    public static void main(final String[] args) throws Exception {
	for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
	    if ("Nimbus".equals(laf.getName())) {
		UIManager.setLookAndFeel(laf.getClassName());
	    }
	}
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		// Property <code>values</code> holds a complete list of values used for autocompletion. Alternative implementations may retrieve data from a database or other sources.
		final List<PropertyDescriptor<DemoEntity>> acceptableValues = Finder.getPropertyDescriptors(DemoEntity.class);

		// create an instance of the overlayable text field, which bill be used for attaching overlay components
		final IValueMatcher<PropertyDescriptor<DemoEntity>> matcher = new PojoValueMatcher<PropertyDescriptor<DemoEntity>>(acceptableValues, "key", 10) {
		    @Override
		    public List<PropertyDescriptor<DemoEntity>> findMatches(final String value) {
			try {
			    Thread.sleep(1000);
			} catch (final InterruptedException e) {
			}

			return super.findMatches(value);
		    }
		};

		final MultiplePropertiesListCellRenderer<PropertyDescriptor<DemoEntity>> cellRenderer = new MultiplePropertiesListCellRenderer<PropertyDescriptor<DemoEntity>>("key", new Pair[] {new Pair<String, String>("Description", "desc")});
		final AutocompleterTextFieldLayer<PropertyDescriptor<DemoEntity>> autocompleter = new AutocompleterTextFieldLayer(new JTextField(), matcher, PropertyDescriptor.class, "key", cellRenderer, "caption...", ",");
		cellRenderer.setAuto(autocompleter.getAutocompleter());

		final JPanel panel = new JPanel(new MigLayout("fill"));
		panel.add(autocompleter, "growx, h 25!, w 150, wrap");
		panel.add(new JButton(new Command<Boolean>("Print values") {
		    @Override
		    protected Boolean action(final ActionEvent e) throws Exception {
			for (final PropertyDescriptor<DemoEntity> entity : autocompleter.values()) {
			    System.out.println(entity);
			}
			return true;
		    }
		}), "align right");

		SimpleLauncher.show("Autocompleter demo", panel);
	    }
	});
    }
}

package ua.com.fielden.platform.example.swing.components.autocompleter;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.smart.autocompleter.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.swing.components.smart.autocompleter.renderer.TwoPropertyListCellRenderer;
import ua.com.fielden.platform.swing.components.textfield.UpperCaseTextField;
import ua.com.fielden.platform.swing.components.textfield.caption.CaptionTextFieldLayer;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;

/**
 * <code>AutocompleterMultiExample</> demonstrates how to use {@link CaptionTextFieldLayer} for selection of multiple values.
 *
 * @author 01es
 *
 */
public class AutocompleterMultiExample {
    public static void main(final String[] args) throws Exception {
	for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
	    if ("Nimbus".equals(laf.getName())) {
		UIManager.setLookAndFeel(laf.getClassName());
	    }
	}
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		// Property <code>values</code> holds a complete list of values used for autocompletion. Alternative implementations may retrieve data from a database or other sources.
		final DemoEntity[] acceptableValues = new DemoEntity[] { new DemoEntity("NAME 1", "demo for name 1 demo for name 1 demo for name 1"),
			new DemoEntity("NAME 2", "demo for name 2"), new DemoEntity("NAME 3", "demo for name 3"), new DemoEntity("NMAE", "demo for name 2"),
			new DemoEntity("DONE 1", "demo for name 3"), new DemoEntity("D2NE 2", "demo for name 3"), new DemoEntity("DONE 3", "demo for name 3") };

		// create an instance of the overlayable text field, which bill be used for attaching overlay components
		final IValueMatcher<DemoEntity> matcher = new PojoValueMatcher<DemoEntity>(Arrays.asList(acceptableValues), "name", 10) {
		    @Override
		    public List<DemoEntity> findMatches(final String value) {
			try {
			    Thread.sleep(1000);
			} catch (final InterruptedException e) {
			}

			return super.findMatches(value);
		    }
		};

		final TwoPropertyListCellRenderer<DemoEntity> cellRenderer = new TwoPropertyListCellRenderer<DemoEntity>("name", "desc");
		final AutocompleterTextFieldLayer<DemoEntity> autocompleter = new AutocompleterTextFieldLayer<DemoEntity>(new UpperCaseTextField(), matcher, DemoEntity.class, "name", cellRenderer, "caption...", ";");
		cellRenderer.setAuto(autocompleter.getAutocompleter());

		final JPanel panel = new JPanel(new MigLayout("fill"));
		panel.add(autocompleter, "growx, h 25!, w 150, wrap");
		panel.add(new JButton(new Command<Boolean>("Print values") {
		    @Override
		    protected Boolean action(final ActionEvent e) throws Exception {
			for (final DemoEntity entity : autocompleter.values()) {
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

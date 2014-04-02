package ua.com.fielden.platform.swing.components.bind.test;

import javax.swing.JLabel;
import javax.swing.JToolTip;
import javax.swing.LookAndFeel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.jdesktop.swingx.plaf.DatePickerAddon;
import org.jdesktop.swingx.plaf.DefaultsList;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;

import ua.com.fielden.platform.swing.components.CustomTooltip;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;

public class MultiLineToolTipTest {

    static {
        // this patch need for correct usage JXDatePicker in "Nimbus" L&F. When support for "Nimbus" will be provided by default -> it should be removed
        LookAndFeelAddons.contribute(new DatePickerAddon() {
            @Override
            protected void addNimbusDefaults(final LookAndFeelAddons addon, final DefaultsList defaults) {
                super.addNimbusDefaults(addon, defaults);
                //TODO: don't use an image here, Nimbus uses Painters for everything
                // => e.g. reuse the com.sun.java.swing.plaf.nimbus.ComboBoxComboBoxArrowButtonPainter
                // (at the moment the OS-X icon looks most similar, it's much better
                // than no icon...)
                defaults.add("JXDatePicker.arrowIcon", LookAndFeel.makeIcon(DatePickerAddon.class, "macosx/resources/combo-osx.png"));
                // no borders, this is done by Nimbus
                defaults.add("JXDatePicker.border", null);
            }
        });
    }

    public static void main(final String[] args) throws Exception {
        for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(laf.getName())) {
                try {
                    UIManager.setLookAndFeel(laf.getClassName());
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }

        ToolTipManager.sharedInstance().setDismissDelay(1000 * 300);
        final JLabel label = new JLabel("huifhueifef.") {
            @Override
            public JToolTip createToolTip() {
                final JLabel tipComponent = new JLabel();
                final CustomTooltip customTooltip = new CustomTooltip(this, tipComponent, true) {
                    private static final long serialVersionUID = -951889975845513426L;

                    @Override
                    public void updateTipText(final String tipText) {
                        tipComponent.setText(tipText);
                    }
                };
                return customTooltip;
            }
        };
        label.setToolTipText("<html>TOOOOL - TIP - T" /*+ "OOOOL - TIP - <b>TOOOOL - TIP - TOOOOL</b> - TIP - TOOOOL - TIP - TOOOOL - TIP - TOOOOL - TIP</html>"*/);
        SimpleLauncher.show("Test", label);
    }
}
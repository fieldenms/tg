/**
 *
 */
package ua.com.fielden.platform.example.swing.treemenu.withtabs;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.example.swing.treemenu.DemoUiModel;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.menu.SimpleInfoPanel;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.swing.menu.TreeMenuPanel;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.menu.filter.WordFilter;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.view.BaseNotifPanel;
import ua.com.fielden.platform.swing.view.MasterPanel;

/**
 * Panel used for demonstration purposes
 * 
 * @author Yura
 * @author 01es
 */
public class TreeMenuWithTabsDemoPanel extends MasterPanel {

    private static final long serialVersionUID = 6487516858880182913L;

    @SuppressWarnings("unchecked")
    public TreeMenuWithTabsDemoPanel(final BlockingIndefiniteProgressPane blockingPane) {
        final TreeMenuItem menu = new TreeMenuItem("root", "root panel");

        final BaseNotifPanel masterPanel = new BaseNotifPanel("Master Panel", new DemoUiModel(true)) {
            private static final long serialVersionUID = 1L;

            @Override
            public String getInfo() {
                return "<html>" + "<h3>Master Panel</h3>" + "Used for Work Order editing." + "</html>";
            }

            @Override
            public ICloseGuard canClose() {
                return null;
            }

            @Override
            public String whyCannotClose() {
                return "can reason";
            }

        };

        final BaseNotifPanel costPanel = new BaseNotifPanel("Cost Panel", new DemoUiModel(true)) {
            private static final long serialVersionUID = 1L;
            private ICloseGuard canClose = null;
            private ICloseGuard thisGuard = this;

            @Override
            public String getInfo() {
                return "<html>" + "<h3>Cost</h3>" + "A facility for reviewing work order cost." + "</html>";
            }

            @Override
            public ICloseGuard canClose() {
                return canClose;
            }

            @Override
            public String whyCannotClose() {
                return "Costs are not saved";
            }

            @Override
            protected void layoutComponents() {
                super.layoutComponents();
                final JPanel bodyPanel = new JPanel(new MigLayout("fill, insets 0", "[fill, grow]", "[c,grow,fill][]"));
                bodyPanel.add(new JPanel(), "wrap");
                final JPanel actionPanel = new JPanel(new MigLayout("fill, insets 0", "[c, fill, grow][c, fill, grow]", "[]"));
                actionPanel.add(new JButton(new BlockingLayerCommand<Void>("Can Close", getBlockingLayer()) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected boolean preAction() {
                        super.preAction();
                        setBlockingMessage("Making can close... one sec delay");
                        return true;
                    }

                    @Override
                    protected Void action(final ActionEvent e) throws Exception {
                        Thread.sleep(1000);
                        canClose = null;
                        return null;
                    }

                    @Override
                    protected void postAction(final Void value) {
                        setBlockingMessage("Completed");
                        super.postAction(value);
                    }

                }), "w :100:200");

                actionPanel.add(new JButton(new Command<Void>("Cannot Close") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected Void action(final ActionEvent e) throws Exception {
                        canClose = thisGuard;
                        return null;
                    }
                }), "w :100:200");

                bodyPanel.add(actionPanel);
                add(bodyPanel);
            }

        };

        final String costPanelInfo = "<html>" + "<h3>Cost Info</h3>"
                + "Cost menu item demonstrates a case where a menu item cannot be closed without first finalising some action." + "<br/><br/> "
                + "By default the menu's view is closable. Button <i>Cannot Close</i> should be pressed to emulate some unclosable state. "
                + "Once pressed no other menu item can be selected or tab closed until the state is changed to closable. "
                + "This can be achieved by pressing button <i>Can Close</i>." + "</html>";
        final TreeMenuItem masterMenu = new TreeMenuItem(masterPanel).//
        addItem(new TreeMenuItem(costPanel, costPanel.toString(), new SimpleInfoPanel(costPanelInfo), false));

        final String indChargesPanelInfo = "<html>" + "<h3>Indirect Charges Info</h3>"
                + "Unlike other menu items, this menu item does not require a long initialisation (e.g. no db requests) and thus its activation does not involve UI blocking."
                + "</html>";

        final TreeMenuItem indChargeMenu = new TreeMenuItem(new BaseNotifPanel("Indirect Charges Panel", new DemoUiModel(false)) {
            private static final long serialVersionUID = 1L;

            @Override
            public String getInfo() {
                return "<html>" + "<h3>Indirect Charges</h3>" + "Something to do with wokr order cost entry..." + "</html>";
            }

            @Override
            public ICloseGuard canClose() {
                return null;
            }

            @Override
            public String whyCannotClose() {
                return "can change";
            }

        },//
        "Indirect Charges Panel",//
        new SimpleInfoPanel(indChargesPanelInfo), false).addItem(new TreeMenuItem(new BaseNotifPanel("Ind sub Panel", new DemoUiModel(true)) {
            private static final long serialVersionUID = 1L;

            @Override
            public String getInfo() {
                return "<html>" + "<h3>Indirect Charges Subpanel</h3>" + "Something to do with wokr order cost entry..." + "</html>";
            }

            @Override
            public ICloseGuard canClose() {
                return null;
            }

            @Override
            public String whyCannotClose() {
                return "can change";
            }
        }));

        menu.add(masterMenu);
        menu.add(indChargeMenu);

        setOneTouchExpandable(true);

        final String defaultInfo = "<html>" + "<h2>Information</h2>"
                + "This application demonstrates features of the tree menu controls with tab support where each menu item has a designated tab." + "<br/><br/> "
                + "Simple selection of a menu item does not open its content (the view), but instead displays its information panel. "
                + "Some menu items may not be provided with an information panel. " + "In this case a general window information panel is displayed." + "<br/><br/>"
                + "Double clicking a menu item or selecting it and pressing the enter key loads item's content (also known as item's view) on a separate tab. "
                + "This way navigation between menu items can be done using tabs or the tree menu."
                + "Please note that opening a menu item automatically bring the input focus to its view."
                + "Once a menu is open selecting it in the tree automatically activates a corresponding tab. Otherwise, its information panel is displayed." + "<br/><br/>"
                + "<h3>Hot Keys</h3>" + "The following hot keys are supported:"//
                + "<ul>"//
                + "  <li>CTRL+1 -- focuses the tree menu."//
                + "  <li>CTRL+2 -- focuses the tree filter."//
                + "  <li>CTRL+3 -- focuses the tabbed pane."//
                + "  <li>CRTL+I -- activates info tab."//
                + "  <li>CRTL+W -- closes the current tab."//
                + "  <li>CTRL+PAGE_DOWN -- moves to the next tab (circular action)."//
                + "  <li>CTRL+PAGE_UP -- moves to the previous tab (circular action)."//
                + "  <li>ENTER -- when applied to a closed menu item loads a corresponding view into a sepeare tab, or simply focuses a corresponding view in an open tab."//
                + "</ul>"//
                + "</html>";
        addTreeMenuPanel(new TreeMenuPanel(new TreeMenuWithTabs(menu, new WordFilter(), new SimpleInfoPanel(defaultInfo), blockingPane)));
    }

    @Override
    public String getInfo() {
        return "Tree Menu With Tabs Demo Panel";
    }
}

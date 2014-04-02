/**
 *
 */
package ua.com.fielden.platform.example.swing.treemenu;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.menu.TreeMenu;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.swing.menu.TreeMenuPanel;
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
public class TreeMenuDemoPanel extends MasterPanel {

    private static final long serialVersionUID = 6487516858880182913L;

    @SuppressWarnings("unchecked")
    public TreeMenuDemoPanel(final BlockingIndefiniteProgressPane blockingPane) {
        final TreeMenuItem menu = new TreeMenuItem("root", "root panel");

        final BaseNotifPanel masterPanel = new BaseNotifPanel("Master Panel", new DemoUiModel(true)) {
            private static final long serialVersionUID = 1L;

            @Override
            public ICloseGuard canClose() {
                return null;
            }

            @Override
            public String whyCannotClose() {
                return "can reason";
            }

            @Override
            public String getInfo() {
                return "Master Panel";
            }
        };

        final BaseNotifPanel costPanel = new BaseNotifPanel("Cost Panel", new DemoUiModel(true)) {
            private static final long serialVersionUID = 1L;
            private ICloseGuard canClose;
            private ICloseGuard thisGuard = this;

            @Override
            public String getInfo() {
                return "Cost Panel";
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

        final TreeMenuItem masterMenu = new TreeMenuItem(masterPanel).//
        addItem(new TreeMenuItem(costPanel));

        final TreeMenuItem indChargeMenu = new TreeMenuItem(new BaseNotifPanel("Indirect Charges Panel", new DemoUiModel(false)) {
            private static final long serialVersionUID = 1L;

            @Override
            public String getInfo() {
                return "Indirect Charges Panel";
            }

            @Override
            public ICloseGuard canClose() {
                return null;
            }

            @Override
            public String whyCannotClose() {
                return "can change";
            }

        }).addItem(new TreeMenuItem(new BaseNotifPanel("Ind sub Panel", new DemoUiModel(true)) {
            private static final long serialVersionUID = 1L;

            @Override
            public String getInfo() {
                return "Ind sub Panel";
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
        addTreeMenuPanel(new TreeMenuPanel(new TreeMenu(menu, new WordFilter(), TreeMenuPanel.createContentHolder(), blockingPane)));
    }

    @Override
    public String getInfo() {
        return "Tree menu demo panel";
    }
}

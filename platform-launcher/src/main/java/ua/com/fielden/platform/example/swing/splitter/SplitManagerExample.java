package ua.com.fielden.platform.example.swing.splitter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.swing.splitter.SplitEnum;
import ua.com.fielden.platform.swing.splitter.SplitManager;

/**
 * application implemented only for SplitManager testing purpose
 * 
 * @author oleh
 * 
 */
public class SplitManagerExample extends AbstractUiApplication {

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Exception {
        final JFrame mainFrame = new JFrame("Splitter Manager Demo");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setPreferredSize(new Dimension(640, 480));
        final JPanel panel = new JPanel(new BorderLayout());
        mainFrame.add(panel, BorderLayout.CENTER);
        final JButton eastButton = new JButton();
        final JButton westButton = new JButton();
        final JButton northButton = new JButton();
        final JButton southButton = new JButton();
        final JButton centerButton = new JButton();
        final SplitManager splitter = new SplitManager(panel);
        splitter.reset();
        @SuppressWarnings("serial")
        final Action listener = new AbstractAction() {
            private int clicked;
            {
                clicked = 1;
            }

            @Override
            public void actionPerformed(final ActionEvent e) {
                splitter.reset();
                if (clicked == 0) {
                    splitter.split(SplitEnum.EAST, "EAST").split(SplitEnum.SOUTH, "SOUTH")//
                    .split(SplitEnum.WEST, "WEST").split(SplitEnum.NORTH, "NORTH");
                    splitter.setOneTouchExpandable(true);
                    splitter.setComponent("EAST", eastButton, JSplitPane.RIGHT);
                    splitter.setComponent("SOUTH", southButton, JSplitPane.BOTTOM);
                    splitter.setComponent("WEST", westButton, JSplitPane.LEFT);
                    splitter.setComponent("NORTH", northButton, JSplitPane.TOP);
                    splitter.setComponent("NORTH", centerButton, JSplitPane.BOTTOM);
                    clicked = 1;
                } else {
                    splitter.split(SplitEnum.EAST, "EAST").split(SplitEnum.WEST, "WEST")//
                    .split(SplitEnum.SOUTH, "SOUTH").split(SplitEnum.NORTH, "NORTH");
                    splitter.setOneTouchExpandable(true);
                    splitter.setComponent("EAST", eastButton, JSplitPane.RIGHT);
                    splitter.setComponent("SOUTH", southButton, JSplitPane.BOTTOM);
                    splitter.setComponent("WEST", westButton, JSplitPane.LEFT);
                    splitter.setComponent("NORTH", northButton, JSplitPane.TOP);
                    splitter.setComponent("NORTH", centerButton, JSplitPane.BOTTOM);
                    clicked = 0;
                }
                splitter.flush(BorderLayout.CENTER);
            }

        };
        eastButton.setAction(listener);
        westButton.setAction(listener);
        northButton.setAction(listener);
        southButton.setAction(listener);
        centerButton.setAction(listener);
        eastButton.setText("EAST");
        westButton.setText("WEST");
        centerButton.setText("CENTER");
        northButton.setText("NORTH");
        southButton.setText("SOUTH");
        splitter.split(SplitEnum.EAST, "EAST").split(SplitEnum.SOUTH, "SOUTH")//
        .split(SplitEnum.WEST, "WEST").split(SplitEnum.NORTH, "NORTH");
        splitter.setOneTouchExpandable(true);
        splitter.setComponent("EAST", eastButton, JSplitPane.RIGHT);
        splitter.setComponent("SOUTH", southButton, JSplitPane.BOTTOM);
        splitter.setComponent("WEST", westButton, JSplitPane.LEFT);
        splitter.setComponent("NORTH", northButton, JSplitPane.TOP);
        splitter.setComponent("NORTH", centerButton, JSplitPane.BOTTOM);
        splitter.flush(BorderLayout.CENTER);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    public static void main(final String[] args) {
        new SplitManagerExample().launch(args);
    }

}

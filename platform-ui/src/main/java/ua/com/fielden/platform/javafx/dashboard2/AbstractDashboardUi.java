package ua.com.fielden.platform.javafx.dashboard2;

import java.util.List;

import javafx.event.EventHandler;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.menu.MenuNotificationPanel;
import ua.com.fielden.platform.swing.menu.StubUiModel;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.swing.view.BaseNotifPanel;

import com.jidesoft.swing.StyledLabelBuilder;

/**
 * UI class for dashboard.
 *
 * @author TG Team
 *
 */
public abstract class AbstractDashboardUi<DASHBOARD_MODEL extends AbstractDashboard<? extends AbstractDashboardUi>> extends BaseNotifPanel<StubUiModel> {
    private static final long serialVersionUID = 7623224149991897675L;
    private final JComponent paramsGetterUi;
    private final List<IDashboardItemUi> dashboardItemUis;
    private final DASHBOARD_MODEL dashboardModel;

    public AbstractDashboardUi(final JComponent paramsGetterUi, final List<IDashboardItemUi> dashboardItemUis, final DASHBOARD_MODEL dashboardModel) {
	super(new MenuNotificationPanel("Dashboard"){
	    @Override
	    protected JPanel createLayerComponent(final String caption) {
		final JPanel panel = new JPanel(new MigLayout("fill, insets 0 5 0 5", "[left][right]", "[grow,fill,c,30:30:30]"));
		panel.setBorder(new EmptyBorder(0, 0, 0, 0));
		final JLabel headerLable = new StyledLabelBuilder().add(caption, "bold,f:darkgray").createLabel();
		panel.add(headerLable);
		panel.add(new ButtonsUI(new Runnable() {
		    public void run() {
			dashboardModel.refreshAll();
		    }
		}));
	        return panel;
	    }
	}, new StubUiModel(true));

	this.dashboardModel = dashboardModel;
	this.paramsGetterUi = paramsGetterUi;
	this.dashboardItemUis = dashboardItemUis;
    }

    @Override
    public abstract String getInfo();

    @Override
    protected abstract void layoutComponents();

    protected JComponent getParamsGetterUi() {
	return paramsGetterUi;
    }

    protected List<IDashboardItemUi> getDashboardItemUis() {
	return dashboardItemUis;
    }

    public DASHBOARD_MODEL getDashboardModel() {
	return dashboardModel;
    }

    public static Shape createSettingsShape(final Runnable action) {
        final Circle circle = new Circle(20);
        Shape shape = circle;
        for (int i = 0; i < 7; i++) {
            final Rectangle r = new Rectangle(0, 0, 56, 6);
            r.setRotate(i * 45);
            r.setTranslateX(-56.0 / 2);
            r.setTranslateY(-6.0 / 2);
            shape = Shape.union(shape, r);
        }
        shape = Shape.subtract(shape, new Circle(10));

        shape.setFill(Color.WHITE);
        shape.setScaleX(.3);
        shape.setScaleY(.3);

        final Shape lastShape = shape;

        lastShape.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
        	lastShape.setEffect(new InnerShadow(BlurType.GAUSSIAN, Color.RED, 15, 0, 0, 0));
            }
        });
        lastShape.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
        	lastShape.setEffect(null);

        	SwingUtilitiesEx.invokeLater(action);
        	// refreshAction.run();
            }
        });
        return lastShape;
    }

    public static Shape createRefreshShape(final Runnable action) {
        final Circle circle = new Circle(20);
        Shape shape = circle;
        shape = Shape.subtract(shape, new Circle(10));

        final Arc a = new Arc(0, 0, 20, 20, 90, 45);
        a.setType(ArcType.ROUND);
        shape = Shape.subtract(shape, a);

        final Polygon p = new Polygon(0, 0, 0, 10, -10, 0, 0, -10, 0, 0);
        p.setTranslateY(-15.0);
        shape = Shape.union(shape, p);

        final Arc a2 = new Arc(0, 0, 20, 20, 270, 45);
        a2.setType(ArcType.ROUND);
        shape = Shape.subtract(shape, a2);

        final Polygon p2 = new Polygon(0, 0, 0, 10, -10, 0, 0, -10, 0, 0);
        p2.setTranslateY(+15.0);
        p2.setRotate(180.0);
        p2.setTranslateX(+10.0);
        shape = Shape.union(shape, p2);

        shape.setFill(Color.WHITE);
        shape.setScaleX(.3);
        shape.setScaleY(.3);

        final Shape lastShape = shape;

        lastShape.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
        	lastShape.setEffect(new InnerShadow(BlurType.GAUSSIAN, Color.RED, 15, 0, 0, 0));
            }
        });
        lastShape.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
        	lastShape.setEffect(null);

        	SwingUtilitiesEx.invokeLater(action);
        	// refreshAction.run();
            }
        });
        return lastShape;
    }
}
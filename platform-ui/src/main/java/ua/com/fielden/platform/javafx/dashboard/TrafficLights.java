package ua.com.fielden.platform.javafx.dashboard;

import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import ua.com.fielden.platform.selectioncheckbox.SelectionCheckBoxPanel.IAction;

public class TrafficLights extends Group {
    private final TrafficLightsModel model;
    private final TrafficLight redLight, goldLight, greenLight;

    public TrafficLights(final TrafficLightsModel model, final IAction redAction, final IAction goldAction, final IAction greenAction) {
	this.model = model;

	final int radius = 13;
	final int gap = 3;

	redLight = new TrafficLight(model.getRedLightingModel(), radius, Color.RED, redAction);
	redLight.getModel().addAfterChangeAction(new IAction() {
	    @Override
	    public void action() {
		redLight.updateColor();
	    }
	});
	redLight.setTranslateX(2 * gap + radius);
	redLight.setTranslateY(gap + radius);
	goldLight = new TrafficLight(model.getYellowLightingModel(), radius, Color.GOLD, goldAction);
	goldLight.getModel().addAfterChangeAction(new IAction() {
	    @Override
	    public void action() {
		goldLight.updateColor();
	    }
	});
	goldLight.setTranslateX(2 * gap + radius + gap * 2 + radius * 2);
	goldLight.setTranslateY(gap + radius);
	greenLight = new TrafficLight(model.getGreenLightingModel(), radius, Color.GREEN, greenAction);
	greenLight.getModel().addAfterChangeAction(new IAction() {
	    @Override
	    public void action() {
		greenLight.updateColor();
	    }
	});
	greenLight.setTranslateX(2 * gap + radius + gap * 4 + radius * 4);
	greenLight.setTranslateY(gap + radius);

	getChildren().add(redLight);
	getChildren().add(goldLight);
	getChildren().add(greenLight);
    }

    public TrafficLightsModel getModel() {
	return model;
    }

    public static class TrafficLightModel {
	private boolean isLighting = false;
	private final List<IAction> afterChangeActions = new ArrayList<>();

	public boolean isLighting() {
	    return isLighting;
	}

	public void addAfterChangeAction(final IAction action) {
	    afterChangeActions.add(action);
	}

	public void setLighting(final boolean isLighting) {
	    this.isLighting = isLighting;

	    for (final IAction afterChange : afterChangeActions) {
		afterChange.action();
	    }
	}
    }

    private static class TrafficLight extends Circle {
	private final TrafficLightModel model;
	private final Color lightingColor;
	private final IAction action;

	public TrafficLight(final TrafficLightModel model, final double radius, final Color lightingColor, final IAction action) {
	    super(0, 0, radius);

	    this.model = model;

	    this.lightingColor = lightingColor;
	    this.action = action;

	    updateColor();

	    setOnMousePressed(new EventHandler<MouseEvent>() {
		@Override
		public void handle(final MouseEvent event) {
		    if (isLighting()) {
			setRadius(getRadius() - 1);
		    }
		}
	    });
	    setOnMouseReleased(new EventHandler<MouseEvent>() {
		@Override
		public void handle(final MouseEvent event) {
		    if (isLighting()) {
			setRadius(getRadius() + 1);
			TrafficLight.this.action.action();
		    }
		}
	    });

	    setOnMouseEntered(new EventHandler<MouseEvent>() {
		@Override
		public void handle(final MouseEvent event) {
		    if (isLighting()) {
			setEffect(new InnerShadow(BlurType.GAUSSIAN, realColor(), 15, 0, 0, 0));
		    }
		}
	    });
	    setOnMouseExited(new EventHandler<MouseEvent>() {
		@Override
		public void handle(final MouseEvent event) {
		    if (isLighting()) {
			setEffect(null);
		    }
		}
	    });
	}

	public void updateColor() {
	    setFill(new RadialGradient(225, 0.5, 0, 0, getRadius(), false, CycleMethod.NO_CYCLE, new Stop(0.0, Color.WHITE), new Stop(1.0, realColor())));
	}

	private Color realColor() {
	    return isLighting() ? lightingColor : Color.GREY;
	}

	private boolean isLighting() {
	    return model.isLighting();
	}

	public TrafficLightModel getModel() {
	    return model;
	}
    }
}

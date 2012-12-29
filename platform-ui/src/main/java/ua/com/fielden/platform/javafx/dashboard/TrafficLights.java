package ua.com.fielden.platform.javafx.dashboard;

import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;

/**
 * A traffic lights control.
 *
 * @author TG Team
 *
 */
public class TrafficLights extends Group {
    public static final Color GREEN_COLOUR = Color.GREEN;
    public static final Color GOLD_COLOUR = Color.GOLD;
    public static final Color RED_COLOUR = Color.RED;
    private final TrafficLightsModel model;
    private final TrafficLight redLight, goldLight, greenLight;

    public TrafficLights(final TrafficLightsModel model, final Runnable redAction, final Runnable goldAction, final Runnable greenAction) {
	this.model = model;

	final int radius = 13;
	final int gap = 3;

	redLight = new TrafficLight(model.getRedLightingModel(), radius, RED_COLOUR, redAction);
	redLight.getModel().addAfterChangeAction(new Runnable() {
	    @Override
	    public void run() {
		redLight.updateColor();
	    }
	});
	redLight.setTranslateX(2 * gap + radius);
	redLight.setTranslateY(gap + radius);
	goldLight = new TrafficLight(model.getYellowLightingModel(), radius, GOLD_COLOUR, goldAction);
	goldLight.getModel().addAfterChangeAction(new Runnable() {
	    @Override
	    public void run() {
		goldLight.updateColor();
	    }
	});
	goldLight.setTranslateX(2 * gap + radius + gap * 2 + radius * 2);
	goldLight.setTranslateY(gap + radius);
	greenLight = new TrafficLight(model.getGreenLightingModel(), radius, GREEN_COLOUR, greenAction);
	greenLight.getModel().addAfterChangeAction(new Runnable() {
	    @Override
	    public void run() {
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
	// private boolean isLighting = false;
	private Integer count = 0;
	private final List<Runnable> afterChangeActions = new ArrayList<>();

	public boolean isLighting() {
	    return count > 0;
	}

	public void addAfterChangeAction(final Runnable action) {
	    afterChangeActions.add(action);
	}

	public TrafficLightModel setCount(final Integer count) {
	    this.count = count;

	    for (final Runnable afterChange : afterChangeActions) {
		afterChange.run();
	    }
	    return this;
	}

	public Integer getCount() {
	    return count;
	}
    }

    public static class TrafficLight extends Circle {
	private final TrafficLightModel model;
	private final Color lightingColor;
	private final Runnable action;
	private final Tooltip tooltip;
	private boolean isBlinking = false;

	public void startBlinking() {
	    if (!isBlinking) {
		isBlinking = true;
		setRadius(getRadius() + 2);
		updateColor();
	    }
	}

	public void stopBlinking() {
	    if (isBlinking) {
		isBlinking = false;
		setRadius(getRadius() - 2);
		updateColor();
	    }
	}

	protected Tooltip tooltip() {
	    return tooltip;
	}

	public TrafficLight(final TrafficLightModel model, final double radius, final Color lightingColor, final Runnable action) {
	    super(0, 0, radius);

	    tooltip = new Tooltip();

	    this.model = model;

	    this.lightingColor = lightingColor;
	    this.action = action;

	    updateColor();

	    setOnMousePressed(new EventHandler<MouseEvent>() {
		@Override
		public void handle(final MouseEvent event) {
		    if (isLighting()) {
			setEffect(new InnerShadow(BlurType.GAUSSIAN, realColor(), 15, 0, 0, 0));
		    }
		}
	    });
	    setOnMouseReleased(new EventHandler<MouseEvent>() {
		@Override
		public void handle(final MouseEvent event) {
		    if (isLighting()) {
			setEffect(null);
			TrafficLight.this.action.run();
		    }
		}
	    });

	    setStrokeWidth(0.0);
	    setStroke(lightingColor.darker());
	    setOnMouseEntered(new EventHandler<MouseEvent>() {
		@Override
		public void handle(final MouseEvent event) {
		    if (isLighting()) {
			setRadius(getRadius() - 0.5);
			setStrokeWidth(1.0);
		    }
		}
	    });
	    setOnMouseExited(new EventHandler<MouseEvent>() {
		@Override
		public void handle(final MouseEvent event) {
		    if (isLighting()) {
			setRadius(getRadius() + 0.5);
			setStrokeWidth(0.0);
		    }
		}
	    });
	}

	public void updateColor() {
	    setFill(new RadialGradient(225, 0.5, 0, 0, getRadius(), false, CycleMethod.NO_CYCLE, new Stop(0.0, Color.WHITE), new Stop(1.0, realColor())));

	    updateTooltip();
	}

	protected void updateTooltip() {
	    if (model.getCount() > 0) {
		tooltip.setText(model.getCount() + " items");
		Tooltip.install(this, tooltip);
	    } else {
		tooltip.setText("");
		Tooltip.uninstall(this, tooltip);
	    }
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

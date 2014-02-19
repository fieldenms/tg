package ua.com.fielden.platform.javafx.dashboard2;

import java.math.RoundingMode;
import java.text.NumberFormat;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import ua.com.fielden.platform.types.Money;

public class SentinelSectionView extends Group {
    private final SentinelSectionModel model;
    private final Color lightingColor;
    private final Runnable action;
    private Tooltip tooltip;
    private boolean isBlinking = false;
    // private final Arc arc;
    private final Rectangle rect;
    private final Path path;
    private final Text text1;
    private final Text text2, text2a;
    private final Text text3;
    // private final Font font = Font.font("Monospaced", FontWeight.EXTRA_BOLD, FontPosture.REGULAR, 14.0);  // Font.font(font.getFamily(), font.get, arg2, arg3);
    private final double gap = 25.0;
    private final double width, height;

    public void startBlinking() {
	if (!isBlinking) {
	    isBlinking = true;
	    //		arc.setRadiusX(arc.getRadiusX() + 2);
	    //		arc.setRadiusY(arc.getRadiusY() + 2);
	    rect.setWidth(rect.getWidth() + 2);
	    rect.setHeight(rect.getHeight() + 2);
	    updateColor();
	}
    }

    public void stopBlinking() {
	if (isBlinking) {
	    isBlinking = false;
	    //		arc.setRadiusX(arc.getRadiusX() - 2);
	    //		arc.setRadiusY(arc.getRadiusY() - 2);
	    rect.setWidth(rect.getWidth() - 2);
	    rect.setHeight(rect.getHeight() - 2);
	    updateColor();
	}
    }

    protected Tooltip tooltip() {
	return tooltip;
    }

    protected void setTooltip(final Tooltip tooltip) {
	this.tooltip = tooltip;
    }

    public SentinelSectionView(final SentinelSectionModel model, /*final double radius, */final double width, final double height, final Color lightingColor, final Runnable action, final boolean enableMoney, final boolean enableDecimal) {
	this.width = width;
	this.height = height;
	// arc = new Arc(0, 0, radius, radius, 0, 360);
	rect = new Rectangle(0, 0, width, height);
	// rect.setArcHeight(10);
	// rect.setArcWidth(10);

	this.model = model;

	this.getChildren().add(rect);

	text1 = new Text();
	text1.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, FontPosture.REGULAR, 17.0));
	this.getChildren().add(text1);

	text2 = new Text();
	text2.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, FontPosture.REGULAR, 21.0));
	if (enableMoney) {
	    this.getChildren().add(text2);
	}
	text2a = new Text();
	text2a.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, FontPosture.REGULAR, 21.0));
	if (enableDecimal) {
	    this.getChildren().add(text2a);
	}

	text3 = new Text();
	text3.setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, FontPosture.REGULAR, 15.0));
	this.getChildren().add(text3);

	this.lightingColor = lightingColor;
	// final Color strokeColor = lightingColor; // Color.BLACK; // lightingColor;// .brighter();
	this.action = action;

	setOnMousePressed(new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(final MouseEvent event) {
		if (isLighting()) {
		    rect.setEffect(new InnerShadow(BlurType.GAUSSIAN, realColor(), 15, 0, 0, 0));
		}
	    }
	});
	setOnMouseReleased(new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(final MouseEvent event) {
		if (isLighting()) {
		    rect.setEffect(null);
		    SentinelSectionView.this.action.run();
		}
	    }
	});

	final double allertStrokeWidth = 2.0;
	rect.setStrokeWidth(allertStrokeWidth);
	rect.setStroke(darkerStrokeColor());

	path = new Path();
	path.setStrokeWidth(allertStrokeWidth);
	path.setStroke(darkerStrokeColor());
	final MoveTo moveTo = new MoveTo(50.0, 0.0);
	final LineTo lineTo = new LineTo(50.0, height);
	path.getElements().addAll(moveTo, lineTo);

	this.getChildren().add(path);

	updateColor();

	setOnMouseEntered(new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(final MouseEvent event) {
		if (isLighting()) {
		    rect.setWidth(rect.getWidth() - 0.5);
		    rect.setHeight(rect.getHeight() - 0.5);
		    rect.setStroke(lighterStrokeColor());
		    path.setStroke(lighterStrokeColor());
		    rect.setStrokeWidth(2.0);

		    //			arc.setRadiusX(arc.getRadiusX() - 0.5);
		    //			arc.setRadiusY(arc.getRadiusY() - 0.5);
		    //			arc.setStrokeWidth(2.0);
		}
		updateTooltip();
	    }
	});
	setOnMouseExited(new EventHandler<MouseEvent>() {
	    @Override
	    public void handle(final MouseEvent event) {
		if (isLighting()) {
		    rect.setWidth(rect.getWidth() + 0.5);
		    rect.setHeight(rect.getHeight() + 0.5);
		    rect.setStroke(darkerStrokeColor());
		    path.setStroke(darkerStrokeColor());
		    rect.setStrokeWidth(2.0);

		    //			arc.setRadiusX(arc.getRadiusX() + 0.5);
		    //			arc.setRadiusY(arc.getRadiusY() + 0.5);
		    //			arc.setStrokeWidth(0.0);
		}
	    }
	});
    }

    public void updateColor() {
	// new RadialGradient(225, 0.5, 0, 0, getRadiusX(), false, CycleMethod.NO_CYCLE, new Stop(0.0, Color.WHITE), new Stop(1.0, realColor()))
	rect.setFill(realColor().interpolate(Color.WHITE, 0.45));
	rect.setStrokeWidth(2.0);
	rect.setStroke(darkerStrokeColor());
	path.setStroke(darkerStrokeColor());

	text1.setFill(this.model.getCount().longValue() > 0 ? realColor().interpolate(Color.BLACK, 0.15) : Color.GREY.interpolate(Color.WHITE, 0.85));
	text2.setFill(this.model.getCount().longValue() > 0 ? realColor().interpolate(Color.BLACK, 0.15) : Color.GREY.interpolate(Color.WHITE, 0.85));
	text2a.setFill(this.model.getCount().longValue() > 0 ? realColor().interpolate(Color.BLACK, 0.15) : Color.GREY.interpolate(Color.WHITE, 0.85));
	text3.setFill(this.model.getCount().longValue() > 0 ? realColor().interpolate(Color.BLACK, 0.15) : Color.GREY.interpolate(Color.WHITE, 0.85));

	text1.setText(this.model.getCount().toString());
	text1.setTranslateX(gap - text1.getBoundsInLocal().getWidth() / 2.0);
	text1.setTranslateY(height / 2.0 + text1.getBoundsInLocal().getHeight() / 2.0);

	text2.setText(moneyToString(this.model.getMoney()));
	text2.setTranslateX(width - gap / 4.0 - text2.getBoundsInLocal().getWidth());
	text2.setTranslateY(height / 2.0 + text2.getBoundsInLocal().getHeight() / 2.0 - (height / 2.0) / 2.0);
	text2a.setText(this.model.getDecimal().toPlainString());
	text2a.setTranslateX(width - gap / 4.0 - text2a.getBoundsInLocal().getWidth());
	text2a.setTranslateY(height / 2.0 + text2a.getBoundsInLocal().getHeight() / 2.0 - (height / 2.0) / 2.0);

	text3.setText(this.model.getDesc());
	text3.setTranslateX(width - gap / 4.0 - text3.getBoundsInLocal().getWidth());
	text3.setTranslateY(height + text3.getBoundsInLocal().getHeight() / 2.0 - (height / 2.0) / 2.0); //  + text2.getBoundsInLocal().getHeight() / 2.0
    }

    public Color darkerStrokeColor() {
	return realColor().interpolate(Color.WHITE, 0.25);
    }

    public Color lighterStrokeColor() {
	return realColor().interpolate(Color.WHITE, 0.65);
    }

    protected void updateTooltip() {
	//	    if (tooltip == null) { // lazy initialisation to improve performance
	//		tooltip = new Tooltip();
	//	    }
	//
	//	    if (model.getCount() > 0) {
	//		tooltip.setText(model.getDesc() + "(" + model.getCount() + " items)");
	//		Tooltip.install(this, tooltip);
	//	    } else {
	//		tooltip.setText("");
	//		Tooltip.uninstall(this, tooltip);
	//	    }

	//	    if (model.getCount() > 0) {
	//		addCallout("\n" + model.getDesc() + "\n"/* + "\n" + model.getCount() + " items\n" + model.getCount() + " items\n" + model.getCount() + " items\n" + model.getCount() + " items\n" + model.getCount() + " items"*/);
	//	    } else {
	//		addCallout("empty!");
	//	    }
    }

    //	private Callout currentCallout;
    //
    //	private void addCallout(final String text) {
    //	    // add callout:
    //	    final Group infoNode = new Group();
    //
    //	    final Text textNode = new Text(text);
    //	    textNode.setFill(Color.WHITE);
    //	    textNode.setFont(font);
    //	    textNode.textOriginProperty().set(VPos.TOP);
    //
    //	    infoNode.getChildren().add(textNode);
    //
    //	    currentCallout = new Callout(rect, infoNode, /*this.getScene(), */this, new Runnable() {
    //		@Override
    //		public void run() {
    //		    if (currentCallout != null) {
    //			TrafficLight.this.getChildren().remove(currentCallout);
    //			currentCallout = null;
    //		    }
    //		}
    //	    }, Position.SOUTH, realColor().interpolate(Color.WHITE, 0.45));
    //	    this.getChildren().add(currentCallout);
    //	}

    private Color realColor() {
	return isLighting() ? lightingColor : Color.GREY;
    }

    private final boolean isLighting() {
	return model.isLighting();
    }

    public SentinelSectionModel getModel() {
	return model;
    }

    public static String moneyToString(final Money money) {
	final NumberFormat usdCostFormat = NumberFormat.getCurrencyInstance(); // NumberFormat.getCurrencyInstance(Locale.US);
	usdCostFormat.setMinimumFractionDigits(2);
	usdCostFormat.setMaximumFractionDigits(2);
	// System.out.println(usdCostFormat.format(displayVal.doubleValue()) );
	return usdCostFormat.format(money.getAmount().setScale(2, RoundingMode.HALF_UP).doubleValue());
    }

}

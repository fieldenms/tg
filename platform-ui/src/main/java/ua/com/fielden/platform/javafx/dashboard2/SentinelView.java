package ua.com.fielden.platform.javafx.dashboard2;

import javafx.scene.Group;
import javafx.scene.paint.Color;

/**
 * A traffic lights control.
 * 
 * @author TG Team
 * 
 */
public class SentinelView extends Group {
    public static final Color GREEN_COLOUR = Color.GREEN;
    public static final Color GOLD_COLOUR = Color.GOLD;
    public static final Color RED_COLOUR = Color.RED;
    private final SentinelModel model;
    private final SentinelSectionView redLight, goldLight, greenLight;

    public SentinelView(final SentinelModel model, final Runnable redAction, final Runnable goldAction, final Runnable greenAction, /* final double radius, */final double width, final double height, final double gapX, final double gapY, final boolean enableMoney, final boolean enableDecimal) {
        this.model = model;

        greenLight = new SentinelSectionView(model.getGreenLightingModel(), width, height, GREEN_COLOUR, greenAction, enableMoney, enableDecimal);
        greenLight.getModel().addAfterChangeAction(new Runnable() {
            @Override
            public void run() {
                greenLight.updateColor();
            }
        });
        greenLight.setTranslateX(gapX);
        greenLight.setTranslateY(0.0);
        goldLight = new SentinelSectionView(model.getYellowLightingModel(), width, height, GOLD_COLOUR, goldAction, enableMoney, enableDecimal);
        goldLight.getModel().addAfterChangeAction(new Runnable() {
            @Override
            public void run() {
                goldLight.updateColor();
            }
        });
        goldLight.setTranslateY(gapY * 2 + height);
        goldLight.setTranslateX(gapX);
        redLight = new SentinelSectionView(model.getRedLightingModel(), width, height, RED_COLOUR, redAction, enableMoney, enableDecimal);
        redLight.getModel().addAfterChangeAction(new Runnable() {
            @Override
            public void run() {
                redLight.updateColor();
            }
        });
        redLight.setTranslateY(gapY * 4 + height * 2);
        redLight.setTranslateX(gapX);

        getChildren().add(greenLight);
        getChildren().add(goldLight);
        getChildren().add(redLight);
    }

    public SentinelModel getModel() {
        return model;
    }
}

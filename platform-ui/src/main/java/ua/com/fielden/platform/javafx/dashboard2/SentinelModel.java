package ua.com.fielden.platform.javafx.dashboard2;

/**
 * A traffic lights control model.
 * 
 * @author TG Team
 * 
 */
public class SentinelModel {
    private final SentinelSectionModel redLightingModel;
    private final SentinelSectionModel yellowLightingModel;
    private final SentinelSectionModel greenLightingModel;

    public SentinelModel(final IDescGetter redDescGetter, final IDescGetter yellowDescGetter, final IDescGetter greenDescGetter) {
        redLightingModel = new SentinelSectionModel(redDescGetter);
        yellowLightingModel = new SentinelSectionModel(yellowDescGetter);
        greenLightingModel = new SentinelSectionModel(greenDescGetter);
    }

    public SentinelSectionModel getRedLightingModel() {
        return redLightingModel;
    }

    public SentinelSectionModel getYellowLightingModel() {
        return yellowLightingModel;
    }

    public SentinelSectionModel getGreenLightingModel() {
        return greenLightingModel;
    }
}

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

    public SentinelModel(final String redDesc, final String yellowDesc, final String greenDesc) {
	redLightingModel = new SentinelSectionModel(redDesc);
	yellowLightingModel = new SentinelSectionModel(yellowDesc);
	greenLightingModel = new SentinelSectionModel(greenDesc);
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

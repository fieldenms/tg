package ua.com.fielden.platform.javafx.dashboard;

import ua.com.fielden.platform.javafx.dashboard.TrafficLights.TrafficLightModel;

/**
 * A traffic lights control model.
 * 
 * @author TG Team
 * 
 */
public class TrafficLightsModel {
    private final TrafficLightModel redLightingModel = new TrafficLightModel();
    private final TrafficLightModel yellowLightingModel = new TrafficLightModel();
    private final TrafficLightModel greenLightingModel = new TrafficLightModel();

    public TrafficLightModel getRedLightingModel() {
        return redLightingModel;
    }

    public TrafficLightModel getYellowLightingModel() {
        return yellowLightingModel;
    }

    public TrafficLightModel getGreenLightingModel() {
        return greenLightingModel;
    }
}

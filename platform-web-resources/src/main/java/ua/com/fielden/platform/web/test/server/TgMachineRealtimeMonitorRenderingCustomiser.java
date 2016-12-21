package ua.com.fielden.platform.web.test.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;

public class TgMachineRealtimeMonitorRenderingCustomiser implements IRenderingCustomiser<Map<String, Map<String, String>>> {

    private final List<String> properties = new ArrayList<String>() {
        {
            add("");
            add("orgUnit");
            add("lastMessage.gpsTime");
            add("lastMessage.vectorSpeed");
        }
    };

    @Override
    public Optional<Map<String, Map<String, String>>> getCustomRenderingFor(final AbstractEntity<?> entity) {
        final Map<String, Map<String, String>> res = new HashMap<String, Map<String, String>>();
        final Integer vectorSpeed = (entity.get("lastMessage") == null || ((AbstractEntity) entity.get("lastMessage")).get("vectorSpeed") == null) ? null : (Integer) ((AbstractEntity) entity.get("lastMessage")).get("vectorSpeed");

        for (final String propName : properties) {
            final Map<String, String> propStyle = new HashMap<>();
            if (vectorSpeed == null) {
                propStyle.put("background-color", "white");
            } else if (vectorSpeed.equals(0)) {
                propStyle.put("background-color", "red"); // TODO adjust if needed to: CategoryChartFactory.getAwtColor(javafx.scene.paint.Color.rgb(255, 199, 206)); // javafx.scene.paint.Color.RED
            } else {
                propStyle.put("background-color", "green"); // TODO adjust if needed to: CategoryChartFactory.getAwtColor(javafx.scene.paint.Color.rgb(198, 239, 206)); // javafx.scene.paint.Color.GREEN
            }
            res.put(propName, propStyle);
        }
        return Optional.of(res);
    }
}

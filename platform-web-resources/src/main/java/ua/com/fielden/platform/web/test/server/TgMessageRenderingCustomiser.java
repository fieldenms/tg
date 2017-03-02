package ua.com.fielden.platform.web.test.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;

public class TgMessageRenderingCustomiser implements IRenderingCustomiser<Map<String, Map<String, String>>> {

    private final List<String> properties = new ArrayList<String>() {
        {
            add("machine");
            add("gpsTime");
            add("vectorSpeed");
            add("travelledDistance");
            add("din1");
        }
    };

    @Override
    public Optional<Map<String, Map<String, String>>> getCustomRenderingFor(final AbstractEntity<?> entity) {
        final Map<String, Map<String, String>> res = new HashMap<String, Map<String, String>>();
        final Integer vectorSpeed = entity.get("vectorSpeed");

        for (final String propName : properties) {
            final Map<String, String> propStyle = new HashMap<>();
            if (vectorSpeed == null) {
                propStyle.put("background-color", "white");
            } else if (vectorSpeed.equals(0)) {
                propStyle.put("background-color", "red");
            } else {
                propStyle.put("background-color", "green");
            }
            res.put(propName, propStyle);
        }
        return Optional.of(res);
    }
}

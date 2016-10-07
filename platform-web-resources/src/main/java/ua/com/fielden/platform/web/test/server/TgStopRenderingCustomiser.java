package ua.com.fielden.platform.web.test.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;

public class TgStopRenderingCustomiser implements IRenderingCustomiser<AbstractEntity<?>, Map<String, Map<String, String>>> {
    
    private final List<String> properties = new ArrayList<String>() {
        {
            add("machineResult");
            add("orgUnitResult");
            add("periodString");
            add("stopTimeFrom");
            add("nightStopResult");
            add("stopTimeTo");
            add("radius");
            add("distance");
        }
    };

    @Override
    public Optional<Map<String, Map<String, String>>> getCustomRenderingFor(final AbstractEntity<?> entity) {
        final Map<String, Map<String, String>> res = new HashMap<String, Map<String, String>>();
        final Set<TgMessage> messages = entity.get("messages");

        for (final String propName : properties) {
            final Map<String, String> propStyle = new HashMap<>();
            if (messages == null || messages.isEmpty()) {
                propStyle.put("background-color", "white");
            } else {
                propStyle.put("background-color", "rgb(255,235,156)");
            }
            res.put(propName, propStyle);
        }
        return Optional.of(res);
    }
}

package ua.com.fielden.platform.web.view.master.chart.decker.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

public class ChartSeries<T extends AbstractEntity<?>> {

    private final String propertyName;
    private String title;
    private Colour colour;
    private EntityActionConfig actionConfig;

    public ChartSeries(final String propertyName) {
        this.propertyName = propertyName;
    }
}

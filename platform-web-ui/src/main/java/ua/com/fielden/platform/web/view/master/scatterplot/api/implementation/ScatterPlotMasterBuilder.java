package ua.com.fielden.platform.web.view.master.scatterplot.api.implementation;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.chart.decker.api.*;
import ua.com.fielden.platform.web.view.master.chart.decker.api.impl.ChartDeck;
import ua.com.fielden.platform.web.view.master.chart.decker.api.impl.ChartDeckerMaster;
import ua.com.fielden.platform.web.view.master.scatterplot.api.IScatterPlotCategoryProperty;
import ua.com.fielden.platform.web.view.master.scatterplot.api.IScatterPlotMasterBuilder;
import ua.com.fielden.platform.web.view.master.scatterplot.api.IScatterPlotTitle;
import ua.com.fielden.platform.web.view.master.scatterplot.api.IScatterPlotValueProperty;

import java.util.ArrayList;
import java.util.List;

public class ScatterPlotMasterBuilder<T extends AbstractEntity<?>> implements IScatterPlotMasterBuilder<T>, IScatterPlotCategoryProperty<T>, IScatterPlotValueProperty<T> {

    private Class<T> entityType;
    private String categoryPropertyName;
    private String valuePropertyName;
    private boolean saveOnActivation = false;

    @Override
    public IScatterPlotCategoryProperty<T> forEntity(final Class<T> entityType) {
        this.entityType = entityType;
        return this;
    }

    @Override
    public IScatterPlotCategoryProperty<T> forEntityWithSaveOnActivation(final Class<T> entityType) {
        this.entityType = entityType;
        this.saveOnActivation = true;
        return this;
    }

    @Override
    public IScatterPlotValueProperty<T> setCategoryProopertyName(final String propertyName) {
        this.categoryPropertyName = propertyName;
        return this;
    }

    @Override
    public IScatterPlotTitle<T> setValuePropertyName(final String propertyName) {
        return null;
    }
}
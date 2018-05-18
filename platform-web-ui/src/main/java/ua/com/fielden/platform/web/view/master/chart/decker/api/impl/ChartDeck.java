package ua.com.fielden.platform.web.view.master.chart.decker.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerAddDeck;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerAlso;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerBarColour;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithAction;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerWithTitle;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerXAxisTitle;
import ua.com.fielden.platform.web.view.master.chart.decker.api.IChartDeckerYAxisTitle;

public class ChartDeck<T extends AbstractEntity<?>> implements IChartDeckerWithTitle<T>
{

    @Override
    public IChartDeckerYAxisTitle<T> withXAxisTitle(final String title) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IChartDeckerBarColour<T> withYAxisTitle(final String title) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IChartDeckerWithAction<T> withBarColour(final Colour barColour) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IChartDeckerAlso<T> withAction(final EntityActionConfig action) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IChartDeckerAddDeck<T> also() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IMaster<T> done() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IChartDeckerXAxisTitle<T> withTitle(final String title) {
        // TODO Auto-generated method stub
        return null;
    }

}

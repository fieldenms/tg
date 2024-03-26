package ua.com.fielden.platform.web.view.master.scatterplot.api.implementation;

import ua.com.fielden.platform.web.view.master.scatterplot.api.IScatterPlotAxisRangeProp;
import ua.com.fielden.platform.web.view.master.scatterplot.api.IScatterPlotRangeConfig;

public class ScatterPlotAxisRange implements IScatterPlotRangeConfig, IScatterPlotAxisRangeProp {

    private String source = "";

    public static IScatterPlotAxisRangeProp data() {
        return new ScatterPlotAxisRange("data");
    }

    public static IScatterPlotAxisRangeProp masterEntity() {
        return new ScatterPlotAxisRange("masterEntity");
    }

    public ScatterPlotAxisRange (String source) {
        this.source = source;
    }


    @Override
    public String getSource() {
        return source;
    }

    @Override
    public IScatterPlotRangeConfig prop(final String propertyName) {
        this.source += ":" + propertyName;
        return this;
    }
}

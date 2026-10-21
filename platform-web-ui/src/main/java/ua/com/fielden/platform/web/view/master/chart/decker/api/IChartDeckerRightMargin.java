package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract for specifying the right margin of the chart of a deck.
///
public interface IChartDeckerRightMargin<T extends AbstractEntity<?>> extends IChartDeckerWithSeries<T> {

    /// Specifies the right margin of the chart in pixels, which is the space after the last bar.
    /// By default, it is 35 pixels.
    ///
    IChartDeckerWithSeries<T> rightMargin(final int px);

}

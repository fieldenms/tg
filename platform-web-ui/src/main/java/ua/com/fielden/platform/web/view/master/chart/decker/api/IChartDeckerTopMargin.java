package ua.com.fielden.platform.web.view.master.chart.decker.api;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract for specifying the top margin of the chart of a deck.
///
public interface IChartDeckerTopMargin<T extends AbstractEntity<?>> extends IChartDeckerRightMargin<T> {

    /// Specifies the top margin of the chart in pixels.
    /// By default, it is 40 pixels, which is the space for the title of the chart above the bars.
    /// A chart without a title, such as one with its title displayed elsewhere, can use a smaller margin.
    /// The chart keeps room for the labels of the highest bars regardless of the margin.
    ///
    IChartDeckerRightMargin<T> topMargin(final int px);

}

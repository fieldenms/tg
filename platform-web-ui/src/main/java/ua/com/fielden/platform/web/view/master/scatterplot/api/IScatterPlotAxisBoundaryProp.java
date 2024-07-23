package ua.com.fielden.platform.web.view.master.scatterplot.api;

import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

/**
 * A contract to specify a property name that references the data used for calculating the boundaries of values for a scatter plot axis.
 *
 * @author TG Team
 */
public interface IScatterPlotAxisBoundaryProp {

    /**
     * Sets a property name that references the data used for calculating the boundaries of values for a scatter plot axis.
     *
     * @param propertyName
     * @return
     */
    IScatterPlotAxisBoundaryConfig prop(IConvertableToPath propertyName);

}

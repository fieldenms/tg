package ua.com.fielden.platform.web.view.master.scatterplot.api.implementation;

import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.web.centre.exceptions.EntityCentreConfigurationException;
import ua.com.fielden.platform.web.view.master.scatterplot.api.IScatterPlotAxisBoundaryConfig;
import ua.com.fielden.platform.web.view.master.scatterplot.api.IScatterPlotAxisBoundaryProp;

/**
 * Represents a scatter plot axis configuration: a data source and a property name in the data source that is used to configure the boundaries of values for an axis.
 *
 * @author TG Team
 */
public class ScatterPlotAxisBoundary implements IScatterPlotAxisBoundaryConfig, IScatterPlotAxisBoundaryProp {

    public static final String SOURCE_RESULT_SET = "data";
    public static final String SOURCE_MASTER_ENTITY = "masterEntity";
    private final String source;
    private String propName;

    /**
     * An entry for configuring a scatter plot axis boundary, represented by a result set of an Entity Centre with which the scatter plot is associated.
     *
     * @return a contract to specify a property name within the resultset, which should be used to compute the boundaries.
     */
    public static IScatterPlotAxisBoundaryProp resultSet() {
        return new ScatterPlotAxisBoundary(SOURCE_RESULT_SET);
    }

    /**
     * An entry for configuring a scatter plot axis boundary, represented by a master entity behind the scatter plot itself.
     * It is expected that such property would be of collectional nature.
     *
     * @return a contract to specify a property name of the master entity, which should be used to compute the boundaries.
     */
    public static IScatterPlotAxisBoundaryProp masterEntity() {
        return new ScatterPlotAxisBoundary(SOURCE_MASTER_ENTITY);
    }

    private ScatterPlotAxisBoundary(String source) {
        this.source = source;
    }


    @Override
    public String getSource() {
        return source + ":" + propName;
    }

    @Override
    public IScatterPlotAxisBoundaryConfig prop(final IConvertableToPath propertyName) {
        if (propName != null) {
            throw new EntityCentreConfigurationException("Property [%s] has already been specified and cannot be changed.".formatted(propName));
        }
        this.propName = propertyName.toPath();
        return this;
    }

}

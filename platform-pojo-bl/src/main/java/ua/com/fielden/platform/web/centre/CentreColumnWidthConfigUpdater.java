package ua.com.fielden.platform.web.centre;

import java.util.Map;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@CompanionObject(ICentreColumnWidthConfigUpdater.class)
public class CentreColumnWidthConfigUpdater extends AbstractFunctionalEntityWithCentreContext<String> {

    @IsProperty(Object.class)
    @Title("column Parameters")
    private Map<String, Map<String, Integer>> columnParameters = null;

    @Observable
    protected CentreColumnWidthConfigUpdater setColumnParameters(final Map<String, Map<String, Integer>> columnParameters) {
        this.columnParameters = columnParameters;
        return this;
    }

    public Map<String, Map<String, Integer>> getColumnParameters() {
        return columnParameters;
    }
}

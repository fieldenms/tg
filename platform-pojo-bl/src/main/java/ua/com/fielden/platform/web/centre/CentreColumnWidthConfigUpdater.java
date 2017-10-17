package ua.com.fielden.platform.web.centre;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Entity for updating widths and grow factors of entity centre EGI columns.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@CompanionObject(ICentreColumnWidthConfigUpdater.class)
public class CentreColumnWidthConfigUpdater extends AbstractFunctionalEntityWithCentreContext<String> {

    @IsProperty(Object.class)
    @Title("Column Parameters")
    private Map<String, Map<String, Integer>> columnParameters = new HashMap<>();

    @Observable
    protected CentreColumnWidthConfigUpdater setColumnParameters(final Map<String, Map<String, Integer>> columnParameters) {
        this.columnParameters.clear();
        this.columnParameters.putAll(columnParameters);
        return this;
    }

    public Map<String, Map<String, Integer>> getColumnParameters() {
        return Collections.unmodifiableMap(columnParameters);
    }
}

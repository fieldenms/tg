package ua.com.fielden.platform.web.centre;

import java.util.Collections;
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
    @Title(value = "Column widths", desc = "Column widths")
    private Map<String, Integer> propWidths;

    @IsProperty(Object.class)
    @Title(value = "Column grow factors", desc = "Column grow factors")
    private Map<String, Integer> propGrows;

    @Observable
    public CentreColumnWidthConfigUpdater setPropWidths(final Map<String, Integer> propWidths) {
        this.propWidths.clear();
        this.propWidths.putAll(propWidths);
        return this;
    }

    public Map<String, Integer> getPropWidths() {
        return Collections.unmodifiableMap(this.propWidths);
    }

    @Observable
    public CentreColumnWidthConfigUpdater setPropGrows(final Map<String, Integer> propGrows) {
        this.propGrows.clear();
        this.propGrows.putAll(propGrows);
        return this;
    }

    public Map<String, Integer> getPropGrows() {
        return Collections.unmodifiableMap(this.propGrows);
    }
}

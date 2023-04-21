package ua.com.fielden.platform.web.centre.api.resultset.toolbar.impl;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;

import java.util.Collection;
import java.util.List;

import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;

/**
 * A toolbar for insertion points.
 * This is a standard Entity Centre toolbar without custom actions and without the config action.
 *
 * @author TG Team
 *
 */
public class InsertionPointToolbar extends CentreToolbar {

    @Override
    protected DomElement createToolbarElement() {
        return new DomContainer().add(pagination("standart-action"), refreshButton(), helpButton());
    }

    @Override
    public List<String> getAvailableShortcuts() {
        return of(paginationShortcut(), refreshShortcut()).flatMap(Collection::stream).collect(toList());
    }

}
package ua.com.fielden.platform.web.centre.api.resultset.toolbar.impl;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;

import java.util.Collection;
import java.util.List;

import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;

/**
 * Toolbar for insertion points. (This is normal centre toolbar without custom actions and without config button.)
 *
 * @author TG Team
 *
 */
public class InsertionPointToolbar extends CentreToolbar {

    @Override
    protected DomElement createToolbarElement() {
        return new DomContainer().add(pagination("standart-action"), refreshButton());
    }

    @Override
    public List<String> getAvailableShortcuts() {
        return of(paginationShortcut(), refreshShortcut()).flatMap(Collection::stream).collect(toList());
    }
}

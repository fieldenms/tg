package ua.com.fielden.platform.web.centre.api.resultset.toolbar.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;

public class InsertionPointToolbar extends CentreToolbar {

    @Override
    protected DomElement createToolbarElement() {
        return new DomContainer().add(pagination("standart-action"), refreshButton());
    }

    @Override
    public List<String> getAvailableShortcuts() {
        return Stream.of(paginationShortcut(), refreshShortcut()).flatMap(Collection::stream).collect(Collectors.toList());
    }
}

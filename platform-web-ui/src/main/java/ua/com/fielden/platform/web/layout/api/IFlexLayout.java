package ua.com.fielden.platform.web.layout.api;

import java.util.Optional;

public interface IFlexLayout {

    String render(boolean vertical, int gap);

    Optional<Boolean> isVerticalLayout();
}

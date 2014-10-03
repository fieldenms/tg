package ua.com.fielden.platform.web.interfaces;

import ua.com.fielden.platform.web.component.WebComponent;


public interface ILayout<T> extends IRenderable{

    ILayout<T> add(WebComponent component);

    ILayout<T> add(WebComponent component, T constraints);
}

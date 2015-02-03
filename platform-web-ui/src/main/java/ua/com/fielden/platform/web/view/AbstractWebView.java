package ua.com.fielden.platform.web.view;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.SingleDomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.web.WebUtils;
import ua.com.fielden.platform.web.action.AbstractWebAction;
import ua.com.fielden.platform.web.component.AbstractWebComponent;
import ua.com.fielden.platform.web.component.WebPanel;
import ua.com.fielden.platform.web.model.WebModel;

/**
 * Represents the custom polymer element.
 *
 * @author TG Team
 *
 */
public abstract class AbstractWebView<WM extends WebModel> extends WebPanel {

    private final WM webModel;
    private final String name;

    public AbstractWebView(final WM webModel, final String name) {
        this.webModel = webModel;
        this.name = name;
    }

    public WM getWebModel() {
        return webModel;
    }

    public String getName() {
        return name;
    }

    @Override
    public DomElement render() {
        final DomElement polymerElement = new DomElement("polymer-element").attr("name", WebUtils.polymerTagName(this));
        polymerElement.add(new DomElement("template").add(generateAjaxElements()).add(super.render()));
        if (getWebModel() != null) {
            polymerElement.add(new DomElement("script").add(new InnerTextElement("(function() {\nPolymer(" + getWebModel().generate() + ");\n})();")));
        } else {
            polymerElement.attr("noscript", null);
        }
        return new DomContainer().
                add(importHTML("/resources/polymer/polymer/polymer.html")).
                add(generateModelImports()).
                add(generateViewImports()).
                add(polymerElement);
    }

    private DomElement[] generateViewImports() {
        final List<DomElement> viewImports = new ArrayList<>();
        for (final AbstractWebComponent component : components) {
            component.imports().forEach(path -> viewImports.add(importHTML(path)));
        }
        return viewImports.toArray(new DomElement[0]);
    }

    /**
     * Generate appropriate core-ajax web components for the specified actions.
     */
    private DomElement[] generateAjaxElements() {
        final List<DomElement> ajaxElements = new ArrayList<>();
        if (getWebModel() != null) {
            for (final AbstractWebAction<?> action : getWebModel().getActions()) {
                ajaxElements.add(createAjaxElement(action));
            }
        }
        return ajaxElements.toArray(new DomElement[0]);
    }

    private DomElement createAjaxElement(final AbstractWebAction<?> action) {
        final DomElement ajax = new DomElement("core-ajax").
                attr("url", "/users/SU/" + action.getFuncEntityClass().getSimpleName()).
                attr("method", "POST").
                attr("handleas", "json").
                attr("id", "id_" + action.hashCode()).
                attr("on-core-response", "{{onResponse_" + action.hashCode() + "}}").
                attr("on-core-error", "{{onError_" + action.hashCode() + "}}");
        return ajax;
    }

    private DomElement[] generateModelImports() {
        final List<DomElement> modelImports = new ArrayList<>();
        if (getWebModel() != null) {
            for (final String path : getWebModel().getImports()) {
                modelImports.add(importHTML(path));
            }
        }
        return modelImports.toArray(new DomElement[0]);
    }

    private DomElement importHTML(final String path) {
        return new SingleDomElement("link").attr("rel", "import").attr("href", path);
    }
}

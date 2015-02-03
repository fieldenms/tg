package ua.com.fielden.platform.web.component;

import java.util.List;

import ua.com.fielden.platform.dom.DomElement;

/**
 * Represents the polymer's paper-input component.
 *
 * @author TG Team
 *
 */
public class WebInput extends AbstractWebComponent {

    private final String property;
    private final String caption;

    public WebInput(final String caption, final String property) {
        this.caption = caption;
        this.property = property;
    }

    @Override
    public List<String> imports() {
        final List<String> htmlImports = super.imports();
        htmlImports.add("/resources/polymer/paper-input/paper-input.html");
        return htmlImports;
    }

    @Override
    public DomElement render() {
        return new DomElement("paper-input").attr("label", caption).attr("inputValue", "{{" + property + "}}");
    }

}

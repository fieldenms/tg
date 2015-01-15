package ua.com.fielden.platform.web.component;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.web.model.WebModel;

/**
 * Represents a HTML text element that can be bind to property or just contain some text formatted with HTML.
 *
 * @author TG Team
 *
 */
public class WebLabel extends AbstractWebComponent {

    /**
     * This is a property that is binded to this label.
     */
    private final String propertyName;
    private final WebModel webModel;

    private final String caption;

    public WebLabel(final WebModel webModel, final String propertyName) {
        this.webModel = webModel;
        this.propertyName = propertyName;
        this.caption = null;
    }

    public WebLabel(final String caption) {
        this.webModel = null;
        this.propertyName = null;
        this.caption = caption;
    }

    @Override
    public DomElement render() {
        return new InnerTextElement(webModel == null || propertyName == null ? caption : "{{" + propertyName + "}}");
    }

}

package ua.com.fielden.platform.web.component;

import java.util.List;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.TextElement;
import ua.com.fielden.platform.web.action.AbstractWebAction;

/**
 * Represents the polymer's paper-button component.
 *
 * @author TG Team
 *
 */
public class WebButton extends AbstractWebComponent {

    private final AbstractWebAction<?> action;

    private boolean raised = false;

    public WebButton(final AbstractWebAction<?> action) {
	this.action = action;
    }

    public WebButton setRaised(final boolean raised) {
	this.raised = raised;
	return this;
    }

    @Override
    public List<String> imports() {
        final List<String> htmlImports = super.imports();
        htmlImports.add("/resources/polymer/paper-button/paper-button.html");
        return htmlImports;
    }

    @Override
    public DomElement render() {
	final DomElement button = new DomElement("paper-button");
	if (raised) {
	    button.attr("raised", null);
	}
	return button.attr("on-click", "{{onAction_" + action.hashCode() + "}}").add(new TextElement(action.getCaption()));
    }

}

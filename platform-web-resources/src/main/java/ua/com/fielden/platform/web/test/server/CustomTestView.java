package ua.com.fielden.platform.web.test.server;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.custom_view.CustomView;
import ua.com.fielden.platform.web.interfaces.IRenderable;

public class CustomTestView extends CustomView {

    public CustomTestView() {
        super("CustomView");
    }

    @Override
    public IRenderable build() {
        final String text = ResourceLoader.getText("ua/com/fielden/platform/web/spikes/custom-view-template.html");
        final String customView = text.
                replaceAll("@viewName", getViewName()).
                replace("@customViewContent", "It's cutom view content");

        return new IRenderable() {

            @Override
            public DomElement render() {
                return new InnerTextElement(customView);
            }
        };
    }

}

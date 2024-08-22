package ua.com.fielden.platform.owasp.html;

import org.owasp.html.HtmlChangeListener;
import org.owasp.html.PolicyFactory;

import javax.annotation.Nullable;

/**
 * Base class for {@link HtmlChangeListener}s that require no context.
 * <p>
 * {@link #sanitize(PolicyFactory, String)} is the preferred way of using this listener.
 */
public abstract class SimpleHtmlChangeListener implements HtmlChangeListener<Void> {

    public abstract void discardedTag(String elementName);

    public abstract void discardedAttributes(String tagName, String... attributeNames);

    @Override
    public final void discardedTag(@Nullable Void context, String elementName) {
        discardedTag(elementName);
    }

    @Override
    public final void discardedAttributes(@Nullable Void context, String tagName, String... attributeNames) {
        discardedAttributes(tagName, attributeNames);
    }

    public String sanitize(final PolicyFactory policyFactory, final String html) {
        return policyFactory.sanitize(html, this, null);
    }

}

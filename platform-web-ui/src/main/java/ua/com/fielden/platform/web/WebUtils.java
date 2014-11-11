package ua.com.fielden.platform.web;

import ua.com.fielden.platform.web.view.AbstractWebView;

/**
 * Utilities for TG Web Framework.
 *
 * @author TG Team
 *
 */
public class WebUtils {

    /**
     * Returns valid web component name for the given {@link AbstractWebView} instance.
     *
     * @param webView
     * @return
     */
    public static <WV extends AbstractWebView<?>> String polymerTagName(final WV webView) {
	return "tg-" + webView.getClass().getSimpleName().toLowerCase();
    }

}

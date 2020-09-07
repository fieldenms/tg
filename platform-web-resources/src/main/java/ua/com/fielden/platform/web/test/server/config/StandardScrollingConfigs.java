package ua.com.fielden.platform.web.test.server.config;

import ua.com.fielden.platform.web.centre.api.resultset.scrolling.IScrollConfig;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.impl.ScrollConfig;

public class StandardScrollingConfigs {
    public static IScrollConfig standardStandaloneScrollingConfig(final int firstPropsToFix) {
        return ScrollConfig.configScroll().withFixedCheckboxesPrimaryActionsAndFirstProps(firstPropsToFix).withFixedSecondaryActions().withFixedHeader().done();
    }

    public static IScrollConfig standardEmbeddedScrollingConfig(final int firstPropsToFix) {
        return ScrollConfig.configScroll().withFixedCheckboxesPrimaryActionsAndFirstProps(firstPropsToFix).withFixedSecondaryActions().withFixedHeader().done();
    }

}

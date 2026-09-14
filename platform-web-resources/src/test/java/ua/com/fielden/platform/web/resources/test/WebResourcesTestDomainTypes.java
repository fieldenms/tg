package ua.com.fielden.platform.web.resources.test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.web.resources.webui.test_entities.Action1;
import ua.com.fielden.platform.web.resources.webui.test_entities.Action2;
import ua.com.fielden.platform.web.resources.webui.test_entities.Action3;

import java.util.List;

import static ua.com.fielden.platform.utils.CollectionUtil.concatList;

/// A class to enlist platform test domain entities for web resource tests.
///
class WebResourcesTestDomainTypes extends PlatformTestDomainTypes {

    @Override
    public List<Class<? extends AbstractEntity<?>>> entityTypes() {
        return concatList(
                super.entityTypes(),
                // Additional entity types go here.
                List.of(Action1.class,
                        Action2.class,
                        Action3.class));
    }

}

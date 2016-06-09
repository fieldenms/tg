package ua.com.fielden.platform.web.menu.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.menu.module.impl.WebMenuModule;
import ua.com.fielden.platform.web.minijs.JsCode;

public class WebMainMenu implements IExecutable {

    private final List<WebMenuModule> modules = new ArrayList<>();

    public WebMenuModule addModule(final String title) {
        final WebMenuModule module = new WebMenuModule(title);
        modules.add(module);
        return module;
    }

    @Override
    public JsCode code() {
        final String menuModules = "[" + StringUtils.join(modules, ",") + "]";
        return new JsCode(menuModules);
    }
}

package ua.com.fielden.platform.web.app;

import org.apache.commons.lang.StringUtils;

public class WebAppUtils {

    /**
     * Generates the entity master's component name for the specified entity type name.
     *
     * @param clazz
     * @return
     */
    public static String generateMasterName(final Class<?> clazz) {
        return "tg-" + StringUtils.lowerCase(StringUtils.join(clazz.getSimpleName().split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"), '-')) + ".html";
    }

}

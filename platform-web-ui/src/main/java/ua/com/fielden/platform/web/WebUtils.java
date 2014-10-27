package ua.com.fielden.platform.web;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.web.model.WebModel;

public class WebUtils {

    public static <WM extends WebModel> String controllerName(final WM webModel) {
	return functionalName(webModel) + "Controller";
    }

    public static <WM extends WebModel> String angularModulePath(final WM webModel, final String appSpecificResourcePath) {
        return "/resources/" + dashPath(webModel.getClass().getName()).replaceAll(appSpecificResourcePath, "") + "/" + angularModuleName(webModel);
    }

    private static String dashPath(final String className) {
	System.out.println("className = " + className);
	final String substring = className.substring(0, className.lastIndexOf("."));
	System.out.println("substring = " + substring);
	final String replaceAll = substring.replaceAll("\\.", "/");
	System.out.println("replaceAll = " + replaceAll);
	return replaceAll;
    }

    public static <WM extends WebModel> String angularModuleName(final WM webModel) {
        return StringUtils.uncapitalize(functionalName(webModel)) + "Module";
    }

    /**
     * Returns a functional name of the logic which is covered by this {@link WebModel}. Typically the web model should be named as with convention like
     * "CustomFunctionalityWebModel".
     *
     * @return
     */
    private static <WM extends WebModel> String functionalName(final WM webModel) {
        final String className = className(webModel);
        return className.replaceAll("WebModel", "");
    }

    private static <WM extends WebModel> String className(final WM webModel) {
	return webModel.getClass().getSimpleName();
    }
}

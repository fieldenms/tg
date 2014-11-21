package ua.com.fielden.platform.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.master.EntityMaster;
import ua.com.fielden.platform.web.view.AbstractWebView;

/**
 * The web application configurator. Allows one to specify which entity centre, entity master and other custom parameters for the application.
 *
 * @author TG Team
 *
 */
public class WebAppConfig{

    private final String appName;

    /*
     * Properties related to device configuration (i.e. their minimal and maximal width). The default values are in pt (1/72 inch).
     * This values will be used to configure ng-matchmedia module.
     */
    private float desktopMinWidth = 770;
    private float tabletMaxWidth = 770, tabletMinWidth = 300;
    private float phoneMaxWidth = 300;

    /*
     * The properties below represent essential tg views: entity centres, entity masters. Also it is possible to specify custom view.
     */
    private final Map<String, EntityCentre> centres = new LinkedHashMap<>();
    /*
     * The properties below represent essential tg views: entity centres, entity masters. Also it is possible to specify custom view.
     */
    private final List<EntityMaster> masters = new ArrayList<>();
    private final Map<String, AbstractWebView<?>> customViews = new LinkedHashMap<>();
    private String defaultRoute = null;

    /**
     * Creates web application instance and initialises it with application name.
     *
     * @param appName
     */
    public WebAppConfig(final String appName) {
	this.appName = appName;
    }

    /**
     * Set the minimal screen width of the desktop device.
     *
     * @param minWidth
     * @return
     */
    public WebAppConfig desktop(final float minWidth) {
	this.desktopMinWidth = minWidth;
	return this;
    }

    /**
     * Set the minimal and maximal screen width of the tablet device.
     *
     * @param minWidth
     * @param maxWidth
     * @return
     */
    public WebAppConfig tablet(final float minWidth, final float maxWidth) {
	if (maxWidth < minWidth) {
	    throw new IllegalArgumentException("The min tablet width can not be greater then the max tablet width: " + minWidth + " > " + maxWidth);
	}
	this.tabletMaxWidth = maxWidth;
	this.tabletMinWidth = minWidth;
	return this;
    }

    /**
     * Set the maximal screen width of the phone device.
     *
     * @param maxWidth
     * @return
     */
    public WebAppConfig phone(final float maxWidth) {
	this.phoneMaxWidth = maxWidth;
	return this;
    }

    /**
     * Returns the minimal screen width of the desktop device.
     *
     * @return
     */
    public float desktopMinWidth() {
	return desktopMinWidth;
    }

    /**
     * Returns the maximal screen width of the tablet device.
     *
     * @return
     */
    public float tabletMaxWidth() {
	return tabletMaxWidth;
    }

    /**
     * Returns the minimal screen width of the tablet device.
     *
     * @return
     */
    public float tabletMinWidth() {
	return tabletMinWidth;
    }

    /**
     * Returns the maximal screen width of the phone device.
     *
     * @return
     */
    public float phoneMaxWidth() {
	return phoneMaxWidth;
    }

    /**
     * Adds new {@link EntityCentre} instance to this web application configuration.
     *
     * @param centre
     */
    public void addCentre(final EntityCentre centre) {
	addCentre(centre, false);
    }

    /**
     * Adds new {@link EntityCentre} instance to this web application configuration.
     *
     * @param centre
     */
    public void addCentre(final EntityCentre centre, final boolean isDefault) {
	centres.put(centre.getMenuItemType().getName(), centre);
	if (isDefault) {
	    defaultRoute = generateCentreHash(centre);
	}
    }

    private String generateCentreHash(final EntityCentre centre) {
	return "centre/" + centre.getMenuItemType().getName();
    }

    public void addCustomView(final AbstractWebView<?> webView) {
	addCustomView(webView, false);
    }

    /**
     * Adds new custom {@link AbstractWebView} to this web application configuration.
     *
     * @param webView
     */
    public void addCustomView(final AbstractWebView<?> webView, final boolean isDefault) {
	customViews.put(WebUtils.polymerTagName(webView), webView);
	if (isDefault) {
	    defaultRoute = generateCustomViewHash(webView);
	}
    }

    private String generateCustomViewHash(final AbstractWebView<?> webView) {
	return "webview/" + WebUtils.polymerTagName(webView);
    }

    /**
     * Returns registered entity centres.
     *
     * @return
     */
    public Map<String, EntityCentre> getCentres() {
	return Collections.unmodifiableMap(centres);
    }

    /**
     * Returns registered custom views.
     *
     * @return
     */
    public Map<String, AbstractWebView<?>> getCustomViews() {
	return Collections.unmodifiableMap(customViews);
    }

    /**
     * Runs the web application by sending the html file for this application.
     *
     * @return
     */
    public String run() {
	// TODO should check whether user is authentic first.
	return ResourceLoader.getText("ua/com/fielden/platform/web/app.html").
		replaceAll("@defaultView", defaultRoute == null ? "0" : "'" + defaultRoute + "'").
		replaceAll("@appName", appName).
		replaceAll("@pages", generatePages());
    }

    private String generatePages() {
	final List<String> pagesBuilder = new ArrayList<>();

	customViews.forEach((key, value) -> {
	    pagesBuilder.add("{ name: '" + value.getName() + "'," +
		    	       "hash: '" + generateCustomViewHash(value) + "',"+
		    	       "url: '/webview/" + key + "',"+
		    	       "lazyLoad: false}");
	});
	centres.forEach((key, value) -> {
	    pagesBuilder.add("{ name: '" + value.getName() + "',"+
		    	       "hash: '" + generateCentreHash(value) + "',"+
		    	       "url: '/resources/centre/entity-centre.html'," +
		    	       "attributes: {centreName: '" + key + "'}," +
		    	       "lazyLoad: false}");
	});

	return "[\n" + StringUtils.join(pagesBuilder, ",\n") + "\n]";
    }
}

package ua.com.fielden.platform.swing.review;

import java.util.List;

import org.apache.commons.lang.StringUtils;

public class PropertiesNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -3338881347188690711L;

    private final List<String> notFoundedCriteriaProperties;
    private final List<String> notFoundedFetchProperties;

    public PropertiesNotFoundException(final List<String> notFoundedCriteriaProperties, final List<String> notFoundedFetchProperties) {
	this.notFoundedCriteriaProperties = notFoundedCriteriaProperties;
	this.notFoundedFetchProperties = notFoundedFetchProperties;
    }

    @Override
    public String getMessage() {
	String message = notFoundedCriteriaProperties.size() > 0 ? "<html>Failed to locate next criteria properties:<br>" : "";
	for (int propertyIndex = 0; propertyIndex < notFoundedCriteriaProperties.size(); propertyIndex++) {
	    message += "<br>" + notFoundedCriteriaProperties.get(propertyIndex);
	}
	final String messagePrefix = StringUtils.isEmpty(message) ? "<html>" : "<br><br>";
	message += notFoundedFetchProperties.size() > 0 ? messagePrefix + "Failed to locate next fetch properties:<br>" : message;
	for (int propertyIndex = 0; propertyIndex < notFoundedFetchProperties.size(); propertyIndex++) {
	    message += "<br>" + notFoundedFetchProperties.get(propertyIndex);
	}

	return message + "</html>";
    }
}

package ua.com.fielden.platform.web.centre.api;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity_centre.review.criteria.DynamicPropForExport;

public interface IDynamicPropConfig {

    List<Map<String, String>> build();

    List<DynamicPropForExport> buildToExport();
}

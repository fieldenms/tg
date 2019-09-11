package ua.com.fielden.platform.web.centre.api;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity_centre.review.criteria.DynamicPropForExport;

/**
 * A contract that should return dynamic properties configuration for entity centre generation or export function
 *
 * @author TG Team
 *
 */
public interface IDynamicPropConfig {

    List<Map<String, String>> build();

    List<DynamicPropForExport> buildToExport();
}

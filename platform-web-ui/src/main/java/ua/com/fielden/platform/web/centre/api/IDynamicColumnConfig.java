package ua.com.fielden.platform.web.centre.api;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity_centre.review.criteria.DynamicColumnForExport;

/**
 * A contract that should return dynamic properties configuration for entity centre generation or the export function.
 *
 * @author TG Team
 *
 */
public interface IDynamicColumnConfig {

    List<Map<String, Object>> build();

    List<DynamicColumnForExport> buildToExport();
}

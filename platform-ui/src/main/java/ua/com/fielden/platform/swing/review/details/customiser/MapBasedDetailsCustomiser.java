package ua.com.fielden.platform.swing.review.details.customiser;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.swing.review.details.IDetails;

/**
 * {@link IDetailsCustomiser} for analysis.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public class MapBasedDetailsCustomiser implements IDetailsCustomiser {

    @SuppressWarnings("rawtypes")
    private final Map<Class, IDetails> detailsMap;

    public MapBasedDetailsCustomiser() {
        detailsMap = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <DT> IDetails<DT> getDetails(final Class<DT> detailsParamType) {
        return detailsMap.get(detailsParamType);
    }

    public MapBasedDetailsCustomiser addDetails(final Class<?> detailsClass, final IDetails<?> details) {
        detailsMap.put(detailsClass, details);
        return this;
    }

    public MapBasedDetailsCustomiser removeDetails(final Class<?> detailsClass) {
        detailsMap.remove(detailsClass);
        return this;
    }
}

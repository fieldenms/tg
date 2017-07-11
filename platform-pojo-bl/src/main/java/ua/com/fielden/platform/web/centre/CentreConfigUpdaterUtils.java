package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isShortCollection;
import static ua.com.fielden.platform.web.centre.WebApiUtils.dslName;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.annotation.CustomProp;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.utils.Pair;

/**
 * Utilities for {@link CentreConfigUpdater} and its implementation.
 * 
 * @author TG Team
 *
 */
public class CentreConfigUpdaterUtils {
    
    /**
     * Creates ordered set of {@link CustomisableColumn} entities to be rendered in {@link CentreConfigUpdater} master.
     * 
     * @param checkedPropertiesWithoutSummaries -- centre's checked properties without 'totals' ('summary properties')
     * @param sortedProperties -- centre's sorted properties
     * @param managedType -- managed type behind the centre manager
     * @param factory -- entity factory
     * @return
     */
    public static LinkedHashSet<CustomisableColumn> createCustomisableColumns(final List<String> checkedPropertiesWithoutSummaries, final List<Pair<String, Ordering>> sortedProperties, final Class<?> managedType, final EntityFactory factory) {
        final LinkedHashSet<CustomisableColumn> result = new LinkedHashSet<>();
        for (final String checkedProp: checkedPropertiesWithoutSummaries) {
            final Pair<String, String> titleAndDesc = CriteriaReflector.getCriteriaTitleAndDesc(managedType, checkedProp);
            final CustomisableColumn customisableColumn = factory.newEntity(CustomisableColumn.class, null, dslName(checkedProp), titleAndDesc.getValue());
            customisableColumn.setTitle(titleAndDesc.getKey());
            if ("".equals(checkedProp) || 
                    (!AnnotationReflector.isPropertyAnnotationPresent(CustomProp.class, managedType, checkedProp) && 
                    !isShortCollection(managedType, checkedProp))
            ) {
                customisableColumn.setSortable(true);
                final Pair<Ordering, Integer> sortingValueAndNumber = getSortingValueAndNumber(sortedProperties, checkedProp);
                if (sortingValueAndNumber != null) {
                    customisableColumn.setSorting(Ordering.ASCENDING == sortingValueAndNumber.getKey()); // 'null' is by default, means no sorting exist
                    customisableColumn.setSortingNumber(sortingValueAndNumber.getValue());
                }
            }
            result.add(customisableColumn);
        }
        return result;
    }
    
    private static Pair<Ordering, Integer> getSortingValueAndNumber(final List<Pair<String, Ordering>> sortedProperties, final String prop) {
        for (final Pair<String, Ordering> sortedProperty : sortedProperties) {
            if (sortedProperty.getKey().equals(prop)) {
                return Pair.pair(sortedProperty.getValue(), sortedProperties.indexOf(sortedProperty));
            }
        }
        return null;
    }
    
    /**
     * Creates simplified representation of sorting information as a list of strings similar to [prop1:asc] / [prop2:desc].
     * 
     * @param customisableColumns
     * @return
     */
    public static List<String> createSortingVals(final LinkedHashSet<CustomisableColumn> customisableColumns) {
        return new ArrayList<>(
            customisableColumns.stream()
            .filter(customisableColumn -> customisableColumn.getSortingNumber() >= 0) // consider only 'sorted' properties
            .sorted((o1, o2) -> o1.getSortingNumber().compareTo(o2.getSortingNumber()))
            .map(customisableColumn -> customisableColumn.getKey() + ':' + (Boolean.TRUE.equals(customisableColumn.getSorting()) ? "asc" : "desc"))
            .collect(Collectors.toCollection(LinkedHashSet::new))
        );
    }
    
}

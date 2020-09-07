package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering.ASCENDING;
import static ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering.DESCENDING;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isShortCollection;
import static ua.com.fielden.platform.web.centre.WebApiUtils.dslName;
import static ua.com.fielden.platform.web.centre.WebApiUtils.treeName;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
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
    
    /** Private default constructor to prevent instantiation. */
    private CentreConfigUpdaterUtils() {
    }
    
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
    public static List<String> createSortingVals(final Set<CustomisableColumn> customisableColumns) {
        return new ArrayList<>(
            customisableColumns.stream()
            .filter(customisableColumn -> customisableColumn.getSortingNumber() >= 0) // consider only 'sorted' properties
            .sorted((o1, o2) -> o1.getSortingNumber().compareTo(o2.getSortingNumber()))
            .map(customisableColumn -> customisableColumn.getKey() + ':' + (Boolean.TRUE.equals(customisableColumn.getSorting()) ? "asc" : "desc"))
            .collect(Collectors.toCollection(LinkedHashSet::new))
        );
    }
    
    /**
     * Removes all sorting configuration and column order / visibility from result-set tick <code>secondTick</code>.
     * 
     * @param secondTick
     * @param root
     */
    public static void removeOrderVisibilityAndSorting(final IAddToResultTickManager secondTick, final Class<?> root) {
        // remove sorting information
        final List<Pair<String, Ordering>> currOrderedProperties = new ArrayList<>(secondTick.orderedProperties(root));
        for (final Pair<String, Ordering> orderedProperty: currOrderedProperties) {
            if (ASCENDING == orderedProperty.getValue()) {
                secondTick.toggleOrdering(root, orderedProperty.getKey());
            }
            secondTick.toggleOrdering(root, orderedProperty.getKey());
        }
        
        // remove usage information
        final List<String> currUsedProperties = secondTick.usedProperties(root);
        for (final String currUsedProperty: currUsedProperties) {
            secondTick.use(root, currUsedProperty, false);
        }
    }
    
    /**
     * Applies new sorting configuration and column order / visibility against result-set tick <code>secondTick</code>. {@link WebApiUtils#treeName(String)} normalisation of property names is not required,
     * it is done inside the function.
     * 
     * @param secondTick
     * @param root
     * @param newUsedProps
     * @param newSortingVals
     */
    public static void applyNewOrderVisibilityAndSorting(final IAddToResultTickManager secondTick, final Class<?> root, final Set<String> newUsedProps, final List<String> newSortingVals) {
        removeOrderVisibilityAndSorting(secondTick, root);
        
        // apply usage information
        for (final String chosenId: newUsedProps) {
            secondTick.use(root, treeName(chosenId), true);
        }
        
        // apply sorting information
        for (final String sortingVal: newSortingVals) {
            final String[] splitted = sortingVal.split(":");
            final String name = treeName(splitted[0]);
            secondTick.toggleOrdering(root, name);
            if ("desc".equals(splitted[1])) {
                secondTick.toggleOrdering(root, name);
            }
        }
    }
    
    /**
     * Applies new sorting configuration and column order / visibility against result-set tick <code>secondTick</code>. {@link WebApiUtils#treeName(String)} normalisation of property names is required 
     * for <code>newUsedProps</code> and <code>newSortingProps</code>.
     * 
     * @param secondTick
     * @param root
     * @param newUsedProps
     * @param newSortingProps
     */
    public static void applyNewOrderVisibilityAndSorting(final IAddToResultTickManager secondTick, final Class<?> root, final List<String> newUsedProps, final List<Pair<String, Ordering>> newSortingProps) {
        removeOrderVisibilityAndSorting(secondTick, root);
        
        // apply usage information
        for (final String usedProp : newUsedProps) {
            secondTick.use(root, usedProp, true);
        }
        
        // apply sorting information
        for (final Pair<String, Ordering> sortingProp: newSortingProps) {
            final String name = sortingProp.getKey();
            secondTick.toggleOrdering(root, name);
            if (DESCENDING.equals(sortingProp.getValue())) {
                secondTick.toggleOrdering(root, name);
            }
        }
    }
    
}
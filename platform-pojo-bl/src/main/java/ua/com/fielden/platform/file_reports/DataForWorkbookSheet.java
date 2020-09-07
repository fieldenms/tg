package ua.com.fielden.platform.file_reports;

import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity_centre.review.criteria.DynamicColumnForExport;
import ua.com.fielden.platform.types.tuples.T2;

/**
 * This is a package-private helper class to represent data that is used for building an Excel workbook sheet.
 * 
 * @author TG Team
 *
 * @param <E>
 */
public class DataForWorkbookSheet<E extends AbstractEntity<?>> {
    private final String sheetTitle;
    private final Stream<E> entities;
    private final List<String> propNames = new ArrayList<>();
    private final List<String> propTitles = new ArrayList<>();
    private final Map<String, DynamicColumnForExport> collectionalProperties = new LinkedHashMap<>();

    public DataForWorkbookSheet(final String sheetTitle, final Stream<E> entities, final List<T2<String, String>> propertyNamesAndTitles, final Map<String, DynamicColumnForExport> collectionalProperties) {
        this.sheetTitle = sheetTitle;
        this.entities = entities;
        for (final T2<String, String> pair : propertyNamesAndTitles) {
            propNames.add(pair._1);
            propTitles.add(pair._2);
        }
        this.collectionalProperties.putAll(collectionalProperties);
    }

    public String getSheetTitle() {
        return sheetTitle;
    }

    public Stream<E> getEntities() {
        return entities;
    }

    public List<String> getPropNames() {
        return propNames;
    }

    public List<String> getPropTitles() {
        return propTitles;
    }

    /**
     * An abstraction to correctly obtain the value of property by {@code propName} from {@code entity}.
     * This is needed to correctly handle cases of exporting entities that have their collectional properties in-lined and represented as dynamic properties.
     *
     * @param entity
     * @param propName
     * @return
     */
    public <M extends AbstractEntity<?>> Object getValue(final M entity, final String propName) {
        if (collectionalProperties.containsKey(propName)) {
            final DynamicColumnForExport collectionalProperty = collectionalProperties.get(propName);
            final Collection<? extends AbstractEntity<?>> collection = entity.get(collectionalProperty.getCollectionalPropertyName());
            final Optional<? extends AbstractEntity<?>> subEntity = collection.stream()
                    .filter(e -> equalsEx(e.get(collectionalProperty.getGroupProp()), collectionalProperty.getGroupPropValue()))
                    .findFirst();
            return subEntity.map(e -> e.get(collectionalProperty.getDisplayProp())).orElse(null);
        }
        return entity.get(propName);
    }
}
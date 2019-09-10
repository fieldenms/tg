package ua.com.fielden.platform.file_reports;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity_centre.review.criteria.DynamicPropForExport;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class DataForWorkbookSheet<E extends AbstractEntity<?>> {
    private final String sheetTitle;
    private final Stream<E> entities;
    private final List<String> propNames = new ArrayList<>();
    private final List<String> propTitles = new ArrayList<>();
    private final Map<String, DynamicPropForExport> collectionalProperties;

    public DataForWorkbookSheet(final String sheetTitle, final Stream<E> entities, final List<Pair<String, String>> propertyNamesAndTitles, final Map<String, DynamicPropForExport> collectionalProperties) {
        super();
        this.sheetTitle = sheetTitle;
        this.entities = entities;
        for (final Pair<String, String> pair : propertyNamesAndTitles) {
            propNames.add(pair.getKey());
            propTitles.add(pair.getValue());
        }
        this.collectionalProperties = collectionalProperties;
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

    public <M extends AbstractEntity<?>> Object getValue(final M entity, final String propertyName) {
        if (collectionalProperties.containsKey(propertyName)) {
            final DynamicPropForExport collectionalProperty = collectionalProperties.get(propertyName);
            final Set<? extends AbstractEntity<?>> collection = entity.get(collectionalProperty.getCollectionalPropertyName());
            final Optional<? extends AbstractEntity<?>> subEntity = collection.stream()
                    .filter(e -> EntityUtils.equalsEx(e.get(collectionalProperty.getKeyProp()), collectionalProperty.getKeyPropValue()))
                    .findFirst();
            return subEntity.map(e -> e.get(collectionalProperty.getValueProp())).orElse(null);
        }
        return entity.get(propertyName);
    }
}
package ua.com.fielden.platform.entity;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity_centre.review.criteria.DynamicColumnForExport;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.file_reports.WorkbookExporter;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/**
 * DAO implementation for companion object {@link EntityExportActionCo}.
 *
 * @author TG Team
 *
 */
@EntityType(EntityExportAction.class)
public class EntityExportActionDao extends CommonEntityDao<EntityExportAction> implements EntityExportActionCo {
    private final ICriteriaEntityRestorer criteriaEntityRestorer;

    @Inject
    public EntityExportActionDao(final IFilter filter, final ICriteriaEntityRestorer criteriaEntityRestorer) {
        super(filter);
        this.criteriaEntityRestorer = criteriaEntityRestorer;
    }

    @Override
    public EntityExportAction new_() {
        final EntityExportAction entity = super.new_();
        entity.setExportAll(true);
        return entity;
    }

    @Override
    @SessionRequired
    public EntityExportAction save(final EntityExportAction entity) {
        //If entity is not valid then throw validation result
        final Result validation = entity.isValid();
        if (!validation.isSuccessful()) {
            throw validation;
        }
        // Otherwise continue data exporting.
        final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit = criteriaEntityRestorer.restoreCriteriaEntity(entity.getCentreContextHolder());

        entity.setFileName(String.format("export-of-%s.xlsx", selectionCrit.getEntityClass().getSimpleName()));
        entity.setMime("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        final List<Stream<AbstractEntity<?>>> entities = new ArrayList<>();
        final List<Pair<String[], String[]>> propAndTitles = new ArrayList<>();
        final List<List<List<DynamicColumnForExport>>> dynamicProperties = new ArrayList<>();
        final List<String> titles = new ArrayList<>();
        // selectionCrit.getDynamicProperties() are used only for EntityExportAction and only in this class;
        //   they are initialised in below selectionCrit.export(...) calls; see selectionCrit.setDynamicProperties method callers for more details;
        //   that's why there is no need to initialise selectionCrit.getDynamicProperties() anywhere outside EntityExportAction, i.e. for other functional actions.
        final Object resultSetHidden = selectionCrit.centreContextHolder().getCustomObject().get("@@resultSetHidden");
        if (resultSetHidden != null && !Boolean.valueOf(resultSetHidden.toString())) {
            final String mainEgiTitle = extractSheetTitle(selectionCrit);
            titles.add(mainEgiTitle);
            entities.add(exportEntities(entity, selectionCrit, mainEgiTitle));
            propAndTitles.add(selectionCrit.generatePropTitlesToExport());
            dynamicProperties.add(selectionCrit.getDynamicProperties());
        }
        if (!entity.getCentreContextHolder().proxiedPropertyNames().contains("relatedContexts")) {
            entity.getCentreContextHolder().getRelatedContexts().entrySet().forEach(contextEntry -> {
                final Object insPointResultSetHiden = contextEntry.getValue().getCustomObject().get("@@resultSetHidden");
                if (insPointResultSetHiden != null && !Boolean.valueOf(insPointResultSetHiden.toString())) {
                    final EnhancedCentreEntityQueryCriteria<?, ?> relatedSelectionCrit = criteriaEntityRestorer.restoreCriteriaEntity(contextEntry.getValue());
                    final String sheetTitle = extractSheetTitle(relatedSelectionCrit);
                    titles.add(sheetTitle);
                    entities.add(exportEntities(entity, relatedSelectionCrit, sheetTitle));
                    propAndTitles.add(relatedSelectionCrit.generatePropTitlesToExport());
                    dynamicProperties.add(relatedSelectionCrit.getDynamicProperties());
                }
            });
        }

        if (entities.isEmpty()) {
            throw failure("There is nothing to export");
        }

        try {
            entity.setData(WorkbookExporter.convertToByteArray(WorkbookExporter.export(entities, propAndTitles, dynamicProperties, titles)));
        } catch (final IOException e) {
            throw failure("An exception occurred during the data export.", e);
        } finally {
            entities.forEach(entitiesStream -> entitiesStream.close());
        }

        return entity;
    }

    private String extractSheetTitle(final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) {
        final CentreContextHolder centreContextHolder = selectionCrit.centreContextHolder();
        final String sheetTitle = (String)centreContextHolder.getCustomObject().get("@@insertionPointTitle");
        if (isEmpty(sheetTitle)) {
            return TitlesDescsGetter.getEntityTitleAndDesc(selectionCrit.getEntityClass()).getKey();
        }
        return sheetTitle;
    }

    private Stream<AbstractEntity<?>> exportEntities(final EntityExportAction entity, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final String sheetTitle) {
        if (entity.isExportAll()) {
            return selectionCrit.export(new LinkedHashMap<>());
        } else if (entity.isExportTop()) {
            return selectionCrit.export(linkedMapOf(t2("fetchSize", entity.getNumber()))).limit(entity.getNumber());
        } else {
            final List<Long> selectedEntitiesIds = selectionCrit.centreContextHolder().getSelectedEntities().stream().map(AbstractEntity::getId).collect(toList());
            if (selectedEntitiesIds.isEmpty()) {
                throw failuref("Please select at least one entity to export from %s view.", sheetTitle);
            } else if (selectedEntitiesIds.stream().anyMatch(Objects::isNull)) {
                throw failuref("Export of selected entities from %s view is not supported due to missing IDs.", sheetTitle);
            }
            return selectionCrit.export(linkedMapOf(t2("ids", selectedEntitiesIds.toArray(new Long[0]))));
        }
    }

}
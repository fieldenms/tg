package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity_centre.review.criteria.DynamicColumnForExport;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.file_reports.WorkbookExporter;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.interfaces.IEntityMasterUrlProvider;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.poi.ss.util.WorkbookUtil.validateSheetName;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getDefaultEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;

/// DAO implementation for companion object [EntityExportActionCo].
///
@EntityType(EntityExportAction.class)
public class EntityExportActionDao extends CommonEntityDao<EntityExportAction> implements EntityExportActionCo {
    public static final String ERR_EMPTY_SELECTION_FOR_EXPORT_OF_SELECTED = "Please select at least one entity to export from %s view.";
    public static final String ERR_MISSING_IDS_FOR_EXPORT_OF_SELECTED = "Export of selected entities from %s view is not supported due to missing IDs.";
    public static final String ERR_EXCEPTION_DURING_DATA_EXPORT = "An exception occurred during the data export.";
    public static final String ERR_NOTHING_TO_EXPORT = "There is nothing to export.";

    public static final String EXPORT_FILE_NAME_TEMPLATE = "export-of-%s.xlsx";
    public static final String EXPORT_FILE_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ICriteriaEntityRestorer criteriaEntityRestorer;
    private final IEntityMasterUrlProvider entityMasterUrlProvider;

    @Inject
    public EntityExportActionDao(
            final ICriteriaEntityRestorer criteriaEntityRestorer,
            final IEntityMasterUrlProvider entityMasterUrlProvider)
    {
        this.criteriaEntityRestorer = criteriaEntityRestorer;
        this.entityMasterUrlProvider = entityMasterUrlProvider;
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
        final CentreContextHolder topCentreContextHolder = getParentCentreContextHolder(entity.getCentreContextHolder());
        final List<Stream<AbstractEntity<?>>> entities = new ArrayList<>();
        final List<Pair<String[], String[]>> propAndTitles = new ArrayList<>();
        final List<List<List<DynamicColumnForExport>>> dynamicProperties = new ArrayList<>();
        final List<String> titles = new ArrayList<>();
        // selectionCrit.getDynamicProperties() are used only for EntityExportAction and only in this class;
        //   they are initialised in below selectionCrit.export(...) calls; see selectionCrit.setDynamicProperties method callers for more details;
        //   that's why there is no need to initialise selectionCrit.getDynamicProperties() anywhere outside EntityExportAction, i.e. for other functional actions.
        // Generate export data
        final String entityTypeName = generateExportData(entity, topCentreContextHolder, false, entities, titles, propAndTitles, dynamicProperties);

        if (entities.isEmpty()) {
            throw failure(ERR_NOTHING_TO_EXPORT);
        }

        entity.setFileName(EXPORT_FILE_NAME_TEMPLATE.formatted(entityTypeName));
        entity.setMime(EXPORT_FILE_MIME);

        try {
            entity.setData(WorkbookExporter.convertToByteArray(WorkbookExporter.export(entities, propAndTitles, dynamicProperties, titles, entityMasterUrlProvider)));
        } catch (final IOException ex) {
            throw failure(ERR_EXCEPTION_DURING_DATA_EXPORT, ex);
        } finally {
            entities.forEach(Stream::close);
        }

        return entity;
    }

    private String generateExportData(
            final EntityExportAction entity,
            final CentreContextHolder contextEntry,
            final boolean allowEmptySelectionForExportOfSelected,
            final List<Stream<AbstractEntity<?>>> entities,
            final List<String> titles,
            final List<Pair<String[], String[]>> propAndTitles,
            final List<List<List<DynamicColumnForExport>>> dynamicProperties)
    {
        final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit = criteriaEntityRestorer.restoreCriteriaEntity(contextEntry);
        final Object resultSetHidden = contextEntry.getCustomObject().get("@@resultSetHidden");
        if (resultSetHidden != null && !Boolean.parseBoolean(resultSetHidden.toString())) {
            final String sheetTitle = extractSheetTitle(selectionCrit);
            titles.add(sheetTitle);
            entities.add(exportEntities(entity, selectionCrit, sheetTitle, allowEmptySelectionForExportOfSelected));
            propAndTitles.add(selectionCrit.generatePropTitlesToExport());
            dynamicProperties.add(selectionCrit.getDynamicProperties());
        }
        if (!contextEntry.proxiedPropertyNames().contains("relatedContexts")) {
            contextEntry.getRelatedContexts().forEach((key, relatedContext) -> generateExportData(entity, relatedContext, true, entities, titles, propAndTitles, dynamicProperties));
        }
        return selectionCrit.getEntityClass().getSimpleName();
    }

    private CentreContextHolder getParentCentreContextHolder(final CentreContextHolder centreContextHolder) {
        if (!centreContextHolder.proxiedPropertyNames().contains("parentCentreContext") && centreContextHolder.getParentCentreContext() != null) {
            return getParentCentreContextHolder(centreContextHolder.getParentCentreContext());
        }
        return centreContextHolder;
    }

    private String extractSheetTitle(final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) {
        final CentreContextHolder centreContextHolder = selectionCrit.centreContextHolder();
        final String sheetTitle = (String)centreContextHolder.getCustomObject().get("@@insertionPointTitle");
        final String title = isEmpty(sheetTitle) ? getEntityTitleAndDesc(selectionCrit.getEntityClass()).getKey() : sheetTitle;
        try {
            validateSheetName(title);
        } catch (final Exception e) {
            return getDefaultEntityTitleAndDesc(selectionCrit.getEntityClass()).getKey();
        }
        return title;
    }

    private Stream<AbstractEntity<?>> exportEntities(
            final EntityExportAction entity,
            final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit,
            final String sheetTitle,
            final boolean allowNoSelectionForExportOfSelected)
    {
        if (entity.isExportAll()) {
            return selectionCrit.export(new LinkedHashMap<>());
        }
        else if (entity.isExportTop()) {
            return selectionCrit.export(linkedMapOf(t2("fetchSize", entity.getNumber()))).limit(entity.getNumber());
        }
        // export selected items
        else {
            final List<Long> selectedEntitiesIds = selectionCrit.centreContextHolder().getSelectedEntities().stream().map(AbstractEntity::getId).toList();
            if (selectedEntitiesIds.isEmpty()) {
                if (allowNoSelectionForExportOfSelected) {
                    return Stream.empty();
                }
                throw failuref(ERR_EMPTY_SELECTION_FOR_EXPORT_OF_SELECTED, sheetTitle);
            } else if (selectedEntitiesIds.stream().anyMatch(Objects::isNull)) {
                throw failuref(ERR_MISSING_IDS_FOR_EXPORT_OF_SELECTED, sheetTitle);
            }
            return selectionCrit.export(linkedMapOf(t2("ids", selectedEntitiesIds.toArray(new Long[0]))));
        }
    }

}
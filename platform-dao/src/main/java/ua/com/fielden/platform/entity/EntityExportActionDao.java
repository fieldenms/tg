package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity_centre.review.criteria.DynamicColumnForExport;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.file_reports.WorkbookExporter;
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
        final Map<String, Object> adhocParams = new LinkedHashMap<>();
        final List<Stream<AbstractEntity<?>>> entities = new ArrayList<>();
        final List<Pair<String[], String[]>> propAndTitles = new ArrayList<>();
        final List<List<List<DynamicColumnForExport>>> dynamicProperties = new ArrayList<>();
        // selectionCrit.getDynamicProperties() are used only for EntityExportAction and only in this class;
        //   they are initialised in below selectionCrit.export(...) calls; see selectionCrit.setDynamicProperties method callers for more details;
        //   that's why there is no need to initialise selectionCrit.getDynamicProperties() anywhere outside EntityExportAction, i.e. for other functional actions.
        entities.add(exportEntities(entity, selectionCrit));
        propAndTitles.add(selectionCrit.generatePropTitlesToExport());
        dynamicProperties.add(selectionCrit.getDynamicProperties());
        entity.getCentreContextHolder().getRelatedContexts().entrySet().forEach(contextEntry -> {
            final EnhancedCentreEntityQueryCriteria<?, ?> relatedSelectionCrit = criteriaEntityRestorer.restoreCriteriaEntity(contextEntry.getValue());
            entities.add(exportEntities(entity, relatedSelectionCrit));
            propAndTitles.add(relatedSelectionCrit.generatePropTitlesToExport());
            dynamicProperties.add(relatedSelectionCrit.getDynamicProperties());
        });

        try {
            entity.setData(WorkbookExporter.convertToByteArray(WorkbookExporter.export(entities, propAndTitles, dynamicProperties)));
        } catch (final IOException e) {
            throw failure("An exception occurred during the data export.", e);
        } finally {
            entities.forEach(entitiesStream -> entitiesStream.close());
        }

        return entity;
    }

    private Stream<AbstractEntity<?>> exportEntities(final EntityExportAction entity, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) {
        if (entity.isExportAll()) {
            return selectionCrit.export(new LinkedHashMap<>());
        } else if (entity.isExportTop()) {
            return selectionCrit.export(linkedMapOf(t2("fetchSize", entity.getNumber()))).limit(entity.getNumber());
        } else {
            if (entity.getSelectedEntityIds().isEmpty()) {
                throw failure("Please select at least one entry to export.");
            } else if (entity.getSelectedEntityIds().stream().anyMatch(Objects::isNull)) {
                throw failure("Export of selected entities is not supported due to missing IDs.");
            }
            return selectionCrit.export(linkedMapOf(t2("ids", entity.getSelectedEntityIds().toArray(new Long[0]))));
        }
    }

}
package ua.com.fielden.platform.entity;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.file_reports.WorkbookExporter;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/**
 * DAO implementation for companion object {@link IEntityExportAction}.
 *
 * @author TG Team
 *
 */
@EntityType(EntityExportAction.class)
public class EntityExportActionDao extends CommonEntityDao<EntityExportAction> implements IEntityExportAction {
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
        
        entity.setFileName(String.format("export-of-%s.xls", selectionCrit.getEntityClass().getSimpleName()));
        entity.setMime("application/vnd.ms-excel");
        final Map<String, Object> adhocParams = new LinkedHashMap<>();
        final Stream<AbstractEntity<?>> entities;
        if (entity.isExportAll()) {
            entities = selectionCrit.export(adhocParams);
        } else if (entity.isExportTop()) {
            adhocParams.put("fetchSize", entity.getNumber());
            entities = selectionCrit.export(adhocParams).limit(entity.getNumber());
        } else {
            if (entity.getSelectedEntityIds().isEmpty()) {
                throw Result.failure("Please select at least one entry to export.");
            }
            adhocParams.put("ids", entity.getSelectedEntityIds().toArray(new Long[0]));
            entities = selectionCrit.export(adhocParams);
        }
        try {
            final Pair<String[], String[]> propAndTitles = selectionCrit.generatePropTitlesToExport();
            entity.setData(WorkbookExporter.convertToByteArray(WorkbookExporter.export(entities, propAndTitles.getKey(), propAndTitles.getValue())));
        } catch (final IOException e) {
            throw Result.failure("An exception occurred during the data export.", e);
        } finally {
            entities.close();
        }

        return entity;
    }

}
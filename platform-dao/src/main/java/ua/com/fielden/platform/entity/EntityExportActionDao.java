package ua.com.fielden.platform.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

/**
 * DAO implementation for companion object {@link IEntityExportAction}.
 *
 * @author TG Team
 *
 */
@EntityType(EntityExportAction.class)
public class EntityExportActionDao extends CommonEntityDao<EntityExportAction> implements IEntityExportAction {
    
    @Inject
    public EntityExportActionDao(final IFilter filter) {
        super(filter);
    }

    @Override
    public EntityExportAction new_() {
        final EntityExportAction entity = super.new_();
        entity.setKey("Export");
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
        //Otherwise continue data exporting.
        final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit = entity.getContext().getSelectionCrit();
        entity.setFileName(String.format("export-of-%s.xls", selectionCrit.getEntityClass().getSimpleName()));
        entity.setMime("application/vnd.ms-excel");
        final Map<String, Object> customObject = new LinkedHashMap<>();
        final Stream<AbstractEntity<?>> entities;
        if (entity.isExportAll()) {
            entities = selectionCrit.exportQueryRunner().apply(customObject);
        } else if (entity.isExportTop()) {
            entities = selectionCrit.exportQueryRunner().apply(customObject).limit(entity.getNumber());
        } else {
            if (entity.getContext().getSelectedEntities().isEmpty()) {
                throw Result.failure("Please select at least one entry to export.");
            }
            final Set<Long> ids = new HashSet<>();
            for (final AbstractEntity<?> selectedEntity : entity.getContext().getSelectedEntities()) {
                ids.add(selectedEntity.getId());
            }
            entities = selectionCrit.exportQueryRunner().apply(customObject).filter(ent -> ids.contains(ent.getId()));
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
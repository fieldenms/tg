package ua.com.fielden.platform.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.file_reports.WorkbookExporter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link IEntityExportAction}.
 *
 * @author Developers
 *
 */
@EntityType(EntityExportAction.class)
public class EntityExportActionDao extends CommonEntityDao<EntityExportAction> implements IEntityExportAction {
    @Inject
    public EntityExportActionDao(final IFilter filter) {
        super(filter);
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
        final Map<String, Object> customObject = new LinkedHashMap<String, Object>();
        final Set<AbstractEntity<?>> entities;
        if (entity.getAll()) {
            customObject.put("@@pageNumber", -1);
            customObject.put("@@action", "export all");
            entities = new LinkedHashSet<>(selectionCrit.exportQueryRunner().apply(customObject));
        } else if (entity.getPageRange()) {
            entities = new LinkedHashSet<>();
            for (int page = entity.getFromPage() - 1; page < entity.getToPage(); page++) {
                customObject.put("@@pageCapacity", entity.getPageCapacity());
                customObject.put("@@action", "navigate");
                customObject.put("@@pageNumber", page);
                entities.addAll(selectionCrit.exportQueryRunner().apply(customObject));
            }
        } else {
            if (entity.getContext().getSelectedEntities().isEmpty()) {
                throw Result.failure("Please select at least one entity to export");
            }
            customObject.put("@@pageNumber", -1);
            customObject.put("@@action", "export all");
            final List<Long> ids = new ArrayList<>();
            for (final AbstractEntity<?> selectedEntity : entity.getContext().getSelectedEntities()) {
                ids.add(selectedEntity.getId());
            }
            entities = new LinkedHashSet<>(selectEntities(selectionCrit.exportQueryRunner().apply(customObject), ids));
        }
        try {
            final Pair<String[], String[]> propAndTitles = selectionCrit.generatePropTitlesToExport();
            entity.setData(WorkbookExporter.convertToByteArray(WorkbookExporter.export(new ArrayList<>(entities), propAndTitles.getKey(), propAndTitles.getValue())));
        } catch (final IOException e) {
            throw Result.failure("Could not export data.", e);
        }

        return entity;
    }

    /**
     * Selects the entities from resulting <code>data</code> to contain only those, that have specified <code>longIds</code>.
     *
     * @param data
     * @param ids
     * @return
     */
    private List<AbstractEntity<?>> selectEntities(final List<AbstractEntity<?>> data, final List<Long> longIds) {
        final List<AbstractEntity<?>> list = new ArrayList<>();
        if (longIds.isEmpty()) {
            return list;
        } else {
            for (final AbstractEntity<?> retrievedEntity : data) {
                if (longIds.contains(retrievedEntity.getId())) {
                    list.add(retrievedEntity);
                }
            }
        }
        return list;
    }
}
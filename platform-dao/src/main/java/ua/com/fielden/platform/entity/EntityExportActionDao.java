package ua.com.fielden.platform.entity;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.poi.ss.util.WorkbookUtil.validateSheetName;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getDefaultEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.security.tokens.Template.READ;
import static ua.com.fielden.platform.security.tokens.TokenUtils.authoriseReading;
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
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
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
    private final IAuthorisationModel authorisationModel;
    private final ISecurityTokenProvider securityTokenProvider;

    @Inject
    public EntityExportActionDao(final IFilter filter, final ICriteriaEntityRestorer criteriaEntityRestorer, final IAuthorisationModel authorisationModel, final ISecurityTokenProvider securityTokenProvider) {
        super(filter);
        this.criteriaEntityRestorer = criteriaEntityRestorer;
        this.authorisationModel = authorisationModel;
        this.securityTokenProvider = securityTokenProvider;
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
        //Generate export data
        final String entityTypeName = generateExportData(entity, topCentreContextHolder, entities, titles, propAndTitles, dynamicProperties);

        if (entities.isEmpty()) {
            throw failure("There is nothing to export");
        }

        entity.setFileName(String.format("export-of-%s.xlsx", entityTypeName));
        entity.setMime("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        try {
            entity.setData(WorkbookExporter.convertToByteArray(WorkbookExporter.export(entities, propAndTitles, dynamicProperties, titles)));
        } catch (final IOException e) {
            throw failure("An exception occurred during the data export.", e);
        } finally {
            entities.forEach(entitiesStream -> entitiesStream.close());
        }

        return entity;
    }

    private String generateExportData(final EntityExportAction entity, final CentreContextHolder contextEntry, final List<Stream<AbstractEntity<?>>> entities, final List<String> titles, final List<Pair<String[], String[]>> propAndTitles, final List<List<List<DynamicColumnForExport>>> dynamicProperties) {
        final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit = criteriaEntityRestorer.restoreCriteriaEntity(contextEntry);
        final Object resultSetHiden = contextEntry.getCustomObject().get("@@resultSetHidden");
        if (resultSetHiden != null && !Boolean.valueOf(resultSetHiden.toString())) {
            final String sheetTitle = extractSheetTitle(selectionCrit);
            titles.add(sheetTitle);
            entities.add(exportEntities(entity, selectionCrit, sheetTitle));
            propAndTitles.add(selectionCrit.generatePropTitlesToExport());
            dynamicProperties.add(selectionCrit.getDynamicProperties());
        }
        if (!contextEntry.proxiedPropertyNames().contains("relatedContexts")) {
            contextEntry.getRelatedContexts().entrySet().forEach(relatedContextEntry -> {
                generateExportData(entity, relatedContextEntry.getValue(), entities, titles, propAndTitles, dynamicProperties);
            });
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

    private Stream<AbstractEntity<?>> exportEntities(final EntityExportAction entity, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit, final String sheetTitle) {
        authoriseReading(selectionCrit.getEntityClass().getSimpleName(), READ, authorisationModel, securityTokenProvider).ifFailure(Result::throwRuntime); // reading of entities should be authorised when exporting
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
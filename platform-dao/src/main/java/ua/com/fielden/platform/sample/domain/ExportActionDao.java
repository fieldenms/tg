package ua.com.fielden.platform.sample.domain;

import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.file_reports.WorkbookExporter.convertToByteArray;

import java.util.List;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.file_reports.WorkbookExporter;
import ua.com.fielden.platform.web.interfaces.IEntityMasterUnifiedResourceLocator;

/**
 * DAO implementation for companion object {@link IExportAction}.
 *
 * @author Developers
 *
 */
@EntityType(ExportAction.class)
public class ExportActionDao extends CommonEntityDao<ExportAction> implements IExportAction {

    private final ITgPersistentEntityWithProperties co;
    private final IEntityMasterUnifiedResourceLocator uriGenerator;

    @Inject
    public ExportActionDao(
            final ITgPersistentEntityWithProperties co,
            final IFilter filter,
            final IEntityMasterUnifiedResourceLocator uriGenerator) {
        super(filter);
        this.co = co;
        this.uriGenerator = uriGenerator;
    }

    @Override
    @SessionRequired
    public ExportAction save(final ExportAction entity) {
        final EntityResultQueryModel<TgPersistentEntityWithProperties> query = select(TgPersistentEntityWithProperties.class).model();
        final QueryExecutionModel<TgPersistentEntityWithProperties, EntityResultQueryModel<TgPersistentEntityWithProperties>> qem = from(query).with(fetchAll(TgPersistentEntityWithProperties.class)).model();

        final List<TgPersistentEntityWithProperties> entitiesToExport = co.firstPage(qem, entity.getCount()).data();

        try {
            entity.setFileName("export-of-TgPersistentEntityWithProperties.xlsx");
            entity.setMime("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            final byte[] data = convertToByteArray(WorkbookExporter.export(entitiesToExport.stream(), new String[] {"key", "desc"}, new String[] {"key", "desc"}, ofNullable(uriGenerator)));
            entity.setData(data);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return entity;
    }

}
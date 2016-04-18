package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.io.IOException;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/** 
 * DAO implementation for companion object {@link IExportAction}.
 * 
 * @author Developers
 *
 */
@EntityType(ExportAction.class)
public class ExportActionDao extends CommonEntityDao<ExportAction> implements IExportAction {
    
    private final ITgPersistentEntityWithProperties co;
    
    @Inject
    public ExportActionDao(
            final ITgPersistentEntityWithProperties co,
            final IFilter filter) {
        super(filter);
        this.co = co;
    }
    
    @Override
    @SessionRequired
    public ExportAction save(final ExportAction entity) {
        final EntityResultQueryModel<TgPersistentEntityWithProperties> query = select(TgPersistentEntityWithProperties.class).model();
        final QueryExecutionModel<TgPersistentEntityWithProperties, EntityResultQueryModel<TgPersistentEntityWithProperties>> qem = from(query).with(fetchAll(TgPersistentEntityWithProperties.class)).model();

        //co.firstPage(qem, entity.getCount());

        try {
            String html = "<html><body>HELLO EXPORTED!</body></html>";
            byte[] data = html.getBytes(); //co.export(qem, new String[] {"key", "desc"}, new String[] {"key", "desc"});
            entity.setData(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return entity;
    }

}
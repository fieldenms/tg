package ua.com.fielden.platform.swing.schedule;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;

public final class MultipleTypeScheduleEntity implements IScheduleEntity<AbstractEntity<?>> {

    private Map<Class<? extends AbstractEntity<?>>, IScheduleEntity<? extends AbstractEntity<?>>> entityMap;

    public MultipleTypeScheduleEntity() {
        entityMap = new HashMap<>();
    }

    public <T extends AbstractEntity<?>> MultipleTypeScheduleEntity setScheduleSeries(final Class<T> type, final IScheduleEntity<T> scheduleEntity) {
        entityMap.put(type, scheduleEntity);
        return this;
    }

    @Override
    public Date getFrom(final AbstractEntity<?> entity) {
        final IScheduleEntity<AbstractEntity<?>> scheduleEntity = getScheduleEntity(DynamicEntityClassLoader.getOriginalType(entity.getType()));
        if (scheduleEntity != null) {
            return scheduleEntity.getFrom(entity);
        }
        return null;
    }

    @Override
    public Date getTo(final AbstractEntity<?> entity) {
        final IScheduleEntity<AbstractEntity<?>> scheduleEntity = getScheduleEntity(DynamicEntityClassLoader.getOriginalType(entity.getType()));
        if (scheduleEntity != null) {
            return scheduleEntity.getTo(entity);
        }
        return null;
    }

    @Override
    public void setFrom(final AbstractEntity<?> entity, final Date fromDate) {
        final IScheduleEntity<AbstractEntity<?>> scheduleEntity = getScheduleEntity(DynamicEntityClassLoader.getOriginalType(entity.getType()));
        if (scheduleEntity != null) {
            scheduleEntity.setFrom(entity, fromDate);
        }
    }

    @Override
    public void setTo(final AbstractEntity<?> entity, final Date toDate) {
        final IScheduleEntity<AbstractEntity<?>> scheduleEntity = getScheduleEntity(DynamicEntityClassLoader.getOriginalType(entity.getType()));
        if (scheduleEntity != null) {
            scheduleEntity.setTo(entity, toDate);
        }
    }

    @Override
    public boolean canEditEntity(final AbstractEntity<?> entity) {
        final IScheduleEntity<AbstractEntity<?>> scheduleEntity = getScheduleEntity(DynamicEntityClassLoader.getOriginalType(entity.getType()));
        if (scheduleEntity != null) {
            return scheduleEntity.canEditEntity(entity);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private IScheduleEntity<AbstractEntity<?>> getScheduleEntity(final Class<?> type) {
        return ((IScheduleEntity<AbstractEntity<?>>) entityMap.get(DynamicEntityClassLoader.getOriginalType(type)));
    }

}

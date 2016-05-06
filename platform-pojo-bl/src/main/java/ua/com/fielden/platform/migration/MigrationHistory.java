package ua.com.fielden.platform.migration;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.migration.controller.IMigrationHistory;

@KeyType(DynamicEntityKey.class)
@KeyTitle("Migration History")
@MapEntityTo("MIGRATION_HISTORY")
@CompanionObject(IMigrationHistory.class)
public class MigrationHistory extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @CompositeKeyMember(1)
    @MapTo("ID_MIGRATION_RUN")
    @Title("Migration")
    private MigrationRun migrationRun;
    @IsProperty
    @CompositeKeyMember(2)
    @MapTo("RETRIEVER_NAME")
    @Title("Retriever")
    private String retrieverTypeName;
    @IsProperty
    @CompositeKeyMember(3)
    @MapTo("THREAD_NAME")
    @Title("Thread")
    private String threadName;
    @IsProperty
    @MapTo("ENTITY_TYPE_NAME")
    @Title("Entity type")
    private String entityTypeName;
    @IsProperty
    @MapTo("RETRIEVED_COUNT")
    @Title("Number of retrieved records")
    private Integer retrievedCount;
    @IsProperty
    @MapTo("INSERTED_COUNT")
    @Title("Number of inserted records")
    private Integer insertedCount;
    @IsProperty
    @MapTo("UPDATED_COUNT")
    @Title("Number of updated records")
    private Integer updatedCount;
    @IsProperty
    @MapTo("SKIPPED_COUNT")
    @Title("Number of skipped records")
    private Integer skippedCount;
    @IsProperty
    @MapTo("FAILED_COUNT")
    @Title("Number of failed records")
    private Integer failedCount;
    @IsProperty
    @MapTo("STARTED")
    @Title("Started")
    private Date started;
    @IsProperty
    @MapTo("FINISHED")
    @Title("Finished")
    private Date finished;

    /**
     * Constructor for the entity factory from TG.
     */
    protected MigrationHistory() {
        setKey(new DynamicEntityKey(this));
    }

    public Integer getRetrievedCount() {
        return retrievedCount;
    }

    public Integer getInsertedCount() {
        return insertedCount;
    }

    public String getEntityTypeName() {
        return entityTypeName;
    }

    public Integer getUpdatedCount() {
        return updatedCount;
    }

    public MigrationRun getMigrationRun() {
        return migrationRun;
    }

    public String getRetrieverTypeName() {
        return retrieverTypeName;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public Date getStarted() {
        return started;
    }

    public Date getFinished() {
        return finished;
    }

    public String getThreadName() {
        return threadName;
    }

    public Integer getSkippedCount() {
        return skippedCount;
    }

    @Observable
    public void setRetrievedCount(final Integer retrievedCount) {
        this.retrievedCount = retrievedCount;
    }

    @Observable
    public void setInsertedCount(final Integer insertedCount) {
        this.insertedCount = insertedCount;
    }

    @Observable
    public void setEntityTypeName(final String entityTypeName) {
        this.entityTypeName = entityTypeName;
    }

    @Observable
    public void setUpdatedCount(final Integer updatedCount) {
        this.updatedCount = updatedCount;
    }

    @Observable
    public void setMigrationRun(final MigrationRun migrationRun) {
        this.migrationRun = migrationRun;
    }

    @Observable
    public void setRetrieverTypeName(final String retrieverTypeName) {
        this.retrieverTypeName = retrieverTypeName;
    }

    @Observable
    public void setFailedCount(final Integer failedCount) {
        this.failedCount = failedCount;
    }

    @Observable
    public void setStarted(final Date started) {
        this.started = started;
    }

    @Observable
    public void setFinished(final Date finished) {
        this.finished = finished;
    }

    @Observable
    public void setThreadName(final String threadName) {
        this.threadName = threadName;
    }

    @Observable
    public void setSkippedCount(final Integer skippedCount) {
        this.skippedCount = skippedCount;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(getRetrieverTypeName());
        sb.append(":\n   entityType = ");
        sb.append(getEntityTypeName());
        sb.append(":\n   duration = ");
        sb.append(getFinished() != null && getStarted() != null ? getDurationFromMilliseconds(getFinished().getTime() - getStarted().getTime()) : " ?");
        sb.append(":\n   retrieved = ");
        sb.append(retrievedCount);
        sb.append(" inserted = ");
        sb.append(insertedCount);
        sb.append(" updated = ");
        sb.append(updatedCount);
        sb.append(" failed = ");
        sb.append(failedCount);
        return sb.toString();
    }

    String getDurationFromMilliseconds(final Long durationInMilliseconds) {
        final Long millisecsInSecond = 1000l;
        final Long millisecsInMinute = 60 * millisecsInSecond;
        final Long millisecsInHour = 60 * millisecsInMinute;

        final Long hours = durationInMilliseconds / millisecsInHour;
        final Long minutes = (durationInMilliseconds - (hours * millisecsInHour)) / millisecsInMinute;
        final Long seconds = (durationInMilliseconds - (hours * millisecsInHour) - (minutes * millisecsInMinute)) / millisecsInSecond;
        final Long mseconds = durationInMilliseconds - hours * millisecsInHour - minutes * millisecsInMinute - seconds * millisecsInSecond;

        return hours + " h " + minutes + " m " + seconds + " s " + mseconds + " ms";
    }
}

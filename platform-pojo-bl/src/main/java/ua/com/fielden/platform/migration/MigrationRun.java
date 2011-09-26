package ua.com.fielden.platform.migration;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@KeyTitle(value="Migration run", desc="Migration run")
@MapEntityTo("MIGRATION_RUN")
public class MigrationRun extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

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
    protected MigrationRun() {
    }

    /**
     * Convenience constructor.
     *
     * @param key
     * @param desc
     */
    public MigrationRun(final String key, final String desc) {
	super(null, key, desc);
    }

    public Date getStarted() {
        return started;
    }

    public Date getFinished() {
        return finished;
    }

    @Observable
    public void setStarted(final Date started) {
        this.started = started;
    }
    @Observable
    public void setFinished(final Date finished) {
        this.finished = finished;
    }
}

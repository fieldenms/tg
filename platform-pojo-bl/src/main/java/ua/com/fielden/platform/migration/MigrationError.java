package ua.com.fielden.platform.migration;

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
import ua.com.fielden.platform.migration.controller.IMigrationError;

@KeyType(DynamicEntityKey.class)
@KeyTitle("Migration Error")
@MapEntityTo("MIGRATION_ERROR")
@CompanionObject(IMigrationError.class)
public class MigrationError extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @CompositeKeyMember(1)
    @MapTo("ID_MIGRATION_HISTORY")
    @Title("Migration History")
    private MigrationHistory migrationHistory;
    @IsProperty
    @CompositeKeyMember(2)
    @Title("Error No")
    @MapTo("ERROR_NUMBER")
    private Integer errorNo;
    @IsProperty
    @MapTo("RAW_DATA")
    @Title("Raw Data")
    private String rawData;
    @IsProperty
    @MapTo("ERROR_TYPE")
    @Title("Error Type")
    private String errorType;

    @IsProperty
    @MapTo("ERROR_MESSAGE")
    @Title("Error Message")
    private String errorText;
    @IsProperty
    @MapTo("ERROR_PRECAUSE")
    @Title("Error Pre-Cause")
    private String errorPreCause;
    @IsProperty
    @MapTo("ERROR_PROP_NAME")
    @Title("Property in error (name)")
    private String errorPropName;
    @IsProperty
    @MapTo("ERROR_PROP_TYPE")
    @Title("Property in error (type)")
    private String errorPropType;
    @IsProperty
    @MapTo("ERROR_PROP_VALUE")
    @Title("Property in error (value)")
    private String errorPropValue;

    /**
     * Constructor for the entity factory from TG.
     */
    protected MigrationError() {
        setKey(new DynamicEntityKey(this));
    }

    public MigrationHistory getMigrationHistory() {
        return migrationHistory;
    }

    public String getErrorText() {
        return errorText;
    }

    public Integer getErrorNo() {
        return errorNo;
    }

    public String getRawData() {
        return rawData;
    }

    public String getErrorPreCause() {
        return errorPreCause;
    }

    public String getErrorPropName() {
        return errorPropName;
    }

    public String getErrorPropType() {
        return errorPropType;
    }

    public String getErrorPropValue() {
        return errorPropValue;
    }

    public String getErrorType() {
        return errorType;
    }

    @Observable
    public void setMigrationHistory(final MigrationHistory migrationHistory) {
        this.migrationHistory = migrationHistory;
    }

    @Observable
    public void setErrorText(final String errorText) {
        this.errorText = errorText;
    }

    @Observable
    public void setErrorNo(final Integer errorNo) {
        this.errorNo = errorNo;
    }

    @Observable
    public void setRawData(final String rawData) {
        this.rawData = rawData;
    }

    @Observable
    public void setErrorPreCause(final String errorPreCause) {
        this.errorPreCause = errorPreCause;
    }

    @Observable
    public void setErrorPropName(final String errorPropName) {
        this.errorPropName = errorPropName;
    }

    @Observable
    public void setErrorPropType(final String errorPropType) {
        this.errorPropType = errorPropType;
    }

    @Observable
    public void setErrorPropValue(final String errorPropValue) {
        this.errorPropValue = errorPropValue;
    }

    @Observable
    public void setErrorType(final String errorType) {
        this.errorType = errorType;
    }
}

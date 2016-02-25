package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;

@KeyTitle(value = "Driver import dataset", desc = "Driver import dataset description")
// @CompanionObject(IDriverImportDataset.class)
@MapEntityTo
@KeyType(String.class)
public class TgDefinersExecutorCompositeKeyMember extends AbstractEntity<String> {
    private static final long serialVersionUID = 4997515237852204508L;

    @IsProperty
    @MapTo
    @Title(value = "Title", desc = "Desc")
    @AfterChange(TgDefinersExecutorCompositeKeyMemberHandler.class)
    private String propWithHandler;

    @Observable
    public TgDefinersExecutorCompositeKeyMember setPropWithHandler(final String propWithHandler) {
        this.propWithHandler = propWithHandler;
        return this;
    }

    public String getPropWithHandler() {
        return propWithHandler;
    }
}
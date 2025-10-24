package ua.com.fielden.platform.tiny;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

@EntityTitle("Tiny Hyperlink")
@MapEntityTo
@KeyType(DynamicEntityKey.class)
@CompanionObject(TinyHyperlinkCo.class)
public class TinyHyperlink extends AbstractPersistentEntity<DynamicEntityKey> {

    public static final String ENTITY_TITLE = getEntityTitleAndDesc(TinyHyperlink.class).getKey();
    public static final String ENTITY_DESC = getEntityTitleAndDesc(TinyHyperlink.class).getValue();

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private String entityTypeName;

    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    private Long entityId;

    public String getEntityTypeName() {
        return entityTypeName;
    }

    @Observable
    public TinyHyperlink setEntityTypeName(final String entityTypeName) {
        this.entityTypeName = entityTypeName;
        return this;
    }

    public Long getEntityId() {
        return entityId;
    }

    @Observable
    public TinyHyperlink setEntityId(final Long entityId) {
        this.entityId = entityId;
        return this;
    }

}

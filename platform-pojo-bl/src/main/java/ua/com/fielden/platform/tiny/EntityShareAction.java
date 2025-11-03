package ua.com.fielden.platform.tiny;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.types.Hyperlink;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

@EntityTitle("Share Entity")
@KeyType(NoKey.class)
@CompanionObject(EntityShareActionCo.class)
public class EntityShareAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    public static final String HYPERLINK = "hyperlink";

    protected EntityShareAction() {
        setKey(NO_KEY);
    }

    @IsProperty
    @Readonly
    @Title(value = "Hyperlink", desc = "A hyperlink to open the shared entity.")
    private Hyperlink hyperlink;

    public Hyperlink getHyperlink() {
        return hyperlink;
    }

    @Observable
    public EntityShareAction setHyperlink(final Hyperlink hyperlink) {
        this.hyperlink = hyperlink;
        return this;
    }

}

package ua.com.fielden.platform.tiny;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.types.Hyperlink;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

@EntityTitle("Share")
@KeyType(NoKey.class)
@CompanionObject(ShareActionCo.class)
public class ShareAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    public static final String
            HYPERLINK = "hyperlink",
            QR_CODE = "qrCode";

    protected ShareAction() {
        setKey(NO_KEY);
    }

    @IsProperty
    @Readonly
    @Title(value = "Hyperlink", desc = "A hyperlink to the shared resource.")
    private Hyperlink hyperlink;

    @IsProperty
    @Readonly
    @Title(value = "QR Code", desc = "QR Code for the hyperlink (Base64).")
    private String qrCode;

    public String getQrCode() {
        return qrCode;
    }

    @Observable
    public ShareAction setQrCode(final String qrCode) {
        this.qrCode = qrCode;
        return this;
    }

    public Hyperlink getHyperlink() {
        return hyperlink;
    }

    @Observable
    public ShareAction setHyperlink(final Hyperlink hyperlink) {
        this.hyperlink = hyperlink;
        return this;
    }

}

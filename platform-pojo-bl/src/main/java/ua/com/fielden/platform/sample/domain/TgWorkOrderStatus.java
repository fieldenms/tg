package ua.com.fielden.platform.sample.domain;

public enum TgWorkOrderStatus {
    E("Entered"), A("Active"), F("Finished"), C("Closed"), X("Cancelled");

    private final String desc;

    TgWorkOrderStatus(final String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return desc;
    }

}

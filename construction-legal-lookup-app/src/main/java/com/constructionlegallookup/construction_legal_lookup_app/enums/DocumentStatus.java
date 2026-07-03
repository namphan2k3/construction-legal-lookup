package com.constructionlegallookup.construction_legal_lookup_app.enums;

public enum DocumentStatus {
    CON_HIEU_LUC("Còn hiệu lực"),
    HET_HIEU_LUC("Hết hiệu lực"),
    CHUA_CO_HIEU_LUC("Chưa có hiệu lực"),
    HET_HIEU_LUC_MOT_PHAN("Hết hiệu lực một phần");

    private final String displayName;

    private DocumentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

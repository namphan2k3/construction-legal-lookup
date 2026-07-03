package com.constructionlegallookup.construction_legal_lookup_app.enums;

public enum DocumentType {
    LUAT("Luật"),
    NGHI_DINH("Nghị định"),
    THONG_TU("Thông tư"),
    QUYET_DINH("Quyết định"),
    QCVN("QCVN"),
    TCVN("TCVN"),
    CONG_VAN("Công văn"),
    KHAC("Khác");

    private final String displayName;

    private DocumentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

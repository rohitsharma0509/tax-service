package com.scb.rider.tax.model.enums;

public enum FileType {
    CSV(".csv"),
    CHK(".chk");

    private String extension;

    private FileType(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}


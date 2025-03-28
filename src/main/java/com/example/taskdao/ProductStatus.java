package com.example.taskdao;

public enum ProductStatus {
    NEEDED("Нужен"),
    ENOUGH("Достаточно"),
    TOO_MUCH("Много");

    private final String displayName;

    ProductStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
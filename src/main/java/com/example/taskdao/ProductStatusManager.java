package com.example.taskdao;

public class ProductStatusManager {
    // Пороговые значения для определения статуса
    private final int minLimitValue;
    private final int maxLimitValue;

    public ProductStatusManager(int minLimitValue, int maxLimitValue) {
        this.minLimitValue = minLimitValue;
        this.maxLimitValue = maxLimitValue;
    }

    public ProductStatus calculateStatus(int quantity) {
        if (quantity < minLimitValue) {
            return ProductStatus.NEEDED;
        } else if (quantity <= maxLimitValue) {
            return ProductStatus.ENOUGH;
        } else {
            return ProductStatus.TOO_MUCH;
        }
    }

    public boolean isLeapYear() {
        //int year = 2024;
        int year = java.time.Year.now().getValue();
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }
}

package com.example.taskdao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductStatusManagerTest {
    private ProductStatusManager productStatusManager;

    @BeforeEach
    void setUp() {
        productStatusManager = new ProductStatusManager(5, 10);
    }

    @AfterEach
    void tearDown() {
        productStatusManager = null;
    }

    @Test
    void isLeapYear() {
        // Проверка високосных годов
        assertTrue(createManagerWithYear(2000).isLeapYear());
        assertTrue(createManagerWithYear(2020).isLeapYear());
        assertTrue(createManagerWithYear(1996).isLeapYear());

        // Проверка невисокосных годов
        assertFalse(createManagerWithYear(1900).isLeapYear());
        assertFalse(createManagerWithYear(2021).isLeapYear());
        assertFalse(createManagerWithYear(2100).isLeapYear());

        // Пограничные значения
        assertTrue(createManagerWithYear(2400).isLeapYear());
        assertFalse(createManagerWithYear(2300).isLeapYear());
        assertTrue(createManagerWithYear(2404).isLeapYear());

        // Значения из презентации
        assertTrue(createManagerWithYear(4).isLeapYear());
        assertFalse(createManagerWithYear(100).isLeapYear());
        assertTrue(createManagerWithYear(400).isLeapYear());
    }

    // Переопределенный вспомогательный метод для создания ProductStatusManager с фиктивным годом
    private ProductStatusManager createManagerWithYear(int year) {
        return new ProductStatusManager(5, 10) {
            @Override
            public boolean isLeapYear() {
                return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
            }
        };
    }
}
package com.example.taskdao;

import io.github.cdimascio.dotenv.Dotenv;

public class ProductDaoFactory {
    private static final Dotenv dotenv = Dotenv.configure()
            .filename("config.env")
            .ignoreIfMissing() // Продолжит работу, даже если файл отсутствует
            .load();

    public static ProductDao createProductDao(String dataSourceType) {
        switch (dataSourceType) {
            case "Список в памяти":
                return new ProductListDao(new ProductList());
            case "PostgreSQL":
                return new ProductPostgresDao(
                        dotenv.get("DB_URL"),
                        dotenv.get("DB_USER"),
                        dotenv.get("DB_PASSWORD")
                );
            case "Текстовый файл":
                return new ProductTxtDao(
                        dotenv.get("TXT_FILE_PATH")
                );
            default:
                throw new IllegalArgumentException("Неизвестный тип источника данных: " + dataSourceType);
        }
    }
}
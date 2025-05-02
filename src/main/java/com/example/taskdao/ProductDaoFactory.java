package com.example.taskdao;

import io.github.cdimascio.dotenv.Dotenv;

public class ProductDaoFactory {
    private static final Dotenv dotenv = Dotenv.configure()
            .filename("config.env")
            .ignoreIfMissing()
            .load();

    private static ProductDao memoryDao;
    private static ProductDao postgresDao;
    private static ProductDao txtDao;
    private static DataSynchronizer synchronizer;
    private static boolean firstTime = true;

    public static ProductDao createProductDao(String dataSourceType) {
        if (memoryDao == null) {
            memoryDao = new ProductListDao(new ProductList());
        }
        if (postgresDao == null) {
            postgresDao = new ProductPostgresDao(
                    dotenv.get("DB_URL"),
                    dotenv.get("DB_USER"),
                    dotenv.get("DB_PASSWORD"));
        }
        if (txtDao == null) {
            txtDao = new ProductTxtDao(dotenv.get("TXT_FILE_PATH"));
        }
        if (synchronizer == null) {
            synchronizer = new DataSynchronizer(memoryDao, postgresDao, txtDao);
        }

        // Первоначальная синхронизация из PostgreSQL
        if (firstTime) {
            synchronizer.syncAll(postgresDao);
            firstTime = false;
        }

        switch (dataSourceType) {
            case "Список в памяти": return memoryDao;
            case "PostgreSQL": return postgresDao;
            case "Текстовый файл": return txtDao;
            default: throw new IllegalArgumentException("Неизвестный тип источника данных: " + dataSourceType);
        }
    }

    public static ProductDao createMemoryDao() {
        if (memoryDao == null) {
            memoryDao = new ProductListDao(new ProductList());
        }
        return memoryDao;
    }

    public static ProductDao createPostgresDao() {
        if (postgresDao == null) {
            postgresDao = new ProductPostgresDao(
                    dotenv.get("DB_URL"),
                    dotenv.get("DB_USER"),
                    dotenv.get("DB_PASSWORD"));
        }
        return postgresDao;
    }

    public static ProductDao createTxtDao() {
        if (txtDao == null) {
            txtDao = new ProductTxtDao(dotenv.get("TXT_FILE_PATH"));
        }
        return txtDao;
    }

    public static void syncAllData(ProductDao sourceDao) {
        if (synchronizer != null) {
            synchronizer.syncAll(sourceDao);
        }
    }
}
package com.example.taskdao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataSynchronizer {
    private final ProductDao memoryDao;
    private final ProductDao postgresDao;
    private final ProductDao txtDao;

    public DataSynchronizer(ProductDao memoryDao, ProductDao postgresDao, ProductDao txtDao) {
        this.memoryDao = memoryDao;
        this.postgresDao = postgresDao;
        this.txtDao = txtDao;
    }

    public void syncAll(ProductDao sourceDao) {
        try {
            // Получаем данные из источника
            List<Product> sourceProducts = sourceDao.getAllProducts();

            // Синхронизируем все источники с текущим активным
            if (sourceDao != memoryDao) {
                syncToSource(sourceProducts, memoryDao);
            }
            if (sourceDao != postgresDao) {
                syncToSource(sourceProducts, postgresDao);
            }
            if (sourceDao != txtDao) {
                syncToSource(sourceProducts, txtDao);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // В реальном приложении здесь должна быть обработка ошибок
        }
    }

    private void syncToSource(List<Product> sourceProducts, ProductDao targetDao) {
        try {
            List<Product> targetProducts = targetDao.getAllProducts();

            // Удаляем только те продукты, которых нет в источнике
            List<Product> productsToDelete = targetProducts.stream()
                    .filter(targetProduct -> sourceProducts.stream()
                            .noneMatch(sourceProduct -> sourceProduct.getId() == targetProduct.getId()))
                    .collect(Collectors.toList());

            for (Product p : productsToDelete) {
                targetDao.deleteProduct(p.getId());
            }

            // Добавляем/обновляем продукты, сохраняя оригинальные ID
            for (Product sourceProduct : sourceProducts) {
                Product productToSync = new Product(
                        sourceProduct.getId(), // Сохраняем оригинальный ID
                        sourceProduct.getName(),
                        sourceProduct.getQuantity(),
                        sourceProduct.getTag(),
                        new ProductStatusManager(5, 10)
                );

                if (targetProducts.stream().anyMatch(p -> p.getId() == sourceProduct.getId())) {
                    targetDao.updateProduct(productToSync);
                } else {
                    targetDao.addProduct(productToSync);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
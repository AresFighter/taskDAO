package com.example.taskdao;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProductTxtDao implements ProductDao {
    private String filePath;

    public ProductTxtDao(String filePath) {
        this.filePath = filePath;
        // Создаем файл с заголовками, если он не существует
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("id,name,quantity,tag");
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        File file = new File(filePath);

        // Если файл не существует или пуст, возвращаем пустой список
        if (!file.exists() || file.length() == 0) {
            return products;
        }

        try (Scanner scanner = new Scanner(file)) {
            // Пропускаем заголовки, если они есть
            if (scanner.hasNextLine()) {
                String firstLine = scanner.nextLine();
                if (!firstLine.startsWith("id,name,quantity,tag")) {
                    // Если первая строка не заголовки, обрабатываем ее как данные
                    processLine(firstLine, products);
                }
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    processLine(line, products);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении файла: " + e.getMessage(), e);
        }
        return products;
    }

    private void processLine(String line, List<Product> products) {
        String[] parts = line.split(",");
        if (parts.length == 4) {
            try {
                int id = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();
                int quantity = Integer.parseInt(parts[2].trim());
                String tag = parts[3].trim();
                products.add(new Product(id, name, quantity, tag, new ProductStatusManager(5, 10)));
            } catch (NumberFormatException e) {
                System.err.println("Ошибка формата данных в строке: " + line);
            }
        }
    }

    @Override
    public void addProduct(Product product) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            if (new File(filePath).length() == 0) {
                writer.write("id,name,quantity,tag");
                writer.newLine();
            }
            // Записываем с тем ID, который был передан
            writer.write(product.getId() + "," + product.getName() + "," + product.getQuantity() + "," + product.getTag());
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при записи в файл", e);
        }
    }

    @Override
    public void updateProduct(Product product) {
        List<Product> products = getAllProducts();
        for (Product p : products) {
            if (p.getId() == product.getId()) {
                p.setName(product.getName());
                p.setQuantity(product.getQuantity());
                p.setTag(product.getTag());
                break;
            }
        }
        saveAllProducts(products);
    }

    @Override
    public void deleteProduct(int id) {
        List<Product> products = getAllProducts();
        products.removeIf(p -> p.getId() == id);
        saveAllProducts(products);
    }

    private void saveAllProducts(List<Product> products) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("id,name,quantity,tag");
            writer.newLine();
            for (Product product : products) {
                writer.write(product.getId() + "," + product.getName() + "," +
                        product.getQuantity() + "," + product.getTag());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении данных", e);
        }
    }
}
package com.example.taskdao;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.*;


public class ProductTxtDao implements ProductDao {
    private String filePath;

    public ProductTxtDao(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(filePath))) {
            // Пропускаем первую строку (заголовки)
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // Пропустить строку с заголовками
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 4) { // Все поля
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    int quantity = Integer.parseInt(parts[2]);
                    String tag = parts[3];
                    products.add(new Product(id, name, quantity, tag, new ProductStatusManager(5, 10)));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении файла: " + e.getMessage(), e);
        }
        return products;
    }

    @Override
    public void addProduct(Product product) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(product.getId() + "," + product.getName() + "," + product.getQuantity() + "," + product.getTag());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
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
            // Записываем заголовки
            writer.write("id,name,quantity,tag");
            writer.newLine();

            // Записываем данные
            for (Product product : products) {
                writer.write(product.getId() + "," + product.getName() + "," + product.getQuantity() + "," + product.getTag());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
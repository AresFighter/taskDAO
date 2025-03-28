package com.example.taskdao;

import java.util.ArrayList;
import java.util.List;

public class ProductList {
    private List<Product> products;

    public ProductList() {
        products = new ArrayList<>();
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public void updateProduct(int id, String name, int quantity, String tag) {
        for (Product product : products) {
            if (product.getId() == id) {
                product.setName(name);
                product.setQuantity(quantity);
                product.setTag(tag);
                break;
            }
        }
    }

    public void deleteProduct(int id) {
        products.removeIf(product -> product.getId() == id);
    }

    public List<Product> getAllProducts() {
        return products;
    }
}
package com.example.taskdao;

import java.util.List;

public interface ProductDao {
    void addProduct(Product product);
    void updateProduct(Product product);
    void deleteProduct(int id);
    List<Product> getAllProducts();
}


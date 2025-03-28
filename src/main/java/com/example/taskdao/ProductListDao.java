package com.example.taskdao;

import java.util.List;

public class ProductListDao implements ProductDao {
    private ProductList productList;

    public ProductListDao(ProductList productList) {
        this.productList = productList;
    }

    @Override
    public void addProduct(Product product) {
        productList.addProduct(product);
    }

    @Override
    public void updateProduct(Product product) {
        for (Product p : productList.getAllProducts()) {
            if (p.getId() == product.getId()) {
                p.setName(product.getName());
                p.setQuantity(product.getQuantity());
                p.setTag(product.getTag());
                break;
            }
        }
    }

    @Override
    public void deleteProduct(int id) {
        productList.deleteProduct(id);
    }

    @Override
    public List<Product> getAllProducts() {
        return productList.getAllProducts();
    }
}
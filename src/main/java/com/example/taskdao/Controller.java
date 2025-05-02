package com.example.taskdao;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

public class Controller {
    @FXML
    private TableView<Product> productTable;

    @FXML
    private TextField nameField;

    @FXML
    private TextField quantityField;

    @FXML
    private TextField tagField;

    @FXML
    private ChoiceBox<String> dataSourceChoiceBox;

    @FXML
    private Button selectDataSourceButton;

    private ProductDao productDao;

    private ProductStatusManager statusManager;
    private ProductDao memoryDao;
    private ProductDao postgresDao;
    private ProductDao txtDao;

    @FXML
    public void initialize() {
        // Инициализация всех DAO
        memoryDao = ProductDaoFactory.createMemoryDao();
        postgresDao = ProductDaoFactory.createPostgresDao();
        txtDao = ProductDaoFactory.createTxtDao();

        // Инициализация выбора источника данных
        dataSourceChoiceBox.getItems().addAll("Список в памяти", "PostgreSQL", "Текстовый файл");
        dataSourceChoiceBox.setValue("Список в памяти");

        // Инициализация DAO по умолчанию
        productDao = ProductDaoFactory.createProductDao(dataSourceChoiceBox.getValue());


        // Инициализация менеджера статусов
        statusManager = new ProductStatusManager(5, 10);

        TableColumn<Product, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Product, String> nameColumn = new TableColumn<>("Название");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, Integer> quantityColumn = new TableColumn<>("Количество");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Product, String> tagColumn = new TableColumn<>("Тег");
        tagColumn.setCellValueFactory(new PropertyValueFactory<>("tag"));

        TableColumn<Product, String> statusColumn = new TableColumn<>("Статус");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Добавляем столбцы в таблицу
        productTable.getColumns().addAll(idColumn, nameColumn, quantityColumn, tagColumn, statusColumn);

        // Загрузка данных в таблицу
        refreshTable();

        // Обработка выбора товара в таблице
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Заполняем поля ввода данными выбранного товара
                nameField.setText(newSelection.getName());
                quantityField.setText(String.valueOf(newSelection.getQuantity()));
                tagField.setText(newSelection.getTag());
            }
        });

        // Обработка выбора источника данных
        /*selectDataSourceButton.setOnAction(event -> {
            String selectedDataSource = dataSourceChoiceBox.getValue();
            productDao = ProductDaoFactory.createProductDao(selectedDataSource);
            refreshTable();
        });*/
        // Обработка выбора источника данных
        selectDataSourceButton.setOnAction(event -> {
            String selectedDataSource = dataSourceChoiceBox.getValue();
            switch (selectedDataSource) {
                case "Список в памяти":
                    productDao = memoryDao;
                    break;
                case "PostgreSQL":
                    productDao = postgresDao;
                    break;
                case "Текстовый файл":
                    productDao = txtDao;
                    break;
            }
            refreshTable();
        });
    }

    @FXML
    public void handleAddProduct() {
        try {
            if (!validateFields()) {
                return;
            }

            int quantity = Integer.parseInt(quantityField.getText());
            if (statusManager.isLeapYear()) {
                quantity *= 2;
            }

            // Генерируем новый ID только один раз
            int newId = generateNewId();

            Product product = new Product(newId, nameField.getText(), quantity, tagField.getText(), statusManager);
            productDao.addProduct(product);

            // Синхронизируем все источники данных из текущего активного
            ProductDaoFactory.syncAllData(productDao);

            refreshTable();
            clearFields();
        } catch (Exception e) {
            showError("Ошибка", "Произошла ошибка при добавлении товара: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int generateNewId() {
        // Находим максимальный ID среди всех источников
        int maxMemoryId = memoryDao.getAllProducts().stream().mapToInt(Product::getId).max().orElse(0);
        int maxPostgresId = postgresDao.getAllProducts().stream().mapToInt(Product::getId).max().orElse(0);
        int maxTxtId = txtDao.getAllProducts().stream().mapToInt(Product::getId).max().orElse(0);

        // Возвращаем максимальный ID + 1
        return Math.max(Math.max(maxMemoryId, maxPostgresId), maxTxtId) + 1;
    }

    private int getNextIdFromPostgres() throws SQLException {
        try (Statement stmt = ((ProductPostgresDao)productDao).getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nextval('products_id_seq')")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 1;
    }

    private int getNextIdFromTxt() {
        return productDao.getAllProducts().stream()
                .mapToInt(Product::getId)
                .max()
                .orElse(0) + 1;
    }

    @FXML
    public void handleUpdateProduct() {
        try {
            Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
            if (selectedProduct == null) {
                showError("Ошибка", "Товар не выбран.");
                return;
            }

            if (!validateFields()) {
                return;
            }

            int quantity = Integer.parseInt(quantityField.getText());
            selectedProduct.setName(nameField.getText());
            selectedProduct.setQuantity(quantity);
            selectedProduct.setTag(tagField.getText());

            productDao.updateProduct(selectedProduct);
            // Синхронизируем все источники данных из текущего активного
            ProductDaoFactory.syncAllData(productDao);

            clearFields();
            refreshTable();
        } catch (Exception e) {
            showError("Ошибка", "Произошла ошибка при изменении товара: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeleteProduct() {
        try {
            Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
            if (selectedProduct == null) {
                showError("Ошибка", "Товар не выбран.");
                return;
            }

            productDao.deleteProduct(selectedProduct.getId());
            // Синхронизируем все источники данных из текущего активного
            ProductDaoFactory.syncAllData(productDao);

            clearFields();
            refreshTable();
        } catch (Exception e) {
            showError("Ошибка", "Произошла ошибка при удалении товара: " + e.getMessage());
        }
    }

    @FXML
    public void handleShowNeededProducts() {
        try {
            List<Product> neededProducts = getNeededProducts();
            if (neededProducts.isEmpty()) {
                showInfo("Информация", "Нет товаров, которые нужно докупить.");
            } else {
                StringBuilder message = new StringBuilder("Товары, которые нужно докупить:\n");
                for (Product product : neededProducts) {
                    message.append(product.getName()).append(" (Количество: ").append(product.getQuantity()).append(")\n");
                }
                showInfo("Нужные товары", message.toString());
            }
        } catch (Exception e) {
            showError("Ошибка", "Произошла ошибка при получении списка нужных товаров: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        if (nameField.getText().isEmpty() || quantityField.getText().isEmpty() || tagField.getText().isEmpty()) {
            showError("Ошибка", "Все поля должны быть заполнены.");
            return false;
        }

        try {
            Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            showError("Ошибка", "Количество должно быть числом.");
            return false;
        }

        return true;
    }

    private List<Product> getNeededProducts() {
        List<Product> neededProducts = new ArrayList<>();
        for (Product product : productDao.getAllProducts()) {
            if ("Нужен".equals(product.getStatus())) {
                neededProducts.add(product);
            }
        }
        return neededProducts;
    }

    private void refreshTable() {
        // Получаем все продукты из текущего DAO
        ObservableList<Product> products = FXCollections.observableArrayList(productDao.getAllProducts());
        productTable.setItems(products);
        productTable.refresh(); // Принудительное обновление таблицы
    }

    private void clearFields() {
        nameField.clear();
        quantityField.clear();
        tagField.clear();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
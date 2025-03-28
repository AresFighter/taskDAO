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

    @FXML
    public void initialize() {
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
        selectDataSourceButton.setOnAction(event -> {
            String selectedDataSource = dataSourceChoiceBox.getValue();
            productDao = ProductDaoFactory.createProductDao(selectedDataSource);
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

            // Проверяем, является ли текущий год високосным, используя statusManager
            if (statusManager.isLeapYear()) {
                quantity *= 2; // Увеличиваем количество в два раза
            }

            // Создаем новый продукт
            int newId = productDao.getAllProducts().size() + 1;
            Product product = new Product(newId, nameField.getText(), quantity, tagField.getText(), statusManager);

            // Добавляем продукт через DAO
            productDao.addProduct(product);

            // Очищаем поля ввода
            clearFields();

            // Обновляем таблицу
            refreshTable();
        } catch (Exception e) {
            showError("Ошибка", "Произошла ошибка при добавлении товара: " + e.getMessage());
        }
    }


    @FXML
    public void handleUpdateProduct() {
        try {
            // Проверка, что товар выбран
            Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
            if (selectedProduct == null) {
                showError("Ошибка", "Товар не выбран.");
                return;
            }

            // Проверка полей
            if (!validateFields()) {
                return;
            }

            // Получаем количество
            int quantity = Integer.parseInt(quantityField.getText());

            // Обновляем данные продукта
            selectedProduct.setName(nameField.getText());
            selectedProduct.setQuantity(quantity);
            selectedProduct.setTag(tagField.getText());

            // Обновляем продукт через DAO
            productDao.updateProduct(selectedProduct);

            // Очищаем поля ввода
            clearFields();

            // Обновляем таблицу
            refreshTable();
        } catch (Exception e) {
            showError("Ошибка", "Произошла ошибка при изменении товара: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeleteProduct() {
        try {
            // Проверка, что товар выбран
            Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
            if (selectedProduct == null) {
                showError("Ошибка", "Товар не выбран.");
                return;
            }

            // Удаляем продукт через DAO
            productDao.deleteProduct(selectedProduct.getId());

            // Очищаем поля ввода
            clearFields();

            // Обновляем таблицу
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
        // Получаем все продукты из DAO и обновляем таблицу
        ObservableList<Product> products = FXCollections.observableArrayList(productDao.getAllProducts());
        productTable.setItems(products);
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
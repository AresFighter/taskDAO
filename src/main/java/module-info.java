module com.example.taskdao {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.dotenv;


    opens com.example.taskdao to javafx.fxml;
    exports com.example.taskdao;
}
module com.example.secedgarjavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires org.jsoup;
    requires com.google.gson;


    opens com.example.secedgarjavafx to javafx.fxml;
    exports com.example.secedgarjavafx;
}
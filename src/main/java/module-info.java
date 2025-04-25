module dk.easv.belman {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.microsoft.sqlserver.jdbc;
    requires java.sql;
    requires java.naming;


    opens dk.easv.belman to javafx.fxml;
    exports dk.easv.belman;
}
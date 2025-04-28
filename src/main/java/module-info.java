module dk.easv.belman {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.microsoft.sqlserver.jdbc;
    requires java.sql;
    requires java.naming;
    requires javafx.swing;
    requires slf4j.api;


    opens dk.easv.belman to javafx.fxml;
    exports dk.easv.belman;
    exports dk.easv.belman.PL;
    opens dk.easv.belman.PL to javafx.fxml;
}
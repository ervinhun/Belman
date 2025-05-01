module dk.easv.belman {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.microsoft.sqlserver.jdbc;
    requires java.sql;
    requires java.naming;
    requires org.slf4j;
    requires org.apache.pdfbox;
    requires MaterialFX;
    requires com.fasterxml.jackson.databind;

    opens dk.easv.belman to javafx.fxml;
    exports dk.easv.belman;
    exports dk.easv.belman.PL;
    opens dk.easv.belman.PL to javafx.fxml;
    opens dk.easv.belman.dal to com.fasterxml.jackson.databind;
}
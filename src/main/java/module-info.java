module dk.easv.belman {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.microsoft.sqlserver.jdbc;
    requires java.sql;
    requires java.naming;
    requires com.google.zxing;
    requires webcam.capture;
    requires com.google.zxing.javase;
    requires javafx.swing;
    requires javafx.swing;
    requires org.slf4j;
    requires MaterialFX;


    opens dk.easv.belman to javafx.fxml;
    exports dk.easv.belman;
    exports dk.easv.belman.PL;
    opens dk.easv.belman.PL to javafx.fxml;
}
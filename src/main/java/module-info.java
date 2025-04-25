module dk.easv.belman {
    requires javafx.controls;
    requires javafx.fxml;


    opens dk.easv.belman to javafx.fxml;
    exports dk.easv.belman;
    exports dk.easv.belman.PL;
    opens dk.easv.belman.PL to javafx.fxml;
}
package dk.easv.belman.PL;

import dk.easv.belman.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class HomeController
{
    @FXML
    private BorderPane borderPane;

    @FXML
    private void cameraOpen()
    {
        // QR/BARCODE READING

        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/operatorOrders.fxml"));
            fxmlLoader.setController(this);
            Parent vbox = fxmlLoader.load();

            borderPane.setCenter(vbox);
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void operatorTab()
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/operatorLogin.fxml"));
            fxmlLoader.setController(this);
            Parent vbox = fxmlLoader.load();

            borderPane.setCenter(vbox);
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void qualityTab()
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/qualityLogin.fxml"));
            fxmlLoader.setController(this);
            Parent vbox = fxmlLoader.load();

            borderPane.setCenter(vbox);
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void adminTab()
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/qualityLogin.fxml"));
            fxmlLoader.setController(this);
            Parent vbox = fxmlLoader.load();

            borderPane.setCenter(vbox);
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void login()
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/qualityOrders.fxml"));
            fxmlLoader.setController(this);
            Parent vbox = fxmlLoader.load();

            borderPane.setCenter(vbox);
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void search()
    {

    }

    @FXML
    private void selectOrder()
    {

    }

    @FXML
    private void continueOrder()
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/operatorPictures.fxml"));
            fxmlLoader.setController(this);
            Parent vbox = fxmlLoader.load();

            borderPane.setCenter(vbox);
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void backToOrders()
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FXML/operatorOrders.fxml"));
            fxmlLoader.setController(this);
            Parent vbox = fxmlLoader.load();

            borderPane.setCenter(vbox);
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
    }
}
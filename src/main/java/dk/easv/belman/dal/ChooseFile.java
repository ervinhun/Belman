package dk.easv.belman.dal;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;


public class ChooseFile {

    public List<File> getChosenFile() {
        return chosenFile;
    }

    private List<File> chosenFile;
    private final Logger logger = LoggerFactory.getLogger(ChooseFile.class);


    public ChooseFile(Window window, List<File> chosenFileForTest) {
        FileChooser fileChooser = new FileChooser();
        //Sets the window title
        fileChooser.setTitle("Upload Image Files");

        //Sets what kind of files the user can open
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter
                ("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif");
        //Sets the filter for the browser
        fileChooser.getExtensionFilters().addAll(extFilter);
        //Open dialog for multiple files, when finishes the File will be stored in chosenFile only if it is not for test
        if (chosenFileForTest == null) {
            this.chosenFile = fileChooser.showOpenMultipleDialog(window);
        } else {
            this.chosenFile = chosenFileForTest;
        }


    }

    public List<String> getSelectedFilePath() {
        if (chosenFile != null && !chosenFile.isEmpty()) {
            List<String> filepaths = chosenFile.stream()
                    .map(File::getAbsolutePath)
                    .toList();
            for (File file : chosenFile) {
                logger.info("File path: {}", file.getAbsolutePath());
            }
            return filepaths;
        } else
            logger.error("File is null, can not return filePath");
        return List.of();
    }



    static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1).toLowerCase();
        } else {
            return "";
        }
    }


}

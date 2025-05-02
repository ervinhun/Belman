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
    private static final int THUMBNAIL_SIZE = 150;
    private final Logger logger = LoggerFactory.getLogger(ChooseFile.class);


    public ChooseFile(Window window, String productNoString, List<File> chosenFileForTest) {
        String imagePath = FilePaths.IMAGE_PATH;
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

        if (chosenFile != null && productNoString != null) {
            //Creates the directories for the images
            File targetDir = new File(imagePath + productNoString);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            File thumbnailDir = new File(targetDir.getAbsolutePath() + "/thumbnail");
            if (!thumbnailDir.exists()) {
                thumbnailDir.mkdirs();
            }
            for (File file : chosenFile) {
                File targetFile = new File(targetDir, file.getName());
                File thumbnailFile = new File(thumbnailDir, file.getName());

                try {
                    // Save original
                    Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    // Create thumbnail
                    createThumbnail(file, thumbnailFile);

                } catch (IOException e) {
                    logger.error("Failed to create thumbnail for " + file.getName(), e);
                }
            }

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

    private void createThumbnail(File sourceFile, File destinationFile) {
        try {
            // Load the image
            javafx.scene.image.Image productImage = new javafx.scene.image.Image(sourceFile.toURI().toString());

            // Create an ImageView to scale the image
            ImageView thumbnailView = new ImageView(productImage);

            thumbnailView.setFitWidth(THUMBNAIL_SIZE);
            thumbnailView.setFitHeight(THUMBNAIL_SIZE);

            thumbnailView.setPreserveRatio(true);
            thumbnailView.setSmooth(true);

            // Snapshot the ImageView to create a resized image
            WritableImage writableImage = thumbnailView.snapshot(null, null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);

            String extension = getFileExtension(sourceFile.getName());

            if (extension.equals("jpg") || extension.equals("jpeg")) {
                // JPG: needs background (white)
                BufferedImage rgbImage = new BufferedImage(
                        bufferedImage.getWidth(),
                        bufferedImage.getHeight(),
                        BufferedImage.TYPE_INT_RGB
                );
                Graphics2D g = rgbImage.createGraphics();
                g.setColor(java.awt.Color.WHITE);
                g.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
                g.drawImage(bufferedImage, 0, 0, null);
                g.dispose();

                ImageIO.write(rgbImage, "jpg", destinationFile);

            } else if (extension.equals("png") || extension.equals("gif")) {
                // PNG and GIF can be saved directly
                ImageIO.write(bufferedImage, extension, destinationFile);
            } else {
                // Fallback
                ImageIO.write(bufferedImage, "png", destinationFile);
            }
        } catch (IOException e) {
            logger.error("Failed to create thumbnail for " + sourceFile.getName(), e);
        }
    }


    static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1).toLowerCase();
        } else {
            return "";
        }
    }

    public static int getThumbnailSize() {
        return THUMBNAIL_SIZE;
    }

}

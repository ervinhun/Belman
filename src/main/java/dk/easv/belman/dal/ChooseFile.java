package dk.easv.belman.dal;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ChooseFile {
    private List<File> chosenFile;

    public ChooseFile(Window window, String productNoString) {
        FileChooser fileChooser = new FileChooser();
        //Sets the window title
        fileChooser.setTitle("Upload Image Files");

        //Sets what kind of files the user can open
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter
                ("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif");
        //Sets the filter for the browser
        fileChooser.getExtensionFilters().addAll(extFilter);
        //Open dialog for multiple files, when finishes the File will be stored in chosenFile
        chosenFile = fileChooser.showOpenMultipleDialog(window);

        if (chosenFile != null && productNoString != null) {
            File targetDir = new File("./src/main/resources/dk/easv/belman/SavedImages/" + productNoString);
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
                    ImageView thumbnailView = CreateThumbnail.createThumbnail(file.toURI().toString());
                    WritableImage writableImage = thumbnailView.snapshot(null, null);
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
                    String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);

                    if (extension.equals("jpg") || extension.equals("jpeg")) {
                        // JPG needs to remove transparency
                        BufferedImage rgbImage = new BufferedImage(
                                bufferedImage.getWidth(),
                                bufferedImage.getHeight(),
                                BufferedImage.TYPE_INT_RGB
                        );
                        Graphics2D g = rgbImage.createGraphics();
                        g.setColor(java.awt.Color.WHITE); // Set background to white
                        g.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
                        g.drawImage(bufferedImage, 0, 0, null);
                        g.dispose();

                        ImageIO.write(rgbImage, "jpg", thumbnailFile);

                    } else if (extension.equals("png") || extension.equals("gif")) {
                        // PNG and GIF can keep transparency
                        ImageIO.write(bufferedImage, extension, thumbnailFile);
                    } else {
                        // Unknown extension — safe fallback: PNG
                        ImageIO.write(bufferedImage, "png", thumbnailFile);
                    }

                    System.out.println("Original saved: " + targetFile.getAbsolutePath());
                    System.out.println("Thumbnail saved: " + thumbnailFile.getAbsolutePath());

                } catch (IOException e) {
                    throw new RuntimeException(e);
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
                System.out.println(file.getName());
            }
            return filepaths;
        } else
            throw new RuntimeException("File is null, can not return filePath");
    }
}

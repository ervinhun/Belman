package dk.easv.belman.PL;

import dk.easv.belman.PL.model.PhotoPreviewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.util.List;

public class PhotoPreviewController {

    @FXML private ImageView imageView;
    @FXML private Button btnNext, btnPrev;

    private final PhotoPreviewModel model = new PhotoPreviewModel();
    private QualityController parentController;

    public void setPhotos(List<File> photos, int selectedIndex, QualityController parentController) {
        this.parentController = parentController;
        model.setPhotos(photos, selectedIndex);
        updateImage();
    }

    private void updateImage() {
        File file = model.getCurrentPhoto();
        if (file != null && file.exists()) {
            imageView.setImage(new Image(file.toURI().toString()));
        }
        btnPrev.setDisable(!model.hasPrevious());
        btnNext.setDisable(!model.hasNext());
    }

    @FXML
    private void nextImage() {
        model.goToNext();
        updateImage();
    }

    @FXML
    private void prevImage() {
        model.goToPrevious();
        updateImage();
    }

    @FXML
    private void goBack() {
        parentController.cancel();
    }
}

package dk.easv.belman.pl;

import dk.easv.belman.be.Photo;
import dk.easv.belman.pl.AbstractOrderController;
import dk.easv.belman.pl.QualityController;
import dk.easv.belman.pl.model.PhotoPreviewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.util.List;

public class PhotoPreviewController {

    @FXML private ImageView imageView;
    @FXML private Button    btnNext;
    @FXML private Button    btnPrev;

    private final PhotoPreviewModel model = new PhotoPreviewModel();
    private QualityController parentController;

    public void setPhotos(List<Photo> photos, int selectedIndex, QualityController parentController) {
        this.parentController = parentController;
        model.setPhotos(photos, selectedIndex);
        updateImage();
    }

    private void updateImage() {
        Photo p = model.getCurrentPhoto();
        if (p != null && p.getPhotoFile() != null) {
            imageView.setImage(
                    new Image(new ByteArrayInputStream(p.getPhotoFile()))
            );
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

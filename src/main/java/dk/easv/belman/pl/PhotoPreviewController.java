package dk.easv.belman.pl;

import com.github.sarxos.webcam.Webcam;
import dk.easv.belman.be.Photo;
import dk.easv.belman.pl.AbstractOrderController;
import dk.easv.belman.pl.QualityController;
import dk.easv.belman.pl.model.PhotoPreviewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.util.List;

public class PhotoPreviewController {

    @FXML private ImageView imageView;
    @FXML private Button    btnNext;
    @FXML private Button    btnPrev;
    @FXML private Label     angleLabel;
    @FXML private StackPane stackP;

    private final PhotoPreviewModel model = new PhotoPreviewModel();
    private QualityController parentController;

    @FXML
    public void initialize() {
        imageView.fitHeightProperty().bind(stackP.heightProperty());
        imageView.fitWidthProperty().bind(stackP.widthProperty());
    }

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
            angleLabel.setText(p.getAngle());
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
        parentController.returnToOrder();
    }
}

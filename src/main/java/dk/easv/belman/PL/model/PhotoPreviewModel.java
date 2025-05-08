package dk.easv.belman.PL.model;

import java.io.File;
import java.util.List;

public class PhotoPreviewModel {
    private List<File> photoFiles;
    private int currentIndex;

    public void setPhotos(List<File> photos, int selectedIndex) {
        this.photoFiles = photos;
        this.currentIndex = Math.max(0, Math.min(selectedIndex, photos.size() - 1));
    }

    public File getCurrentPhoto() {
        return photoFiles != null && !photoFiles.isEmpty() ? photoFiles.get(currentIndex) : null;
    }

    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    public boolean hasNext() {
        return photoFiles != null && currentIndex < photoFiles.size() - 1;
    }

    public void goToPrevious() {
        if (hasPrevious()) currentIndex--;
    }

    public void goToNext() {
        if (hasNext()) currentIndex++;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public List<File> getPhotoFiles() {
        return photoFiles;
    }
}

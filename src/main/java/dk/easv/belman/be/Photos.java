package dk.easv.belman.be;

import java.time.LocalDateTime;
import java.util.UUID;

public class Photos {

    private long id;
    private long productId;
    private UUID uploadedBy;
    private String imagePath;
    private LocalDateTime uploadedAt;
    private boolean isDeleted;
    private UUID deletedBy;
    private LocalDateTime deletedAt;

    public Photos(long id, long productId, UUID uploadedBy, String imagePath, LocalDateTime uploadedAt, UUID deletedBy, LocalDateTime deletedAt) {
        this.id = id;
        this.productId = productId;
        this.uploadedBy = uploadedBy;
        this.imagePath = imagePath;
        this.uploadedAt = uploadedAt;
        this.deletedBy = deletedBy;
        this.deletedAt = deletedAt;
        this.isDeleted = deletedBy != null;
    }

    public Photos (long productId, String imagePath) {
        this.productId = productId;
        this.imagePath = imagePath;
    }

    public long getId() {
        return id;
    }

    public long getProductId() {
        return productId;
    }

    public UUID getUploadedBy() {
        return uploadedBy;
    }

    public String getImagePath() {
        return imagePath;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public UUID getDeletedBy() {
        return deletedBy;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public void setUploadedBy(UUID uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public void setDeletedBy(UUID deletedBy) {
        this.deletedBy = deletedBy;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}

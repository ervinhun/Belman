package dk.easv.belman.be;

import java.time.LocalDateTime;
import java.util.UUID;

public class Photo {
    private Long id;
    // SWITCH TO USER
    private UUID uploadedBy;
    private String imagePath;
    private LocalDateTime uploadedAt;
    private Boolean isDeleted;
    // SWITCH TO USER
    private UUID deletedBy;
    private LocalDateTime deletedAt;
    private String angle;

    public Photo(Long id, UUID uploadedBy, String imagePath, String angle, LocalDateTime uploadedAt, Boolean isDeleted)
    {
        this.id = id;
        this.uploadedBy = uploadedBy;
        this.imagePath = imagePath;
        this.angle = angle;
        this.uploadedAt = uploadedAt;
        this.isDeleted = isDeleted;
    }

    public Long getId()
    {
        return id;
    }

    public UUID getUploadedBy()
    {
        return uploadedBy;
    }

    public String getImagePath()
    {
        return imagePath;
    }

    public LocalDateTime getUploadedAt()
    {
        return uploadedAt;
    }

    public Boolean getIsDeleted()
    {
        return isDeleted;
    }

    public UUID getDeletedBy()
    {
        return deletedBy;
    }

    public LocalDateTime getDeletedAt()
    {
        return deletedAt;
    }

    public String getAngle() { return angle; }

    public void setId(Long id) {
        this.id = id;
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

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public void setDeletedBy(UUID deletedBy) {
        this.deletedBy = deletedBy;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public void setAngle(String angle) {
        this.angle = angle;
    }
}

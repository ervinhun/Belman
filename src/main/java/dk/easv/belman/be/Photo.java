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

    public Photo(Long id, UUID uploadedBy, String imagePath, LocalDateTime uploadedAt, Boolean isDeleted, UUID deletedBy, LocalDateTime deletedAt)
    {
        this.id = id;
        this.uploadedBy = uploadedBy;
        this.imagePath = imagePath;
        this.uploadedAt = uploadedAt;
        this.isDeleted = isDeleted;
        this.deletedBy = deletedBy;
        this.deletedAt = deletedAt;
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
}

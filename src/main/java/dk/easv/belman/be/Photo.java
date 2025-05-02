package dk.easv.belman.be;

import java.util.UUID;

public class Photo {
    private UUID id;
    private User uploadedBy;
    private String imagePath;
    private String uploadedAt;
    private Boolean isDeleted;
    private User deletedBy;
    private String deletedAt;

    public Photo(UUID id, User uploadedBy, String imagePath, String uploadedAt, Boolean isDeleted, User deletedBy, String deletedAt)
    {
        this.id = id;
        this.uploadedBy = uploadedBy;
        this.imagePath = imagePath;
        this.uploadedAt = uploadedAt;
        this.isDeleted = isDeleted;
        this.deletedBy = deletedBy;
        this.deletedAt = deletedAt;
    }

    public UUID getId()
    {
        return id;
    }

    public User getUploadedBy()
    {
        return uploadedBy;
    }

    public String getImagePath()
    {
        return imagePath;
    }

    public String getUploadedAt()
    {
        return uploadedAt;
    }

    public Boolean getIsDeleted()
    {
        return isDeleted;
    }

    public User getDeletedBy()
    {
        return deletedBy;
    }

    public String getDeletedAt()
    {
        return deletedAt;
    }
}

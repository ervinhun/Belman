package dk.easv.belman.be;

import java.util.List;
import java.util.UUID;

public class Order {
    private UUID id;
    private String productNumber;
    private String createdAt;
    private Boolean isDeleted;
    private List<Photo> photos;
    private Boolean isSigned;

    public Order(UUID id, String productNumber, String createdAt, Boolean isDeleted, List<Photo> photos, Boolean isSigned)
    {
        this.id = id;
        this.productNumber = productNumber;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
        this.photos = photos;
        this.isSigned = isSigned;
    }

    public UUID getId()
    {
        return id;
    }

    public String getProductNumber()
    {
        return productNumber;
    }

    public String getCreatedAt()
    {
        return createdAt;
    }

    public Boolean getIsDeleted()
    {
        return isDeleted;
    }

    public List<Photo> getPhotos()
    {
        return photos;
    }

    public Boolean getIsSigned()
    {
        return isSigned;
    }
}

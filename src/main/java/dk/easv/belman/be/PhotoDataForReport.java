package dk.easv.belman.be;

import java.awt.image.BufferedImage;

public class PhotoDataForReport {
    BufferedImage image;
    String angle;

    public PhotoDataForReport(BufferedImage image, String angle) {
        this.image = image;
        this.angle = angle;
    }

    public BufferedImage getImage() {
        return image;
    }

    public String getAngle() {
        return angle;
    }

}

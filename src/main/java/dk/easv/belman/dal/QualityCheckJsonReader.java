package dk.easv.belman.dal;

import java.util.List;

public class QualityCheckJsonReader {
    private String title;
    private String dynamicText;
    private List<String> headText;
    private List<String> bodyText;
    private String signedBy;


// Public no-argument constructor
public QualityCheckJsonReader() {
    // Default constructor
          }

    public String getTitle() {
        return title;
    }

    public String getDynamicText() {
        return dynamicText;
    }

    public List<String> getHeadText() {
        return headText;
    }

    public List<String> getBodyText() {
        return bodyText;
    }

    public String getSignedBy() {
        return signedBy;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDynamicText(String dynamicText) {
        this.dynamicText = dynamicText;
    }

    public void setHeadText(List<String> headText) {
        this.headText = headText;
    }

    public void setBodyText(List<String> bodyText) {
        this.bodyText = bodyText;
    }

    public void setSignedBy(String signedBy) {
        this.signedBy = signedBy;
    }
}

package job.search.app.model;

import com.google.firebase.Timestamp;

public class Update {
    private String version;
    private String date;
    private String description;
    private Timestamp timestamp;

    public Update() {
        // Required for Firestore
    }

    public Update(String version, String date, String description, Timestamp timestamp) {
        this.version = version;
        this.date = date;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}

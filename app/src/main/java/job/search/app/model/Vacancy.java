package job.search.app.model;

public class Vacancy {
    private String companyName;
    private String title;
    private String description;
    private String requirements;
    private String city;
    private long salary;
    private String workType;
    private String jobFormat;
    private String employerId;
    public String getCompanyName() {
        return companyName;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getRequirements() {
        return requirements;
    }
    public String getCity() {
        return city;
    }
    public long getSalary() {
        return salary;
    }

    public String getWorkType() {
        return workType;
    }
    public String getJobFormat() {
        return jobFormat;
    }
    public String getEmployerId() {
        return employerId;
    }
    public void setEmployerId(String employerId) {
        this.employerId = employerId;
    }
}
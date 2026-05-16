package job.search.app.model;

import java.io.Serializable;

public class Job implements Serializable {
    private String id;
    private final String title;
    private final String company;
    private final String salary;
    private final String location;
    private final String description;
    private final String requirements;
    private final boolean isRemote;
    private final String category;
    private final String workType;
    private final String schedule;
    private String employerId;


    public Job(String id, String title, String company, String salary, String location,
               String description, String requirements, boolean isRemote, String category, String workType, String schedule, String employerId) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.salary = salary;
        this.location = location;
        this.description = description;
        this.requirements = requirements;
        this.isRemote = isRemote;
        this.category = category;
        this.workType = workType;
        this.schedule = schedule;
        this.employerId = employerId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }

    public String getCompany() { return company; }

    public String getSalary() { return salary; }

    public String getLocation() { return location; }

    public String getDescription() { return description; }

    public String getRequirements() { return requirements; }

    public boolean isRemote() { return isRemote; }
    public String getCategory() { return category; }
    public String getWorkType() { return workType; }
    public String getSchedule() { return schedule; }
    public String getEmployerId() { return employerId; }
    public void setEmployerId(String employerId) { this.employerId = employerId; }

}
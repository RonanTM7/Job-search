package com.example.job.model;

public class Vacancy {
    private String companyName;
    private String title;
    private String description;
    private String requirements;
    private String city;
    private long salary;
    private String workType;

    // Default constructor is required for Firestore
    public Vacancy() {
    }

    public Vacancy(String companyName, String title, String description, String requirements, String city, long salary, String workType) {
        this.companyName = companyName;
        this.title = title;
        this.description = description;
        this.requirements = requirements;
        this.city = city;
        this.salary = salary;
        this.workType = workType;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public long getSalary() {
        return salary;
    }

    public void setSalary(long salary) {
        this.salary = salary;
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }
}

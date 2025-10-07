package com.example.job.model;

import com.example.job.R;

import java.io.Serializable;

public class Job implements Serializable {
    private String id;
    private String title;
    private String company;
    private String salary;
    private String location;
    private String description;
    private String requirements;
    private boolean isRemote;
    private int logoResId;

    public Job() {}

    public Job(String id, String title, String company, String salary, String location,
               String description, String requirements, boolean isRemote) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.salary = salary;
        this.location = location;
        this.description = description;
        this.requirements = requirements;
        this.isRemote = isRemote;
    }
    public int getLogo() {
        return R.drawable.ic_work; // верни дефолтную иконку
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public boolean isRemote() { return isRemote; }
    public void setRemote(boolean remote) { isRemote = remote; }

}
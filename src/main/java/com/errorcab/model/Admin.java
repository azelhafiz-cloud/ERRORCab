package com.errorcab.model;

/**
 * Admin entity extending User.
 * Demonstrates Inheritance and role specialization.
 */
public class Admin extends User {
    private String department;

    public Admin(int id, String name, String email, String phone, String password, boolean active, String department) {
        super(id, name, email, phone, password, Role.ADMIN, active);
        this.department = department != null ? department : "Operations & Dispatch";
    }

    public Admin(String name, String email, String phone, String password) {
        this(0, name, email, phone, password, true, "Operations & Dispatch");
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String getWelcomeSubtitle() {
        return "System Administration & Fleet Monitoring Dashboard";
    }
}

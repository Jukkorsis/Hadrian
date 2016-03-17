package com.northernwall.hadrian.domain;

public class User implements Comparable<User> {
    private String username;
    private String fullName;
    private boolean ops = false;
    private boolean admin = false;
    private boolean automation = false;

    public User(String username, String fullName, boolean ops, boolean admin, boolean automation) {
        this.username = username;
        this.fullName = fullName;
        this.ops = ops;
        this.admin = admin;
        this.automation = automation;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isOps() {
        return ops;
    }

    public void setOps(boolean ops) {
        this.ops = ops;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isAutomation() {
        return automation;
    }

    public void setAutomation(boolean automation) {
        this.automation = automation;
    }

    @Override
    public int compareTo(User o) {
        return username.compareTo(o.username);
    }
    
}

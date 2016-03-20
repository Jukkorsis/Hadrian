package com.northernwall.hadrian.domain;

public class User implements Comparable<User> {
    private String username;
    private String fullName;
    private boolean ops = false;
    private boolean admin = false;
    private boolean deploy = false;
    private boolean audit = false;

    public User(String username, String fullName, boolean ops, boolean admin, boolean deploy, boolean audit) {
        this.username = username;
        this.fullName = fullName;
        this.ops = ops;
        this.admin = admin;
        this.deploy = deploy;
        this.audit = audit;
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

    public boolean isDeploy() {
        return deploy;
    }

    public void setDeploy(boolean deploy) {
        this.deploy = deploy;
    }

    public boolean isAudit() {
        return audit;
    }

    public void setAudit(boolean audit) {
        this.audit = audit;
    }

    @Override
    public int compareTo(User o) {
        return username.compareTo(o.username);
    }
    
}

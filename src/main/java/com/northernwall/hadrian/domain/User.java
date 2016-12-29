package com.northernwall.hadrian.domain;

public class User implements Comparable<User> {
    private String username;

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public int compareTo(User o) {
        return username.compareTo(o.username);
    }
    
}

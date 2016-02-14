package com.northernwall.hadrian.utilityHandlers;

import java.util.Objects;

public class Route {

    private final String target;
    private final String method;

    public Route(String target, String method) {
        this.target = target.toLowerCase();
        this.method = method.toLowerCase();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.target);
        hash = 89 * hash + Objects.hashCode(this.method);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Route other = (Route) obj;
        if (!this.method.equals(other.method)) {
            return false;
        }
        if (!this.target.equals(other.target)) {
            return false;
        }
        return true;
    }

}

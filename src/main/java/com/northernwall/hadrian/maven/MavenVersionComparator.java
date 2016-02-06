package com.northernwall.hadrian.maven;

import java.util.Comparator;

/**
 * MavenVersionComparator compares versions with the newest versions at the
 * top/beginning of the list.
 *
 * @author rthursto
 */
public class MavenVersionComparator implements Comparator<String> {

    @Override
    public int compare(String ver1, String ver2) {
        if (ver1 == null) {
            if (ver2 == null) {
                return 0;
            }
            return -1;
        }
        if (ver2 == null) {
            return 1;
        }

        int i1 = ver1.indexOf(".");
        if (i1 == -1) {
            i1 = ver1.indexOf("-");
        }
        String temp1;
        if (i1 < 1) {
            temp1 = ver1;
        } else {
            temp1 = ver1.substring(0, i1);
        }

        int i2 = ver2.indexOf(".");
        if (i2 == -1) {
            i2 = ver2.indexOf("-");
        }
        String temp2;
        if (i2 < 1) {
            temp2 = ver2;
        } else {
            temp2 = ver2.substring(0, i2);
        }

        try {
            int num1 = Integer.parseInt(temp1);
            int num2 = Integer.parseInt(temp2);
            if (num1 == num2) {
                if (i1 < 1) {
                    if (i2 < 1) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
                if (i2 < 1) {
                    return 1;
                }
                return compare(ver1.substring(i1 + 1), ver2.substring(i2 + 1));
            }
            return num2 - num1;
        } catch (NumberFormatException nfe) {
        }
        return ver2.compareTo(ver1);
    }

}

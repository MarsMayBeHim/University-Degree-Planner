package com.degreeplanner.backend.planner;

public final class Semesters {

    private Semesters() {}

    /** Position on the academic calendar: Spring Y, then Fall Y, then Spring Y+1. */
    public static int ordinal(int year, String term) {
        return 2 * year + ("FALL".equals(term) ? 1 : 0);
    }

    public static int indexOf(int startYear, String startTerm, int year, String term) {
        return ordinal(year, term) - ordinal(startYear, startTerm);
    }

    public static int yearAt(int startYear, String startTerm, int index) {
        return (ordinal(startYear, startTerm) + index) / 2;
    }

    public static String termAt(int startYear, String startTerm, int index) {
        return (ordinal(startYear, startTerm) + index) % 2 == 1 ? "FALL" : "SPRING";
    }
}
package com.degreeplanner.backend.planner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SemestersTest {

    @Test
    void indexesFollowTheAcademicCalendar() {
        assertThat(Semesters.indexOf(2025, "FALL", 2025, "FALL")).isEqualTo(0);
        assertThat(Semesters.indexOf(2025, "FALL", 2026, "SPRING")).isEqualTo(1);
        assertThat(Semesters.indexOf(2025, "FALL", 2026, "FALL")).isEqualTo(2);
        assertThat(Semesters.indexOf(2026, "SPRING", 2026, "FALL")).isEqualTo(1);
    }

    @Test
    void yearAndTermComeBackFromAnIndex() {
        assertThat(Semesters.yearAt(2025, "FALL", 3)).isEqualTo(2027);
        assertThat(Semesters.termAt(2025, "FALL", 3)).isEqualTo("SPRING");
        assertThat(Semesters.termAt(2025, "FALL", 4)).isEqualTo("FALL");
    }
}
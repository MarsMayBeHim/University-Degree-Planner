package com.degreeplanner.backend.repository;

import com.degreeplanner.backend.model.Course;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("select c from Course c where lower(c.subject) like lower(concat(:q, '%')) "
         + "or lower(c.title) like lower(concat('%', :q, '%')) "
         + "or lower(concat(c.subject, ' ', c.number)) like lower(concat(:q, '%')) "
         + "order by c.subject, c.number")
    List<Course> search(@Param("q") String q);

    @Query(value = "select course_id, term_type from course_offering", nativeQuery = true)
    List<Object[]> findAllOfferings();
}
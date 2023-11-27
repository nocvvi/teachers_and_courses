package org.teacher_and_course;

import java.util.ArrayList;
import java.util.List;

public class SchoolManagementSystem {
    private final List<Teacher> teachers;

    public SchoolManagementSystem() {
        this.teachers = new ArrayList<>();
    }

    public void addTeacher(Teacher teacher) {
        teachers.add(teacher);
    }

    public void removeTeacher(Teacher teacher) {
        teachers.remove(teacher);
    }


    public List<Teacher> getTeachers() {
        return teachers;
    }
}

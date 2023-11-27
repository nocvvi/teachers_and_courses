package org.teacher_and_course;

import javax.swing.*;
import java.util.List;

public class SchoolManagementGUI {
    private final SchoolManagementSystem system;
    private final JTextArea textArea;

    public SchoolManagementGUI() {
        this.system = new SchoolManagementSystem();
        this.textArea = new JTextArea(10, 30);
    }

    public void showMainFrame() {
        SwingUtilities.invokeLater(() -> {
            initComponents();
        });
    }

    private void initComponents() {
        JFrame frame = new JFrame("School Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 300);

        JButton showAllButton = new JButton("Show All Teachers");
        showAllButton.addActionListener(e -> showAllTeachers());

        JButton addButton = new JButton("Add Teacher");
        addButton.addActionListener(e -> addTeacher());

        JButton addCourseButton = new JButton("Add Course to Teacher");
        addCourseButton.addActionListener(e -> addCourseToTeacher());

        JButton removeButton = new JButton("Remove Teacher/Course");
        removeButton.addActionListener(e -> removeTeacherOrCourse());

        JButton editButton = new JButton("Edit Teacher/Course");
        editButton.addActionListener(e -> editTeacherOrCourse());

        JButton showCoursesButton = new JButton("Show All Courses");
        showCoursesButton.addActionListener(e -> showAllCourses());

        JPanel panel = new JPanel();
        panel.add(showAllButton);
        panel.add(addButton);
        panel.add(addCourseButton);
        panel.add(removeButton);
        panel.add(editButton);
        panel.add(showCoursesButton);

        JScrollPane scrollPane = new JScrollPane(textArea);
        JPanel mainPanel = new JPanel();
        mainPanel.add(panel);
        mainPanel.add(scrollPane);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void showAllTeachers() {
        List<Teacher> teachers = DatabaseManager.loadAllTeachers();
        StringBuilder output = new StringBuilder("Teachers:\n");
        for (Teacher teacher : teachers) {
            output.append("Name: ").append(teacher.getFullName()).append(", Age: ").append(teacher.getAge()).append("\n");
            output.append("Courses:\n");
            for (Course course : DatabaseManager.loadTeacherCourses(teacher)) {
                output.append(" - ").append(course.getName()).append(" (").append(course.getHours()).append(" hours)\n");
            }
            output.append("\n");
        }
        textArea.setText(output.toString());
    }

    private void addTeacher() {
        String fullName = JOptionPane.showInputDialog("Enter teacher's full name:");
        if (fullName == null || fullName.trim().isEmpty()) {
            showError("Invalid input. Full name cannot be empty.");
            return;
        }

        String ageStr = JOptionPane.showInputDialog("Enter teacher's age:");
        if (ageStr == null || ageStr.trim().isEmpty()) {
            showError("Invalid input. Age cannot be empty.");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException ex) {
            showError("Invalid input. Age must be a valid number.");
            return;
        }

        Teacher newTeacher = new Teacher(fullName, age);
        int teacherId = DatabaseManager.saveTeacher(newTeacher);
        newTeacher.setId(teacherId);

        showMessage("Teacher added successfully.");
    }

    private void addCourseToTeacher() {
        String teacherName = JOptionPane.showInputDialog("Enter teacher's full name:");
        Teacher selectedTeacher = DatabaseManager.findTeacherByName(teacherName);
        if (selectedTeacher == null) {
            showError("Teacher not found.");
            return;
        }

        String courseName = JOptionPane.showInputDialog("Enter course name:");
        if (courseName == null || courseName.trim().isEmpty()) {
            showError("Invalid input. Course name cannot be empty.");
            return;
        }

        String hoursStr = JOptionPane.showInputDialog("Enter course hours:");
        if (hoursStr == null || hoursStr.trim().isEmpty()) {
            showError("Invalid input. Hours cannot be empty.");
            return;
        }

        int hours;
        try {
            hours = Integer.parseInt(hoursStr);
        } catch (NumberFormatException ex) {
            showError("Invalid input. Hours must be a valid number.");
            return;
        }

        Course newCourse = new Course(courseName, hours);
        int courseId = DatabaseManager.saveCourse(newCourse);
        newCourse.setId(courseId);

        //System.out.println("Course saved: " + newCourse);

        DatabaseManager.saveTeacherCourse(selectedTeacher.getId(), courseId);

        showMessage("Course added to teacher successfully.");
    }


    private void removeTeacherOrCourse() {
        String teacherName = JOptionPane.showInputDialog("Enter teacher's full name:");
        Teacher selectedTeacher = DatabaseManager.findTeacherByName(teacherName);
        if (selectedTeacher == null) {
            showError("Teacher not found.");
            return;
        }

        String[] options = {"Remove Teacher", "Remove Course"};
        int choice = JOptionPane.showOptionDialog(null, "Choose action:", "Remove Teacher/Course", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            DatabaseManager.removeTeacher(selectedTeacher);
            showMessage("Teacher removed successfully.");
        } else if (choice == 1) {
            String courseName = JOptionPane.showInputDialog("Enter course name to remove:");
            Course courseToRemove = DatabaseManager.findCourseByName(selectedTeacher, courseName);
            if (courseToRemove != null) {
                DatabaseManager.removeTeacherCourse(selectedTeacher, courseToRemove);
                showMessage("Course removed from teacher successfully.");
            } else {
                showError("Course not found.");
            }
        }
    }

    private void editTeacherOrCourse() {
        String teacherName = JOptionPane.showInputDialog("Enter teacher's full name:");
        Teacher selectedTeacher = DatabaseManager.findTeacherByName(teacherName);
        if (selectedTeacher == null) {
            showError("Teacher not found.");
            return;
        }

        String[] options = {"Edit Teacher", "Edit Course"};
        int choice = JOptionPane.showOptionDialog(null, "Choose action:", "Edit Teacher/Course", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            editTeacher(selectedTeacher);
        } else if (choice == 1) {
            editCourse(selectedTeacher);
        }
    }

    private void editTeacher(Teacher teacher) {
        String fullName = JOptionPane.showInputDialog("Enter new full name:");
        if (fullName == null || fullName.trim().isEmpty()) {
            showError("Invalid input. Full name cannot be empty.");
            return;
        }

        String ageStr = JOptionPane.showInputDialog("Enter new age:");
        if (ageStr == null || ageStr.trim().isEmpty()) {
            showError("Invalid input. Age cannot be empty.");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException ex) {
            showError("Invalid input. Age must be a valid number.");
            return;
        }

        teacher.setFullName(fullName);
        teacher.setAge(age);

        DatabaseManager.updateTeacher(teacher);

        showMessage("Teacher edited successfully.");
    }

    private void editCourse(Teacher teacher) {
        String courseName = JOptionPane.showInputDialog("Enter course name to edit:");
        Course selectedCourse = DatabaseManager.findCourseByName(teacher, courseName);
        if (selectedCourse == null) {
            showError("Course not found.");
            return;
        }

        String newCourseName = JOptionPane.showInputDialog("Enter new course name:");
        if (newCourseName == null || newCourseName.trim().isEmpty()) {
            showError("Invalid input. Course name cannot be empty.");
            return;
        }

        String hoursStr = JOptionPane.showInputDialog("Enter new course hours:");
        if (hoursStr == null || hoursStr.trim().isEmpty()) {
            showError("Invalid input. Hours cannot be empty.");
            return;
        }

        int newHours;
        try {
            newHours = Integer.parseInt(hoursStr);
        } catch (NumberFormatException ex) {
            showError("Invalid input. Hours must be a valid number.");
            return;
        }

        selectedCourse.setName(newCourseName);
        selectedCourse.setHours(newHours);

        DatabaseManager.updateCourse(selectedCourse);

        showMessage("Course edited successfully.");
    }

    private void showAllCourses() {
        List<Course> courses = DatabaseManager.loadAllCourses();
        StringBuilder output = new StringBuilder("Courses:\n");
        for (Course course : courses) {
            output.append("Name: ").append(course.getName()).append(", Hours: ").append(course.getHours()).append("\n");
        }
        textArea.setText(output.toString());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Message", JOptionPane.INFORMATION_MESSAGE);
    }

}

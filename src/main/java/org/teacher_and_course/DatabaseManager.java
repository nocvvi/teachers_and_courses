package org.teacher_and_course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:school.db";

    public static void createTables() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("SQLite JDBC driver not found");
            return;
        }
        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement()) {
            String createTeacherTable = "CREATE TABLE IF NOT EXISTS teachers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "full_name TEXT," +
                    "age INTEGER);";
            statement.executeUpdate(createTeacherTable);

            String createCourseTable = "CREATE TABLE IF NOT EXISTS courses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "hours INTEGER);";
            statement.executeUpdate(createCourseTable);

            String createTeacherCourseTable = "CREATE TABLE IF NOT EXISTS teacher_course (" +
                    "teacher_id INTEGER," +
                    "course_id INTEGER," +
                    "FOREIGN KEY (teacher_id) REFERENCES teachers (id) ON DELETE CASCADE," +
                    "FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE);";
            statement.executeUpdate(createTeacherCourseTable);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    public static int saveTeacher(Teacher teacher) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO teachers (full_name, age) VALUES (?, ?)")) {
            statement.setString(1, teacher.getFullName());
            statement.setInt(2, teacher.getAge());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating teacher failed, no rows affected.");
            }

            try (Statement idStatement = connection.createStatement()) {
                try (ResultSet resultSet = idStatement.executeQuery("SELECT last_insert_rowid()")) {
                    if (resultSet.next()) {
                        int teacherId = resultSet.getInt(1);
                        teacher.setId(teacherId);
                        return teacherId;
                    } else {
                        throw new SQLException("Creating teacher failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int saveCourse(Course course) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO courses (name, hours) VALUES (?, ?)")) {
            statement.setString(1, course.getName());
            statement.setInt(2, course.getHours());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating course failed, no rows affected.");
            }

            try (Statement idStatement = connection.createStatement()) {
                try (ResultSet resultSet = idStatement.executeQuery("SELECT last_insert_rowid()")) {
                    if (resultSet.next()) {
                        int courseId = resultSet.getInt(1);
                        course.setId(courseId);
                        return courseId;
                    } else {
                        throw new SQLException("Creating course failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void saveTeacherCourse(int teacherId, int courseId) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO teacher_course (teacher_id, course_id) VALUES (?, ?)")) {

            if (!teacherExists(connection, teacherId)) {
                System.err.println("Teacher with ID " + teacherId + " does not exist.");
                return;
            }

            if (!courseExists(connection, courseId)) {
                System.err.println("Course with ID " + courseId + " does not exist.");
                return;
            }

            statement.setInt(1, teacherId);
            statement.setInt(2, courseId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static boolean teacherExists(Connection connection, int teacherId) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM teachers WHERE id = ?")) {
            statement.setInt(1, teacherId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean courseExists(Connection connection, int courseId) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM courses WHERE id = ?")) {
            statement.setInt(1, courseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Teacher> loadAllTeachers() {
        List<Teacher> teachers = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement()) {
            String query = "SELECT * FROM teachers";
            try (ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    Teacher teacher = new Teacher(resultSet.getString("full_name"), resultSet.getInt("age"));
                    teacher.setId(resultSet.getInt("id"));
                    teachers.add(teacher);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return teachers;
    }

    public static List<Course> loadAllCourses() {
        List<Course> courses = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement()) {
            String query = "SELECT * FROM courses";
            try (ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    Course course = new Course(resultSet.getString("name"), resultSet.getInt("hours"));
                    course.setId(resultSet.getInt("id"));
                    courses.add(course);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }

    public static Teacher findTeacherByName(String name) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM teachers WHERE full_name = ?")) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Teacher teacher = new Teacher(resultSet.getString("full_name"), resultSet.getInt("age"));
                    teacher.setId(resultSet.getInt("id"));
                    return teacher;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Course findCourseByName(Teacher teacher, String name) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT courses.id, courses.name, courses.hours FROM courses " +
                             "JOIN teacher_course ON courses.id = teacher_course.course_id " +
                             "WHERE teacher_course.teacher_id = ? AND courses.name = ?")) {
            statement.setInt(1, teacher.getId());
            statement.setString(2, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Course course = new Course(resultSet.getString("name"), resultSet.getInt("hours"));
                    course.setId(resultSet.getInt("id"));
                    return course;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void removeTeacher(Teacher teacher) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement("DELETE FROM teachers WHERE id = ?")) {
            statement.setInt(1, teacher.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void removeCourse(Course course) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement("DELETE FROM courses WHERE id = ?")) {
            statement.setInt(1, course.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static List<Course> loadTeacherCourses(Teacher teacher) {
        List<Course> courses = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT courses.id, courses.name, courses.hours FROM courses " +
                             "JOIN teacher_course ON courses.id = teacher_course.course_id " +
                             "WHERE teacher_course.teacher_id = ?")) {
            statement.setInt(1, teacher.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Course course = new Course(resultSet.getString("name"), resultSet.getInt("hours"));
                    course.setId(resultSet.getInt("id"));
                    courses.add(course);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }
    public static void updateTeacher(Teacher teacher) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement("UPDATE teachers SET full_name = ?, age = ? WHERE id = ?")) {
            statement.setString(1, teacher.getFullName());
            statement.setInt(2, teacher.getAge());
            statement.setInt(3, teacher.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void updateCourse(Course course) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement("UPDATE courses SET name = ?, hours = ? WHERE id = ?")) {
            statement.setString(1, course.getName());
            statement.setInt(2, course.getHours());
            statement.setInt(3, course.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void removeTeacherCourse(Teacher teacher, Course course) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM teacher_course WHERE teacher_id = ? AND course_id = ?")) {
            statement.setInt(1, teacher.getId());
            statement.setInt(2, course.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void close() {
        try {
            DriverManager.getConnection(URL + ";shutdown=true");
        } catch (SQLException ignored) {
        }
    }

}

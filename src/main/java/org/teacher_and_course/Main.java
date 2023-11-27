package org.teacher_and_course;


public class Main {
    public static void main(String[] args) {
        DatabaseManager.createTables();
        SchoolManagementGUI schoolManagementGUI = new SchoolManagementGUI();
        schoolManagementGUI.showMainFrame();
    }
}

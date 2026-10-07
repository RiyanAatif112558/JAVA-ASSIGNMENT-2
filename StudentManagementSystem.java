import java.util.*;

// ---------- Student Class ---------- //
class Student {
    int id;
    String name;
    double marks;

    public Student(int id, String name, double marks) {
        this.id = id;
        this.name = name;
        this.marks = marks;
    }

    @Override
    public String toString() {
        return "ID: " + id + " | Name: " + name + " | Marks: " + marks;
    }
}

// ---------- School Manager Class (Using 4 Collections) ---------- //
class SchoolManager {
    // 1. LIST: Keeps an ordered list of all registered students
    private List<Student> studentList = new ArrayList<>();

    // 2. MAP: Allows fast searching/lookup of students by their ID
    private Map<Integer, Student> studentMap = new HashMap<>();

    // 3. SET: Stores unique course names (automatically prevents duplicates)
    private Set<String> courses = new HashSet<>();

    // 4. MAP of QUEUES: FIFO waiting list for each course if it gets full
    private Map<String, Queue<Student>> courseWaitingList = new HashMap<>();

    public void addStudent(int id, String name, double marks) {
        Student newStudent = new Student(id, name, marks);
        studentList.add(newStudent);
        studentMap.put(id, newStudent);
        System.out.println("Registered student: " + name);
    }

    public void addCourse(String courseName) {
        courses.add(courseName);
        courseWaitingList.put(courseName, new LinkedList<>()); // Initialize queue for this course
    }

    // Example of Queue usage: Joining a waiting list for a full course
    public void joinCourseWaitingList(int studentId, String courseName) {
        Student s = studentMap.get(studentId);
        if (s == null) {
            System.out.println("Student ID " + studentId + " not found.");
            return;
        }
        if (!courses.contains(courseName)) {
            System.out.println("Course '" + courseName + "' does not exist.");
            return;
        }

        // Add student to the end of the course queue (FIFO)
        Queue<Student> queue = courseWaitingList.get(courseName);
        queue.offer(s);
        System.out.println(s.name + " added to the waiting list for " + courseName + " (Position: " + queue.size() + ")");
    }

    public void displayAllStudents() {
        System.out.println("\n--- All Students (ArrayList) ---");
        for (Student s : studentList) {
            System.out.println(s);
        }
    }

    public void searchStudentById(int id) {
        System.out.println("\n--- Searching for Student ID: " + id + " (HashMap) ---");
        Student found = studentMap.get(id);
        if (found != null) {
            System.out.println("Found: " + found);
        } else {
            System.out.println("Student not found.");
        }
    }

    public void displayCourses() {
        System.out.println("\n--- Available Courses (HashSet) ---");
        for (String c : courses) {
            System.out.println("- " + c);
        }
    }

    public void displayWaitingList(String courseName) {
        Queue<Student> queue = courseWaitingList.get(courseName);
        System.out.println("\n--- Waiting List for " + courseName + " (Queue) ---");
        if (queue == null || queue.isEmpty()) {
            System.out.println("Waiting list is empty.");
            return;
        }
        int pos = 1;
        for (Student s : queue) {
            System.out.println(pos++ + ". " + s.name);
        }
    }
}

// ---------- Main Class ---------- //
public class StudentManagementSystem {
    public static void main(String[] args) {
        SchoolManager manager = new SchoolManager();

        // 1. Add courses (Set)
        manager.addCourse("Java Programming");
        manager.addCourse("Mathematics");
        manager.addCourse("Java Programming"); // Duplicate ignored by HashSet

        // 2. Register students (List & Map)
        manager.addStudent(101, "Arun", 85.5);
        manager.addStudent(102, "Priya", 92.0);
        manager.addStudent(103, "Karthik", 78.5);

        manager.displayAllStudents();

        // 3. Fast lookup using HashMap
        manager.searchStudentById(102);

        // 4. Queue demonstration: Students joining a course waiting list
        System.out.println("\n===== COURSE WAITING LIST (Queue) =====");
        manager.joinCourseWaitingList(101, "Java Programming");
        manager.joinCourseWaitingList(103, "Java Programming");

        manager.displayWaitingList("Java Programming");
        manager.displayCourses();
    }
}
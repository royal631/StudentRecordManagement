import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.*;
import java.util.concurrent.*;
import java.util.stream.*;
import java.nio.file.*;

public class UltimateStudentManager {
    private static final String DATA_FILE = "students.dat";
    private static final String BACKUP_FILE = "students_backup_";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final Map<Integer, Student> students = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final ScheduledExecutorService backupService = Executors.newSingleThreadScheduledExecutor();
    
    public UltimateStudentManager() {
        // Schedule automatic backups every 30 minutes
        backupService.scheduleAtFixedRate(this::createBackup, 30, 30, TimeUnit.MINUTES);
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }
    
    public void start() {
        loadFromFile();
        showMainMenu();
    }
    
    private void shutdown() {
        System.out.println("\nPerforming shutdown tasks...");
        saveToFile();
        createBackup();
        executor.shutdown();
        backupService.shutdown();
        System.out.println("System shutdown complete.");
    }

    private void showMainMenu() {
        Scanner scanner = new Scanner(System.in);
        int choice;
        
        do {
            System.out.println("\n=== ULTIMATE STUDENT MANAGEMENT SYSTEM ===");
            System.out.println("1. Add Student");
            System.out.println("2. View All Students");
            System.out.println("3. Search Student");
            System.out.println("4. Update Student");
            System.out.println("5. Delete Student");
            System.out.println("6. Generate Report");
            System.out.println("7. Backup Data");
            System.out.println("8. Restore Backup");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // consume newline
                
                switch (choice) {
                    case 1 -> addStudent(scanner);
                    case 2 -> displayAllStudents();
                    case 3 -> searchStudent(scanner);
                    case 4 -> updateStudent(scanner);
                    case 5 -> deleteStudent(scanner);
                    case 6 -> generateReport();
                    case 7 -> createBackup();
                    case 8 -> restoreBackup(scanner);
                    case 0 -> {
                        System.out.println("Exiting system...");
                        return;
                    }
                    default -> System.out.println("❌ Invalid choice!");
                }
            } catch (InputMismatchException e) {
                System.out.println("❌ Please enter a valid number!");
                scanner.nextLine(); // clear invalid input
                choice = -1;
            }
        } while (true);
    }

    private void addStudent(Scanner scanner) {
        System.out.println("\n=== ADD NEW STUDENT ===");
        
        int rollNo = getValidRollNo(scanner);
        if (rollNo == -1) return;
        
        String name = getValidName(scanner);
        if (name == null) return;
        
        int age = getValidAge(scanner);
        if (age == -1) return;
        
        String course = getValidCourse(scanner);
        if (course == null) return;
        
        String email = getValidEmail(scanner);
        String phone = getValidPhone(scanner);
        
        Student student = new Student(rollNo, name, age, course, email, phone);
        students.put(rollNo, student);
        
        // Save in background
        executor.submit(this::saveToFile);
        
        System.out.println("\n✅ Student added successfully!");
        System.out.println(student);
    }
    
    private int getValidRollNo(Scanner scanner) {
        while (true) {
            System.out.print("Enter Roll No (0 to cancel): ");
            try {
                int rollNo = scanner.nextInt();
                scanner.nextLine();
                
                if (rollNo == 0) return -1;
                if (rollNo < 1) {
                    System.out.println("❌ Roll No must be positive!");
                    continue;
                }
                if (students.containsKey(rollNo)) {
                    System.out.println("❌ Student with this Roll No already exists!");
                    continue;
                }
                return rollNo;
            } catch (InputMismatchException e) {
                System.out.println("❌ Please enter a valid number!");
                scanner.nextLine();
            }
        }
    }
    
    private String getValidName(Scanner scanner) {
        while (true) {
            System.out.print("Enter Full Name (0 to cancel): ");
            String name = scanner.nextLine().trim();
            
            if (name.equals("0")) return null;
            if (name.isEmpty()) {
                System.out.println("❌ Name cannot be empty!");
                continue;
            }
            if (!name.matches("[a-zA-Z ]+")) {
                System.out.println("❌ Name can only contain letters and spaces!");
                continue;
            }
            return name;
        }
    }
    
    private int getValidAge(Scanner scanner) {
        while (true) {
            System.out.print("Enter Age (15-100, 0 to cancel): ");
            try {
                int age = scanner.nextInt();
                scanner.nextLine();
                
                if (age == 0) return -1;
                if (age < 15 || age > 100) {
                    System.out.println("❌ Age must be between 15 and 100!");
                    continue;
                }
                return age;
            } catch (InputMismatchException e) {
                System.out.println("❌ Please enter a valid number!");
                scanner.nextLine();
            }
        }
    }
    
    private String getValidCourse(Scanner scanner) {
        final String[] COURSES = {"BTECH", "MTECH", "MBA", "BCA", "MCA", "OTHER"};
        
        while (true) {
            System.out.println("Available Courses: " + Arrays.toString(COURSES));
            System.out.print("Enter Course (0 to cancel): ");
            String course = scanner.nextLine().trim().toUpperCase();
            
            if (course.equals("0")) return null;
            if (course.isEmpty()) {
                System.out.println("❌ Course cannot be empty!");
                continue;
            }
            if (!Arrays.asList(COURSES).contains(course)) {
                System.out.println("⚠️ Course not in standard list, but accepting anyway");
            }
            return course;
        }
    }
    
    private String getValidEmail(Scanner scanner) {
        while (true) {
            System.out.print("Enter Email (optional, 0 to skip): ");
            String email = scanner.nextLine().trim();
            
            if (email.equals("0") || email.isEmpty()) return "";
            if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                System.out.println("❌ Invalid email format!");
                continue;
            }
            return email;
        }
    }
    
    private String getValidPhone(Scanner scanner) {
        while (true) {
            System.out.print("Enter Phone (optional, 0 to skip): ");
            String phone = scanner.nextLine().trim();
            
            if (phone.equals("0") || phone.isEmpty()) return "";
            if (!phone.matches("^[0-9]{10,15}$")) {
                System.out.println("❌ Phone must be 10-15 digits!");
                continue;
            }
            return phone;
        }
    }

    private void displayAllStudents() {
        if (students.isEmpty()) {
            System.out.println("\n⚠️ No students found in the system.");
            return;
        }
        
        System.out.println("\n=== ALL STUDENTS ===");
        System.out.printf("%-10s %-20s %-5s %-10s %-25s %-15s%n", 
            "Roll No", "Name", "Age", "Course", "Email", "Phone");
        System.out.println("-".repeat(85));
        
        students.values().stream()
            .sorted(Comparator.comparingInt(Student::getRollNo))
            .forEach(s -> System.out.printf("%-10d %-20s %-5d %-10s %-25s %-15s%n",
                s.getRollNo(), s.getName(), s.getAge(), s.getCourse(), 
                s.getEmail(), s.getPhone()));
    }

    private void searchStudent(Scanner scanner) {
        System.out.println("\n=== SEARCH STUDENT ===");
        System.out.println("1. By Roll No");
        System.out.println("2. By Name");
        System.out.println("3. By Course");
        System.out.print("Enter search type: ");
        
        try {
            int type = scanner.nextInt();
            scanner.nextLine();
            
            switch (type) {
                case 1 -> searchByRollNo(scanner);
                case 2 -> searchByName(scanner);
                case 3 -> searchByCourse(scanner);
                default -> System.out.println("❌ Invalid search type!");
            }
        } catch (InputMismatchException e) {
            System.out.println("❌ Please enter a valid number!");
            scanner.nextLine();
        }
    }
    
    private void searchByRollNo(Scanner scanner) {
        System.out.print("Enter Roll No: ");
        try {
            int rollNo = scanner.nextInt();
            scanner.nextLine();
            
            Student student = students.get(rollNo);
            if (student != null) {
                System.out.println("\n🎯 Student Found:");
                System.out.println(student);
            } else {
                System.out.println("❌ Student not found!");
            }
        } catch (InputMismatchException e) {
            System.out.println("❌ Please enter a valid Roll No!");
            scanner.nextLine();
        }
    }
    
    private void searchByName(Scanner scanner) {
        System.out.print("Enter Name (partial match): ");
        String query = scanner.nextLine().trim().toLowerCase();
        
        List<Student> results = students.values().stream()
            .filter(s -> s.getName().toLowerCase().contains(query))
            .sorted(Comparator.comparing(Student::getName))
            .collect(Collectors.toList());
        
        displaySearchResults(results, "name containing '" + query + "'");
    }
    
    private void searchByCourse(Scanner scanner) {
        System.out.print("Enter Course: ");
        String query = scanner.nextLine().trim().toUpperCase();
        
        List<Student> results = students.values().stream()
            .filter(s -> s.getCourse().equalsIgnoreCase(query))
            .sorted(Comparator.comparing(Student::getName))
            .collect(Collectors.toList());
        
        displaySearchResults(results, "course '" + query + "'");
    }
    
    private void displaySearchResults(List<Student> results, String criteria) {
        if (results.isEmpty()) {
            System.out.println("\n⚠️ No students found with " + criteria);
            return;
        }
        
        System.out.printf("\n🔍 Search Results (%d students with %s):%n", results.size(), criteria);
        results.forEach(s -> System.out.printf("%-10d %-20s %-5d %-10s%n", 
            s.getRollNo(), s.getName(), s.getAge(), s.getCourse()));
    }

    private void updateStudent(Scanner scanner) {
        System.out.println("\n=== UPDATE STUDENT ===");
        System.out.print("Enter Roll No to update: ");
        
        try {
            int rollNo = scanner.nextInt();
            scanner.nextLine();
            
            Student student = students.get(rollNo);
            if (student == null) {
                System.out.println("❌ Student not found!");
                return;
            }
            
            System.out.println("\nCurrent Details:");
            System.out.println(student);
            
            System.out.println("\nEnter new details (leave blank to keep current):");
            
            // Update name
            System.out.print("Name [" + student.getName() + "]: ");
            String name = scanner.nextLine().trim();
            if (!name.isEmpty()) {
                student.setName(name);
            }
            
            // Update age
            System.out.print("Age [" + student.getAge() + "]: ");
            String ageInput = scanner.nextLine().trim();
            if (!ageInput.isEmpty()) {
                try {
                    int age = Integer.parseInt(ageInput);
                    if (age < 15 || age > 100) {
                        System.out.println("⚠️ Age not updated - must be between 15-100");
                    } else {
                        student.setAge(age);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Invalid age format - age not updated");
                }
            }
            
            // Update course
            System.out.print("Course [" + student.getCourse() + "]: ");
            String course = scanner.nextLine().trim().toUpperCase();
            if (!course.isEmpty()) {
                student.setCourse(course);
            }
            
            // Update email
            System.out.print("Email [" + student.getEmail() + "]: ");
            String email = scanner.nextLine().trim();
            if (!email.isEmpty()) {
                if (email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                    student.setEmail(email);
                } else {
                    System.out.println("⚠️ Invalid email format - email not updated");
                }
            }
            
            // Update phone
            System.out.print("Phone [" + student.getPhone() + "]: ");
            String phone = scanner.nextLine().trim();
            if (!phone.isEmpty()) {
                if (phone.matches("^[0-9]{10,15}$")) {
                    student.setPhone(phone);
                } else {
                    System.out.println("⚠️ Phone must be 10-15 digits - phone not updated");
                }
            }
            
            // Save changes
            student.setLastUpdated(LocalDateTime.now());
            executor.submit(this::saveToFile);
            
            System.out.println("\n✅ Student updated successfully!");
            System.out.println(student);
            
        } catch (InputMismatchException e) {
            System.out.println("❌ Please enter a valid Roll No!");
            scanner.nextLine();
        }
    }

    private void deleteStudent(Scanner scanner) {
        System.out.println("\n=== DELETE STUDENT ===");
        System.out.print("Enter Roll No to delete: ");
        
        try {
            int rollNo = scanner.nextInt();
            scanner.nextLine();
            
            if (!students.containsKey(rollNo)) {
                System.out.println("❌ Student not found!");
                return;
            }
            
            System.out.println("\nStudent to be deleted:");
            System.out.println(students.get(rollNo));
            
            System.out.print("\nAre you sure you want to delete? (Y/N): ");
            String confirm = scanner.nextLine().trim().toUpperCase();
            
            if (confirm.equals("Y")) {
                students.remove(rollNo);
                executor.submit(this::saveToFile);
                System.out.println("🗑️ Student deleted successfully!");
            } else {
                System.out.println("Deletion cancelled.");
            }
        } catch (InputMismatchException e) {
            System.out.println("❌ Please enter a valid Roll No!");
            scanner.nextLine();
        }
    }

    private void generateReport() {
        System.out.println("\n📊 === STUDENT SYSTEM REPORT ===");
        System.out.println("Total Students: " + students.size());
        
        if (students.isEmpty()) return;
        
        // Age statistics
        IntSummaryStatistics ageStats = students.values().stream()
            .mapToInt(Student::getAge)
            .summaryStatistics();
        
        System.out.println("\nAge Statistics:");
        System.out.println("Youngest: " + ageStats.getMin());
        System.out.println("Oldest: " + ageStats.getMax());
        System.out.printf("Average: %.1f%n", ageStats.getAverage());
        
        // Course distribution
        System.out.println("\nCourse Distribution:");
        students.values().stream()
            .collect(Collectors.groupingBy(Student::getCourse, Collectors.counting()))
            .forEach((course, count) -> System.out.printf("%-10s: %d students%n", course, count));
        
        // Last updated
        Optional<LocalDateTime> lastUpdate = students.values().stream()
            .map(Student::getLastUpdated)
            .max(LocalDateTime::compareTo);
        
        lastUpdate.ifPresent(date -> 
            System.out.println("\nLast Student Update: " + date.format(DATE_FORMAT)));
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(students);
        } catch (IOException e) {
            System.out.println("❌ Error saving data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                students.clear();
                students.putAll((Map<Integer, Student>) obj);
                System.out.println("✅ Loaded " + students.size() + " student records");
            }
        } catch (FileNotFoundException e) {
            // First run - no file yet
        } catch (Exception e) {
            System.out.println("❌ Error loading data: " + e.getMessage());
        }
    }
    
    private void createBackup() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFile = BACKUP_FILE + timestamp + ".dat";
        
        try {
            Files.copy(Paths.get(DATA_FILE), Paths.get(backupFile), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Backup created: " + backupFile);
        } catch (IOException e) {
            System.out.println("❌ Error creating backup: " + e.getMessage());
        }
    }
    
    private void restoreBackup(Scanner scanner) {
        try {
            File backupDir = new File(".");
            File[] backups = backupDir.listFiles((_, name) -> name.startsWith(BACKUP_FILE) && name.endsWith(".dat"));
            
            if (backups == null || backups.length == 0) {
                System.out.println("❌ No backup files found!");
                return;
            }
            
            Arrays.sort(backups, Comparator.comparingLong(File::lastModified).reversed());
            
            System.out.println("\nAvailable Backups:");
            for (int i = 0; i < backups.length; i++) {
                System.out.printf("%d. %s (%.1f KB)%n",
                    i + 1, backups[i].getName(), backups[i].length() / 1024.0);
            }
            
            System.out.print("\nEnter backup number to restore (0 to cancel): ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            if (choice > 0 && choice <= backups.length) {
                System.out.print("Are you sure you want to restore from " + backups[choice-1].getName() + "? (Y/N): ");
                String confirm = scanner.nextLine().trim().toUpperCase();
                
                if (confirm.equals("Y")) {
                    Files.copy(backups[choice-1].toPath(), Paths.get(DATA_FILE), StandardCopyOption.REPLACE_EXISTING);
                    loadFromFile();
                    System.out.println("✅ Restore completed successfully!");
                } else {
                    System.out.println("Restore cancelled.");
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error during restore: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new UltimateStudentManager().start();
    }
}

class Student implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final int rollNo;
    private String name;
    private int age;
    private String course;
    private String email;
    private String phone;
    private final LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    
    public Student(int rollNo, String name, int age, String course, String email, String phone) {
        this.rollNo = rollNo;
        this.name = name;
        this.age = age;
        this.course = course;
        this.email = email;
        this.phone = phone;
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Getters
    public int getRollNo() { return rollNo; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getCourse() { return course; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    
    // Setters with validation
    public void setName(String name) { 
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
            setLastUpdated(LocalDateTime.now());
        }
    }
    
    public void setAge(int age) {
        if (age >= 15 && age <= 100) {
            this.age = age;
            setLastUpdated(LocalDateTime.now());
        }
    }
    
    public void setCourse(String course) {
        if (course != null && !course.trim().isEmpty()) {
            this.course = course.trim().toUpperCase();
            setLastUpdated(LocalDateTime.now());
        }
    }
    
    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty() || 
            email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            this.email = email != null ? email.trim() : "";
            setLastUpdated(LocalDateTime.now());
        }
    }
    
    public void setPhone(String phone) {
        if (phone == null || phone.trim().isEmpty() || 
            phone.matches("^[0-9]{10,15}$")) {
            this.phone = phone != null ? phone.trim() : "";
            setLastUpdated(LocalDateTime.now());
        }
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    @Override
    public String toString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return String.format(
            "Student [Roll No: %d, Name: %s, Age: %d, Course: %s, Email: %s, Phone: %s]\n" +
            "Created: %s, Last Updated: %s",
            rollNo, name, age, course, email, phone,
            createdAt.format(dtf), lastUpdated.format(dtf));
    }
}
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.time.*;
import java.time.format.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class Main {
    private static final String DATA_FILE = "students.dat";
    private static final String BACKUP_FILE = "students_backup_";
    private static final String REPORT_FILE = "student_report_";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Scanner scanner = new Scanner(System.in);
    
    private final Map<Integer, Student> students;
    private final ExecutorService executorService;
    private final ScheduledExecutorService backupService;
    
    public Main() {
        this.students = new ConcurrentHashMap<>();
        this.executorService = Executors.newFixedThreadPool(3);
        this.backupService = Executors.newSingleThreadScheduledExecutor();
        
        // Schedule periodic backups every 30 minutes
        backupService.scheduleAtFixedRate(this::createBackup, 30, 30, TimeUnit.MINUTES);
    }
    
    public static void main(String[] args) {
        Main system = new Main();
        system.initialize();
        
        try {
            system.run();
        } finally {
            system.shutdown();
        }
    }
    
    private void initialize() {
        loadFromFile();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nPerforming shutdown tasks...");
            saveToFile();
            createBackup();
            System.out.println("All data saved successfully.");
        }));
    }
    
    private void shutdown() {
        try {
            executorService.shutdown();
            backupService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if (!backupService.awaitTermination(5, TimeUnit.SECONDS)) {
                backupService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            backupService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        // Do not close the scanner here to avoid closing System.in prematurely
    }
    
    private void run() {
        int choice;
        do {
            displayMenu();
            try {
                choice = Integer.parseInt(scanner.nextLine());
                processChoice(choice);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number.");
                choice = -1;
            }
        } while (choice != 0);
    }
    
    private void displayMenu() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║    STUDENT RECORD MANAGEMENT SYSTEM    ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ 1. Add Student                        ║");
        System.out.println("║ 2. View All Students                  ║");
        System.out.println("║ 3. Search Student                     ║");
        System.out.println("║ 4. Update Student                     ║");
        System.out.println("║ 5. Delete Student                     ║");
        System.out.println("║ 6. View Statistics                    ║");
        System.out.println("║ 7. Generate Report                    ║");
        System.out.println("║ 8. Create Backup                      ║");
        System.out.println("║ 9. Restore from Backup                ║");
        System.out.println("║ 0. Exit                               ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.print("Enter your choice: ");
    }
    
    private void processChoice(int choice) {
        switch (choice) {
            case 1 -> addStudent();
            case 2 -> displayAllStudents();
            case 3 -> searchStudent();
            case 4 -> updateStudent();
            case 5 -> deleteStudent();
            case 6 -> displayStatistics();
            case 7 -> generateReport();
            case 8 -> createBackup();
            case 9 -> restoreFromBackup();
            case 0 -> {
                System.out.println("\nSaving data and exiting...");
                saveToFile();
                System.out.println("Thank you for using the system!");
            }
            default -> System.out.println("Invalid choice! Please try again.");
        }
    }
    
    // Student class with enhanced features
    private static class Student implements Serializable {
        private static final long serialVersionUID = 1L;
        private static final DateTimeFormatter STUDENT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        private final int id;
        private String name;
        private int age;
        private String grade;
        private String email;
        private String phone;
        private final LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<String> attendance;
        private Map<String, Double> grades;

        public Student(int id, String name, int age, String grade, String email, String phone) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.grade = grade;
            this.email = email;
            this.phone = phone;
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
            this.attendance = new ArrayList<>();
            this.grades = new HashMap<>();
        }

        // Getters and setters
        public int getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; updateTimestamp(); }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; updateTimestamp(); }
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; updateTimestamp(); }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; updateTimestamp(); }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; updateTimestamp(); }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public List<String> getAttendance() { return new ArrayList<>(attendance); }
        public Map<String, Double> getGrades() { return new HashMap<>(grades); }

        private void updateTimestamp() {
            this.updatedAt = LocalDateTime.now();
        }

        public void markAttendance(String status) {
            attendance.add(LocalDate.now() + ": " + status);
            updateTimestamp();
        }

        public void addGrade(String subject, double score) {
            grades.put(subject, score);
            updateTimestamp();
        }

        public double calculateAverageGrade() {
            if (grades.isEmpty()) return 0.0;
            return grades.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }

        @Override
        public String toString() {
            return String.format(
                "ID: %d\nName: %s\nAge: %d\nGrade: %s\nEmail: %s\nPhone: %s\n" +
                "Created: %s\nLast Updated: %s\nAttendance: %d records\nGrades: %d subjects (Avg: %.2f)",
                id, name, age, grade, email, phone,
                createdAt.format(STUDENT_DATE_FORMAT), updatedAt.format(STUDENT_DATE_FORMAT),
                attendance.size(), grades.size(), calculateAverageGrade()
            );
        }
    }
    
    // Main operations
    private void addStudent() {
        try {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║            ADD NEW STUDENT             ║");
            System.out.println("╚════════════════════════════════════════╝");
            
            int id = getNextStudentId();
            System.out.print("Enter student name: ");
            String name = scanner.nextLine();
            
            int age = 0;
            while (age <= 0) {
                System.out.print("Enter student age: ");
                try {
                    age = Integer.parseInt(scanner.nextLine());
                    if (age <= 0) {
                        System.out.println("Age must be positive!");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid age! Please enter a number.");
                }
            }
            
            System.out.print("Enter student grade/class: ");
            String grade = scanner.nextLine();
            
            System.out.print("Enter student email: ");
            String email = scanner.nextLine();
            
            System.out.print("Enter student phone: ");
            String phone = scanner.nextLine();
            
            Student student = new Student(id, name, age, grade, email, phone);
            students.put(id, student);
            
            System.out.println("\nStudent added successfully!");
            System.out.println("Generated Student ID: " + id);
            System.out.println("\nStudent Details:");
            System.out.println(student);
            
            // Save in background
            executorService.submit(this::saveToFile);
        } catch (Exception e) {
            System.out.println("Error adding student: " + e.getMessage());
        }
    }
    
    private void displayAllStudents() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║          ALL STUDENT RECORDS           ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        if (students.isEmpty()) {
            System.out.println("No students found in the system.");
            return;
        }
        
        // Sort students by ID
        List<Student> sortedStudents = students.values().stream()
            .sorted(Comparator.comparingInt(Student::getId))
            .collect(Collectors.toList());
        
        // Display in paginated format
        int pageSize = 5;
        int totalPages = (int) Math.ceil((double) sortedStudents.size() / pageSize);
        int currentPage = 0;
        
        while (currentPage < totalPages) {
            System.out.printf("\nPage %d of %d\n", currentPage + 1, totalPages);
            System.out.println("════════════════════════════════════════");
            
            int start = currentPage * pageSize;
            int end = Math.min(start + pageSize, sortedStudents.size());
            
            for (int i = start; i < end; i++) {
                Student student = sortedStudents.get(i);
                System.out.printf("%d. %s (ID: %d, Grade: %s)\n", 
                    i + 1, student.getName(), student.getId(), student.getGrade());
            }
            
            System.out.println("\nOptions:");
            System.out.println("N - Next Page | P - Previous Page | V - View Details | M - Main Menu");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().toUpperCase();
            
            switch (choice) {
                case "N":
                    if (currentPage < totalPages - 1) {
                        currentPage++;
                    } else {
                        System.out.println("You're on the last page!");
                    }
                    break;
                case "P":
                    if (currentPage > 0) {
                        currentPage--;
                    } else {
                        System.out.println("You're on the first page!");
                    }
                    break;
                case "V":
                    System.out.print("Enter student number to view details: ");
                    try {
                        int studentNum = Integer.parseInt(scanner.nextLine());
                        if (studentNum >= start + 1 && studentNum <= end) {
                            Student selected = sortedStudents.get(studentNum - 1);
                            displayStudentDetails(selected);
                        } else {
                            System.out.println("Invalid student number!");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid number!");
                    }
                    break;
                case "M":
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
    
    private void displayStudentDetails(Student student) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║          STUDENT DETAILS               ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println(student);
        
        // Display attendance if available
        if (!student.getAttendance().isEmpty()) {
            System.out.println("\nAttendance Records:");
            student.getAttendance().forEach(System.out::println);
        }
        
        // Display grades if available
        if (!student.getGrades().isEmpty()) {
            System.out.println("\nSubject Grades:");
            student.getGrades().forEach((subject, grade) -> 
                System.out.printf("%s: %.2f\n", subject, grade));
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    private void searchStudent() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           SEARCH STUDENT              ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        System.out.println("Search by:");
        System.out.println("1. ID");
        System.out.println("2. Name");
        System.out.println("3. Grade/Class");
        System.out.println("4. Email");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter choice: ");
        
        try {
            int searchChoice = Integer.parseInt(scanner.nextLine());
            
            switch (searchChoice) {
                case 1 -> searchById();
                case 2 -> searchByName();
                case 3 -> searchByGrade();
                case 4 -> searchByEmail();
                case 0 -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter a number.");
        }
    }
    
    private void searchById() {
        System.out.print("Enter student ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            Student student = students.get(id);
            if (student != null) {
                displayStudentDetails(student);
            } else {
                System.out.println("Student with ID " + id + " not found!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format! Please enter a number.");
        }
    }
    
    private void searchByName() {
        System.out.print("Enter student name (or part of name): ");
        String nameQuery = scanner.nextLine().toLowerCase();
        
        List<Student> matches = students.values().stream()
            .filter(s -> s.getName().toLowerCase().contains(nameQuery))
            .sorted(Comparator.comparing(Student::getName))
            .collect(Collectors.toList());
        
        displaySearchResults(matches, "name containing '" + nameQuery + "'");
    }
    
    private void searchByGrade() {
        System.out.print("Enter grade/class: ");
        String gradeQuery = scanner.nextLine();
        
        List<Student> matches = students.values().stream()
            .filter(s -> s.getGrade().equalsIgnoreCase(gradeQuery))
            .sorted(Comparator.comparing(Student::getName))
            .collect(Collectors.toList());
        
        displaySearchResults(matches, "in grade '" + gradeQuery + "'");
    }
    
    private void searchByEmail() {
        System.out.print("Enter email (or part of email): ");
        String emailQuery = scanner.nextLine().toLowerCase();
        
        List<Student> matches = students.values().stream()
            .filter(s -> s.getEmail().toLowerCase().contains(emailQuery))
            .sorted(Comparator.comparing(Student::getName))
            .collect(Collectors.toList());
        
        displaySearchResults(matches, "with email containing '" + emailQuery + "'");
    }
    
    private void displaySearchResults(List<Student> results, String criteria) {
        System.out.println("\nSearch Results (" + results.size() + " students found " + criteria + "):");
        if (results.isEmpty()) {
            System.out.println("No matching students found.");
            return;
        }
        
        results.forEach(student -> 
            System.out.printf("ID: %d | Name: %-20s | Grade: %-5s | Email: %s\n",
                student.getId(), student.getName(), student.getGrade(), student.getEmail()));
        
        System.out.print("\nEnter student ID to view details (0 to cancel): ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            if (id != 0) {
                Student student = students.get(id);
                if (student != null && results.contains(student)) {
                    displayStudentDetails(student);
                } else {
                    System.out.println("Invalid ID or student not in search results!");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input!");
        }
    }
    
    private void updateStudent() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           UPDATE STUDENT              ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        System.out.print("Enter student ID to update: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            Student student = students.get(id);
            
            if (student == null) {
                System.out.println("Student with ID " + id + " not found!");
                return;
            }
            
            System.out.println("\nCurrent Student Details:");
            System.out.println(student);
            
            System.out.println("\nSelect field to update:");
            System.out.println("1. Name");
            System.out.println("2. Age");
            System.out.println("3. Grade");
            System.out.println("4. Email");
            System.out.println("5. Phone");
            System.out.println("6. Mark Attendance");
            System.out.println("7. Add Grade");
            System.out.println("0. Cancel");
            System.out.print("Enter choice: ");
            
            int fieldChoice = Integer.parseInt(scanner.nextLine());
            
            switch (fieldChoice) {
                case 1 -> {
                    System.out.print("Enter new name: ");
                    student.setName(scanner.nextLine());
                }
                case 2 -> {
                    System.out.print("Enter new age: ");
                    student.setAge(Integer.parseInt(scanner.nextLine()));
                }
                case 3 -> {
                    System.out.print("Enter new grade: ");
                    student.setGrade(scanner.nextLine());
                }
                case 4 -> {
                    System.out.print("Enter new email: ");
                    student.setEmail(scanner.nextLine());
                }
                case 5 -> {
                    System.out.print("Enter new phone: ");
                    student.setPhone(scanner.nextLine());
                }
                case 6 -> {
                    System.out.println("Attendance options:");
                    System.out.println("P - Present");
                    System.out.println("A - Absent");
                    System.out.println("L - Late");
                    System.out.print("Enter status: ");
                    String status = scanner.nextLine().toUpperCase();
                    
                    switch (status) {
                        case "P" -> student.markAttendance("Present");
                        case "A" -> student.markAttendance("Absent");
                        case "L" -> student.markAttendance("Late");
                        default -> System.out.println("Invalid status!");
                    }
                }
                case 7 -> {
                    System.out.print("Enter subject name: ");
                    String subject = scanner.nextLine();
                    System.out.print("Enter grade (0-100): ");
                    double grade = Double.parseDouble(scanner.nextLine());
                    student.addGrade(subject, grade);
                }
                case 0 -> {
                    System.out.println("Update cancelled.");
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
            
            System.out.println("\nStudent record updated successfully!");
            System.out.println("\nUpdated Details:");
            System.out.println(student);
            
            // Save in background
            executorService.submit(this::saveToFile);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input format!");
        } catch (Exception e) {
            System.out.println("Error updating student: " + e.getMessage());
        }
    }
    
    private void deleteStudent() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           DELETE STUDENT              ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        System.out.print("Enter student ID to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            Student student = students.get(id);
            
            if (student == null) {
                System.out.println("Student with ID " + id + " not found!");
                return;
            }
            
            System.out.println("\nStudent to be deleted:");
            System.out.println(student);
            
            System.out.print("\nAre you sure you want to delete this student? (Y/N): ");
            String confirmation = scanner.nextLine().toUpperCase();
            
            if (confirmation.equals("Y")) {
                students.remove(id);
                System.out.println("Student deleted successfully!");
                
                // Save in background
                executorService.submit(this::saveToFile);
            } else {
                System.out.println("Deletion cancelled.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format! Please enter a number.");
        }
    }
    
    private void displayStatistics() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║          SYSTEM STATISTICS            ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        int totalStudents = students.size();
        System.out.println("Total Students: " + totalStudents);
        
        if (totalStudents == 0) return;
        
        // Age statistics
        IntSummaryStatistics ageStats = students.values().stream()
            .mapToInt(Student::getAge)
            .summaryStatistics();
        
        System.out.println("\nAge Statistics:");
        System.out.println("Youngest: " + ageStats.getMin());
        System.out.println("Oldest: " + ageStats.getMax());
        System.out.printf("Average: %.1f\n", ageStats.getAverage());
        
        // Grade distribution
        System.out.println("\nGrade Distribution:");
        students.values().stream()
            .collect(Collectors.groupingBy(Student::getGrade, Collectors.counting()))
            .forEach((grade, count) -> System.out.printf("%-5s: %d students\n", grade, count));
        
        // Attendance summary
        long totalAttendanceRecords = students.values().stream()
            .mapToLong(s -> s.getAttendance().size())
            .sum();
        
        System.out.println("\nTotal Attendance Records: " + totalAttendanceRecords);
        
        // Grade performance
        System.out.println("\nTop Performing Students (by average grade):");
        students.values().stream()
            .filter(s -> !s.getGrades().isEmpty())
            .sorted(Comparator.comparingDouble(Student::calculateAverageGrade).reversed())
            .limit(5)
            .forEach(s -> System.out.printf("%-20s: %.2f average\n", s.getName(), s.calculateAverageGrade()));
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    private void generateReport() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║          GENERATE REPORT              ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        System.out.println("Report Types:");
        System.out.println("1. Full Student List");
        System.out.println("2. Students by Grade");
        System.out.println("3. Attendance Summary");
        System.out.println("4. Grade Performance");
        System.out.println("0. Cancel");
        System.out.print("Enter choice: ");
        
        try {
            int reportChoice = Integer.parseInt(scanner.nextLine());
            String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
            String reportFile = REPORT_FILE + timestamp + ".txt";
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile))) {
                writer.println("STUDENT MANAGEMENT SYSTEM REPORT");
                writer.println("Generated on: " + LocalDateTime.now().format(DATE_FORMAT));
                writer.println("Total Students: " + students.size());
                writer.println("════════════════════════════════════════");
                
                switch (reportChoice) {
                    case 1 -> generateFullReport(writer);
                    case 2 -> generateGradeReport(writer);
                    case 3 -> generateAttendanceReport(writer);
                    case 4 -> generatePerformanceReport(writer);
                    case 0 -> {
                        System.out.println("Report generation cancelled.");
                        return;
                    }
                    default -> {
                        System.out.println("Invalid choice!");
                        return;
                    }
                }
                
                System.out.println("\nReport generated successfully!");
                System.out.println("Saved as: " + reportFile);
            } catch (IOException e) {
                System.out.println("Error generating report: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input!");
        }
    }
    
    private void generateFullReport(PrintWriter writer) {
        writer.println("FULL STUDENT LIST");
        writer.println("════════════════════════════════════════");
        
        students.values().stream()
            .sorted(Comparator.comparingInt(Student::getId))
            .forEach(student -> {
                writer.println("\nStudent ID: " + student.getId());
                writer.println("Name: " + student.getName());
                writer.println("Age: " + student.getAge());
                writer.println("Grade: " + student.getGrade());
                writer.println("Email: " + student.getEmail());
                writer.println("Phone: " + student.getPhone());
                writer.println("Created: " + student.getCreatedAt().format(DATE_FORMAT));
                writer.println("Last Updated: " + student.getUpdatedAt().format(DATE_FORMAT));
                
                if (!student.getGrades().isEmpty()) {
                    writer.println("\nGrades:");
                    student.getGrades().forEach((subject, grade) -> 
                        writer.printf("  %-15s: %.2f\n", subject, grade));
                    writer.printf("Average Grade: %.2f\n", student.calculateAverageGrade());
                }
                
                if (!student.getAttendance().isEmpty()) {
                    writer.println("\nAttendance Records:");
                    student.getAttendance().forEach(writer::println);
                }
                
                writer.println("────────────────────────────────────────");
            });
    }
    
    private void generateGradeReport(PrintWriter writer) {
        writer.println("STUDENTS BY GRADE");
        writer.println("════════════════════════════════════════");
        
        students.values().stream()
            .collect(Collectors.groupingBy(Student::getGrade))
            .forEach((grade, studentList) -> {
                writer.println("\nGrade: " + grade);
                writer.println("Number of Students: " + studentList.size());
                writer.println("────────────────────────────────────────");
                
                studentList.forEach(student -> 
                    writer.printf("ID: %-5d | Name: %-20s | Email: %s\n",
                        student.getId(), student.getName(), student.getEmail()));
                
                writer.println("────────────────────────────────────────");
            });
    }
    
    private void generateAttendanceReport(PrintWriter writer) {
        writer.println("ATTENDANCE SUMMARY");
        writer.println("════════════════════════════════════════");
        
        long totalRecords = students.values().stream()
            .mapToLong(s -> s.getAttendance().size())
            .sum();
        
        writer.println("Total Attendance Records: " + totalRecords);
        writer.println("\nStudents with Most Absences:");
        
        students.values().stream()
            .filter(s -> !s.getAttendance().isEmpty())
            .sorted((s1, s2) -> {
                long absent1 = s1.getAttendance().stream().filter(a -> a.contains("Absent")).count();
                long absent2 = s2.getAttendance().stream().filter(a -> a.contains("Absent")).count();
                return Long.compare(absent2, absent1);
            })
            .limit(5)
            .forEach(student -> {
                long total = student.getAttendance().size();
                long present = student.getAttendance().stream().filter(a -> a.contains("Present")).count();
                long absent = student.getAttendance().stream().filter(a -> a.contains("Absent")).count();
                long late = student.getAttendance().stream().filter(a -> a.contains("Late")).count();
                
                writer.printf("\n%s (ID: %d)\n", student.getName(), student.getId());
                writer.printf("Total: %d | Present: %d (%.1f%%) | Absent: %d (%.1f%%) | Late: %d (%.1f%%)\n",
                    total, present, (present * 100.0 / total),
                    absent, (absent * 100.0 / total),
                    late, (late * 100.0 / total));
            });
    }
    
    private void generatePerformanceReport(PrintWriter writer) {
        writer.println("GRADE PERFORMANCE REPORT");
        writer.println("════════════════════════════════════════");
        
        List<Student> studentsWithGrades = students.values().stream()
            .filter(s -> !s.getGrades().isEmpty())
            .sorted(Comparator.comparingDouble(Student::calculateAverageGrade).reversed())
            .collect(Collectors.toList());
        
        if (studentsWithGrades.isEmpty()) {
            writer.println("No grade data available for any student.");
            return;
        }
        
        writer.println("\nTop Performing Students:");
        studentsWithGrades.stream()
            .limit(5)
            .forEach(student -> 
                writer.printf("%-20s (ID: %d): %.2f average\n",
                    student.getName(), student.getId(), student.calculateAverageGrade()));
        
        writer.println("\nBottom Performing Students:");
        studentsWithGrades.stream()
            .skip(Math.max(0, studentsWithGrades.size() - 5))
            .forEach(student -> 
                writer.printf("%-20s (ID: %d): %.2f average\n",
                    student.getName(), student.getId(), student.calculateAverageGrade()));
        
        DoubleSummaryStatistics gradeStats = studentsWithGrades.stream()
            .mapToDouble(Student::calculateAverageGrade)
            .summaryStatistics();
        
        writer.println("\nPerformance Statistics:");
        writer.printf("Highest Average: %.2f\n", gradeStats.getMax());
        writer.printf("Lowest Average: %.2f\n", gradeStats.getMin());
        writer.printf("Overall Average: %.2f\n", gradeStats.getAverage());
    }
    
    // File operations
    private void loadFromFile() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
    
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            Object obj = ois.readObject();
            students.clear();
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<Integer, Student> loaded = (Map<Integer, Student>) obj;
                students.putAll(loaded);
            } else if (obj instanceof List) {
                // Legacy support: convert List<Student> to Map<Integer, Student>
                @SuppressWarnings("unchecked")
                List<Student> loadedList = (List<Student>) obj;
                for (Student s : loadedList) {
                    students.put(s.getId(), s);
                }
            } else {
                System.out.println("Unknown data format in file.");
            }
            System.out.println("Loaded " + students.size() + " student records from file.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }
    
    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(students);
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }
    
    private void createBackup() {
        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
        String backupFile = BACKUP_FILE + timestamp + ".dat";
        
        try {
            Files.copy(Paths.get(DATA_FILE), Paths.get(backupFile), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Backup created: " + backupFile);
        } catch (IOException e) {
            System.out.println("Error creating backup: " + e.getMessage());
        }
    }
    
    private void restoreFromBackup() {
        File backupDir = new File(".");
        File[] backups = backupDir.listFiles((_, name) -> name.startsWith(BACKUP_FILE) && name.endsWith(".dat"));
        
        if (backups == null || backups.length == 0) {
            System.out.println("No backup files found!");
            return;
        }
        
        Arrays.sort(backups, Comparator.comparingLong(File::lastModified).reversed());
        
        System.out.println("\nAvailable Backups:");
        for (int i = 0; i < backups.length; i++) {
            System.out.printf("%d. %s (%.1f KB)\n",
                i + 1, backups[i].getName(), backups[i].length() / 1024.0);
        }
        
        System.out.print("\nEnter backup number to restore (0 to cancel): ");
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice > 0 && choice <= backups.length) {
                System.out.print("Are you sure you want to restore from " + backups[choice-1].getName() + "? (Y/N): ");
                String confirm = scanner.nextLine().toUpperCase();
                
                if (confirm.equals("Y")) {
                    try {
                        Files.copy(backups[choice-1].toPath(), Paths.get(DATA_FILE), StandardCopyOption.REPLACE_EXISTING);
                        loadFromFile();
                        System.out.println("Restore completed successfully!");
                    } catch (IOException e) {
                        System.out.println("Error during restore: " + e.getMessage());
                    }
                } else {
                    System.out.println("Restore cancelled.");
                }
            } else if (choice != 0) {
                System.out.println("Invalid choice!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input!");
        }
    }
    
    // Utility methods
    private int getNextStudentId() {
        if (students.isEmpty()) return 1;
        return Collections.max(students.keySet()) + 1;
    }
}
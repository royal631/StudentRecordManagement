import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator;

public class Student implements Comparable<Student>, Cloneable {
    // Constants
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Core fields
    private final int rollNo;
    private String name;
    private int age;
    private String course;
    
    // Additional professional fields
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Address address;
    private List<String> enrolledSubjects;
    private Map<String, Double> grades;
    private List<AttendanceRecord> attendance;
    private Guardian guardian;
    
    // Static field for auto-increment
    private static int lastRollNo = 0;
    
    // Constructor with basic fields
    public Student(String name, int age, String course) {
        this.rollNo = ++lastRollNo;
        this.name = name;
        this.age = age;
        this.course = course;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.enrolledSubjects = new ArrayList<>();
        this.grades = new HashMap<>();
        this.attendance = new ArrayList<>();
    }
    
    // Full constructor
    public Student(String name, int age, String course, String email, 
                  String phoneNumber, LocalDate dateOfBirth, Address address) {
        this(name, age, course);
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    // Getters and setters
    public int getRollNo() { return rollNo; }
    
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name; 
        updateTimestamp();
    }
    
    public int getAge() { return age; }
    public void setAge(int age) { 
        this.age = age; 
        updateTimestamp();
    }
    
    public String getCourse() { return course; }
    public void setCourse(String course) { 
        this.course = course; 
        updateTimestamp();
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { 
        this.email = email; 
        updateTimestamp();
    }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { 
        this.phoneNumber = phoneNumber; 
        updateTimestamp();
    }
    
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { 
        this.dateOfBirth = dateOfBirth; 
        updateTimestamp();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    public Address getAddress() { return address; }
    public void setAddress(Address address) { 
        this.address = address; 
        updateTimestamp();
    }
    
    public List<String> getEnrolledSubjects() { return new ArrayList<>(enrolledSubjects); }
    public void setEnrolledSubjects(List<String> enrolledSubjects) { 
        this.enrolledSubjects = new ArrayList<>(enrolledSubjects); 
        updateTimestamp();
    }
    
    public Guardian getGuardian() { return guardian; }
    public void setGuardian(Guardian guardian) { 
        this.guardian = guardian; 
        updateTimestamp();
    }
    
    // Grade management
    public void addGrade(String subject, double grade) {
        grades.put(subject, grade);
        updateTimestamp();
    }
    
    public double getGrade(String subject) {
        return grades.getOrDefault(subject, 0.0);
    }
    
    public Map<String, Double> getAllGrades() {
        return new HashMap<>(grades);
    }
    
    public double calculateAverageGrade() {
        if (grades.isEmpty()) return 0.0;
        return grades.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
    }
    
    // Attendance management
    public void markAttendance(AttendanceStatus status) {
        attendance.add(new AttendanceRecord(LocalDate.now(), status));
        updateTimestamp();
    }
    
    public List<AttendanceRecord> getAttendanceRecords() {
        return new ArrayList<>(attendance);
    }
    
    public double calculateAttendancePercentage() {
        if (attendance.isEmpty()) return 0.0;
        
        long presentDays = attendance.stream()
            .filter(record -> record.status() == AttendanceStatus.PRESENT)
            .count();
            
        return (presentDays * 100.0) / attendance.size();
    }
    
    // Utility methods
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════╗\n");
        sb.append("║             STUDENT RECORD             ║\n");
        sb.append("╠════════════════════════════════════════╣\n");
        sb.append(String.format("║ %-20s: %-18d ║\n", "Roll Number", rollNo));
        sb.append(String.format("║ %-20s: %-18s ║\n", "Name", name));
        sb.append(String.format("║ %-20s: %-18d ║\n", "Age", age));
        sb.append(String.format("║ %-20s: %-18s ║\n", "Course", course));
        
        if (email != null) {
            sb.append(String.format("║ %-20s: %-18s ║\n", "Email", email));
        }
        
        if (phoneNumber != null) {
            sb.append(String.format("║ %-20s: %-18s ║\n", "Phone", phoneNumber));
        }
        
        if (dateOfBirth != null) {
            sb.append(String.format("║ %-20s: %-18s ║\n", "Date of Birth", dateOfBirth));
        }
        
        sb.append(String.format("║ %-20s: %-18s ║\n", "Created At", createdAt.format(DATE_FORMAT)));
        sb.append(String.format("║ %-20s: %-18s ║\n", "Last Updated", updatedAt.format(DATE_FORMAT)));
        
        if (address != null) {
            sb.append("╠════════════════════════════════════════╣\n");
            sb.append("║             ADDRESS DETAILS           ║\n");
            sb.append("╠════════════════════════════════════════╣\n");
            sb.append(address.toString());
        }
        
        if (guardian != null) {
            sb.append("╠════════════════════════════════════════╣\n");
            sb.append("║            GUARDIAN DETAILS           ║\n");
            sb.append("╠════════════════════════════════════════╣\n");
            sb.append(guardian.toString());
        }
        
        if (!enrolledSubjects.isEmpty()) {
            sb.append("╠════════════════════════════════════════╣\n");
            sb.append("║           ENROLLED SUBJECTS           ║\n");
            sb.append("╠════════════════════════════════════════╣\n");
            enrolledSubjects.forEach(sub -> 
                sb.append(String.format("║ - %-36s ║\n", sub)));
        }
        
        if (!grades.isEmpty()) {
            sb.append("╠════════════════════════════════════════╣\n");
            sb.append("║               GRADES                  ║\n");
            sb.append("╠════════════════════════════════════════╣\n");
            grades.forEach((sub, grade) -> 
                sb.append(String.format("║ %-20s: %-18.2f ║\n", sub, grade)));
            sb.append(String.format("║ %-20s: %-18.2f ║\n", "Average Grade", calculateAverageGrade()));
        }
        
        if (!attendance.isEmpty()) {
            sb.append("╠════════════════════════════════════════╣\n");
            sb.append("║             ATTENDANCE                ║\n");
            sb.append("╠════════════════════════════════════════╣\n");
            sb.append(String.format("║ %-20s: %-18.1f%% ║\n", "Attendance", calculateAttendancePercentage()));
        }
        
        sb.append("╚════════════════════════════════════════╝");
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return rollNo == student.rollNo && 
               age == student.age && 
               Objects.equals(name, student.name) && 
               Objects.equals(course, student.course);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(rollNo, name, age, course);
    }
    
    @Override
    public int compareTo(Student other) {
        return Integer.compare(this.rollNo, other.rollNo);
    }
    
    @Override
    public Student clone() {
        try {
            Student cloned = (Student) super.clone();
            cloned.enrolledSubjects = new ArrayList<>(this.enrolledSubjects);
            cloned.grades = new HashMap<>(this.grades);
            cloned.attendance = new ArrayList<>(this.attendance);
            if (this.address != null) {
                cloned.address = this.address.clone();
            }
            if (this.guardian != null) {
                cloned.guardian = this.guardian.clone();
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Can't happen
        }
    }
    
    // Nested classes for additional functionality
    public static class Address implements Cloneable {
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        
        public Address(String street, String city, String state, String postalCode, String country) {
            this.street = street;
            this.city = city;
            this.state = state;
            this.postalCode = postalCode;
            this.country = country;
        }
        
        // Getters and setters
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("║ %-20s: %-18s ║\n", "Street", street));
            sb.append(String.format("║ %-20s: %-18s ║\n", "City", city));
            sb.append(String.format("║ %-20s: %-18s ║\n", "State", state));
            sb.append(String.format("║ %-20s: %-18s ║\n", "Postal Code", postalCode));
            sb.append(String.format("║ %-20s: %-18s ║", "Country", country));
            return sb.toString();
        }
        
        @Override
        public Address clone() {
            try {
                return (Address) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(); // Can't happen
            }
        }
    }
    
    public static class Guardian implements Cloneable {
        private String name;
        private String relationship;
        private String phone;
        private String email;
        
        public Guardian(String name, String relationship, String phone, String email) {
            this.name = name;
            this.relationship = relationship;
            this.phone = phone;
            this.email = email;
        }
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getRelationship() { return relationship; }
        public void setRelationship(String relationship) { this.relationship = relationship; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("║ %-20s: %-18s ║\n", "Guardian Name", name));
            sb.append(String.format("║ %-20s: %-18s ║\n", "Relationship", relationship));
            sb.append(String.format("║ %-20s: %-18s ║\n", "Phone", phone));
            sb.append(String.format("║ %-20s: %-18s ║", "Email", email));
            return sb.toString();
        }
        
        @Override
        public Guardian clone() {
            try {
                return (Guardian) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(); // Can't happen
            }
        }
    }
    
    public record AttendanceRecord(LocalDate date, AttendanceStatus status) {
        @Override
        public String toString() {
            return String.format("%s: %s", date, status);
        }
    }
    
    public enum AttendanceStatus {
        PRESENT, ABSENT, LATE, EXCUSED
    }
    
    // Static utility methods
    public static List<Student> filterByCourse(List<Student> students, String course) {
        return students.stream()
            .filter(s -> s.getCourse().equalsIgnoreCase(course))
            .sorted()
            .collect(Collectors.toList());
    }
    
    public static List<Student> filterByAgeRange(List<Student> students, int minAge, int maxAge) {
        return students.stream()
            .filter(s -> s.getAge() >= minAge && s.getAge() <= maxAge)
            .sorted()
            .collect(Collectors.toList());
    }
    
    public static List<Student> getTopPerformers(List<Student> students, int count) {
        return students.stream()
            .filter(s -> !s.getAllGrades().isEmpty())
            .sorted(Comparator.comparingDouble(Student::calculateAverageGrade).reversed())
            .limit(count)
            .collect(Collectors.toList());
    }
}
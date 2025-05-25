import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;
import java.nio.file.*;
import java.awt.print.*;
import java.text.MessageFormat;
import javax.swing.filechooser.FileNameExtensionFilter;

public class StudentGUIPro extends JFrame {
    // Constants
    private static final int ROWS_PER_PAGE = 10;
    private static final String[] COURSES = {"BTECH", "MTECH", "MBA", "BCA", "MCA", "OTHER"};
    private static final String[] LANG_EN = {"Student Record Management System","Add Student","Update Student","Delete Student","Clear Fields","Search:","Export CSV","Import CSV","Dark Mode","Next Page","Prev Page","Page","Print PDF","Language","Roll No","Name","Age","Course","Email","Phone","Address","Gender","Male","Female","Other","Save Data","Load Data","Statistics","About","Total Students:","Average Age:","Most Common Course:","Student Management System v1.0","Developed by:","Features:","- Add/Edit/Delete Students","- Search and Filter","- CSV Import/Export","- Dark/Light Mode","- Multi-language Support","- Printing Support"};
    private static final String[] LANG_HI = {"छात्र रिकॉर्ड प्रबंधन प्रणाली","छात्र जोड़ें","छात्र अपडेट करें","छात्र हटाएं","फ़ील्ड साफ़ करें","खोजें:","CSV निर्यात","CSV आयात","डार्क मोड","अगला पृष्ठ","पिछला पृष्ठ","पृष्ठ","पीडीएफ प्रिंट करें","भाषा","रोल नंबर","नाम","आयु","कोर्स","ईमेल","फोन","पता","लिंग","पुरुष","महिला","अन्य","डेटा सहेजें","डेटा लोड करें","आँकड़े","के बारे में","कुल छात्र:","औसत आयु:","सबसे आम कोर्स:","छात्र प्रबंधन प्रणाली v1.0","द्वारा विकसित:","विशेषताएँ:","- छात्र जोड़ें/संपादित करें/हटाएँ","- खोज और फ़िल्टर","- CSV आयात/निर्यात","- डार्क/लाइट मोड","- बहुभाषी समर्थन","- मुद्रण समर्थन"};

    // UI Components
    private JTextField tfRollNo, tfName, tfAge, tfSearch, tfEmail, tfPhone, tfAddress;
    private JComboBox<String> cbCourse, cbGender;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnExport, btnImport, btnDarkMode, btnNextPage, btnPrevPage, btnPrint, btnSave, btnLoad, btnStats, btnAbout;
    private JLabel lblPage, lblSearch, lblLang;
    private JTable table;
    private DefaultTableModel tableModel;
    private boolean darkMode = false;
    private boolean isHindi = false;

    // Data & Pagination
    private List<Student> students = new ArrayList<>();
    private List<Student> filteredStudents = new ArrayList<>();
    private int currentPage = 1;

    public StudentGUIPro() {
        initComponents();
        loadFromFile();
        applyLanguage();
        refreshTable();
        setDarkMode(false);
    }

    private void initComponents() {
        setTitle("Student Record Management System");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Top Panel (Input fields + Buttons)
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5,5,5,5);

        // RollNo
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Roll No:"), gbc);
        tfRollNo = new JTextField(10);
        gbc.gridx = 1;
        inputPanel.add(tfRollNo, gbc);

        // Name
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Name:"), gbc);
        tfName = new JTextField(15);
        gbc.gridx = 1;
        inputPanel.add(tfName, gbc);

        // Age
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Age:"), gbc);
        tfAge = new JTextField(5);
        gbc.gridx = 1;
        inputPanel.add(tfAge, gbc);

        // Course Dropdown
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Course:"), gbc);
        cbCourse = new JComboBox<>(COURSES);
        gbc.gridx = 1;
        inputPanel.add(cbCourse, gbc);

        // Gender Dropdown
        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("Gender:"), gbc);
        cbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        gbc.gridx = 1;
        inputPanel.add(cbGender, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 5;
        inputPanel.add(new JLabel("Email:"), gbc);
        tfEmail = new JTextField(20);
        gbc.gridx = 1;
        inputPanel.add(tfEmail, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 6;
        inputPanel.add(new JLabel("Phone:"), gbc);
        tfPhone = new JTextField(15);
        gbc.gridx = 1;
        inputPanel.add(tfPhone, gbc);

        // Address
        gbc.gridx = 0; gbc.gridy = 7;
        inputPanel.add(new JLabel("Address:"), gbc);
        tfAddress = new JTextField(25);
        gbc.gridx = 1;
        inputPanel.add(tfAddress, gbc);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new GridLayout(2, 5, 10, 5));
        btnAdd = new JButton("Add Student");
        btnUpdate = new JButton("Update Student");
        btnDelete = new JButton("Delete Student");
        btnClear = new JButton("Clear Fields");
        btnExport = new JButton("Export CSV");
        btnImport = new JButton("Import CSV");
        btnDarkMode = new JButton("Dark Mode");
        btnPrint = new JButton("Print PDF");
        btnSave = new JButton("Save Data");
        btnLoad = new JButton("Load Data");
        btnStats = new JButton("Statistics");
        btnAbout = new JButton("About");
        
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        btnPanel.add(btnExport);
        btnPanel.add(btnImport);
        btnPanel.add(btnPrint);
        btnPanel.add(btnDarkMode);
        btnPanel.add(btnSave);
        btnPanel.add(btnLoad);
        btnPanel.add(btnStats);
        btnPanel.add(btnAbout);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        inputPanel.add(btnPanel, gbc);

        // Search and Language
        JPanel searchLangPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblSearch = new JLabel("Search:");
        tfSearch = new JTextField(20);
        tfSearch.setToolTipText("Search by roll no, name, course etc.");
        lblLang = new JLabel("Language:");
        JComboBox<String> langCombo = new JComboBox<>(new String[]{"English", "हिन्दी"});
        searchLangPanel.add(lblSearch);
        searchLangPanel.add(tfSearch);
        searchLangPanel.add(lblLang);
        searchLangPanel.add(langCombo);

        // Table setup with additional columns
        tableModel = new DefaultTableModel(new String[]{"Roll No", "Name", "Age", "Course", "Gender", "Email", "Phone"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(60);  // Roll No
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        table.getColumnModel().getColumn(2).setPreferredWidth(40);  // Age
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Course
        table.getColumnModel().getColumn(4).setPreferredWidth(60);  // Gender
        table.getColumnModel().getColumn(5).setPreferredWidth(150); // Email
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Phone
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(900, 300));

        // Pagination Controls
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPrevPage = new JButton("Prev");
        btnNextPage = new JButton("Next");
        lblPage = new JLabel("Page 1");
        paginationPanel.add(btnPrevPage);
        paginationPanel.add(lblPage);
        paginationPanel.add(btnNextPage);

        // Layout main frame
        setLayout(new BorderLayout(10, 10));
        add(inputPanel, BorderLayout.NORTH);
        add(searchLangPanel, BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);
        add(paginationPanel, BorderLayout.PAGE_END);

        // Event Listeners
        btnAdd.addActionListener(e -> addStudent());
        btnUpdate.addActionListener(e -> updateStudent());
        btnDelete.addActionListener(e -> deleteStudent());
        btnClear.addActionListener(e -> clearFields());
        btnExport.addActionListener(e -> exportCSV());
        btnImport.addActionListener(e -> importCSV());
        btnDarkMode.addActionListener(e -> toggleDarkMode());
        btnPrint.addActionListener(e -> printTable());
        btnSave.addActionListener(e -> saveToFile());
        btnLoad.addActionListener(e -> {
            loadFromFile();
            JOptionPane.showMessageDialog(this, isHindi ? "डेटा सफलतापूर्वक लोड हो गया" : "Data loaded successfully!");
        });
        btnStats.addActionListener(e -> showStatistics());
        btnAbout.addActionListener(e -> showAbout());

        tfSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterStudents(); }
            public void removeUpdate(DocumentEvent e) { filterStudents(); }
            public void changedUpdate(DocumentEvent e) { filterStudents(); }
        });

        langCombo.addActionListener(e -> {
            isHindi = langCombo.getSelectedIndex() == 1;
            applyLanguage();
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                Student s = filteredStudents.get((currentPage-1)*ROWS_PER_PAGE + modelRow);
                fillFields(s);
            }
        });

        btnPrevPage.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                refreshTable();
            }
        });

        btnNextPage.addActionListener(e -> {
            int maxPage = (int) Math.ceil(filteredStudents.size() / (double) ROWS_PER_PAGE);
            if (currentPage < maxPage) {
                currentPage++;
                refreshTable();
            }
        });
    }

    // Language toggle method
    private void applyLanguage() {
        String[] lang = isHindi ? LANG_HI : LANG_EN;
        setTitle(lang[0]);
        btnAdd.setText(lang[1]);
        btnUpdate.setText(lang[2]);
        btnDelete.setText(lang[3]);
        btnClear.setText(lang[4]);
        lblSearch.setText(lang[5]);
        btnExport.setText(lang[6]);
        btnImport.setText(lang[7]);
        btnDarkMode.setText(lang[8]);
        btnNextPage.setText(lang[9]);
        btnPrevPage.setText(lang[10]);
        lblPage.setText(lang[11] + " " + currentPage);
        btnPrint.setText(lang[12]);
        lblLang.setText(lang[13]);
        btnSave.setText(lang[25]);
        btnLoad.setText(lang[26]);
        btnStats.setText(lang[27]);
        btnAbout.setText(lang[28]);

        // Labels in input panel
        Component[] components = ((JPanel)getContentPane().getComponent(0)).getComponents();
        ((JLabel)components[0]).setText(lang[14] + ":");
        ((JLabel)components[2]).setText(lang[15] + ":");
        ((JLabel)components[4]).setText(lang[16] + ":");
        ((JLabel)components[6]).setText(lang[17] + ":");
        ((JLabel)components[8]).setText(lang[21] + ":");
        ((JLabel)components[10]).setText(lang[18] + ":");
        ((JLabel)components[12]).setText(lang[19] + ":");
        ((JLabel)components[14]).setText(lang[20] + ":");
    }

    // Student class with additional fields
    static class Student {
        int rollNo;
        String name;
        int age;
        String course;
        String gender;
        String email;
        String phone;
        String address;

        Student(int rollNo, String name, int age, String course, String gender, String email, String phone, String address) {
            this.rollNo = rollNo;
            this.name = name;
            this.age = age;
            this.course = course;
            this.gender = gender;
            this.email = email;
            this.phone = phone;
            this.address = address;
        }
    }

    private void fillFields(Student s) {
        tfRollNo.setText(String.valueOf(s.rollNo));
        tfName.setText(s.name);
        tfAge.setText(String.valueOf(s.age));
        cbCourse.setSelectedItem(s.course);
        cbGender.setSelectedItem(s.gender);
        tfEmail.setText(s.email);
        tfPhone.setText(s.phone);
        tfAddress.setText(s.address);
    }

    private void clearFields() {
        tfRollNo.setText("");
        tfName.setText("");
        tfAge.setText("");
        cbCourse.setSelectedIndex(0);
        cbGender.setSelectedIndex(0);
        tfEmail.setText("");
        tfPhone.setText("");
        tfAddress.setText("");
        table.clearSelection();
    }

    private void addStudent() {
        if (!validateInput()) return;
        int roll = Integer.parseInt(tfRollNo.getText().trim());
        if (students.stream().anyMatch(st -> st.rollNo == roll)) {
            JOptionPane.showMessageDialog(this, 
                isHindi ? "रोल नंबर पहले से मौजूद है!" : "Roll No already exists!", 
                isHindi ? "त्रुटि" : "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Student s = new Student(
            roll, 
            tfName.getText().trim(), 
            Integer.parseInt(tfAge.getText().trim()), 
            (String) cbCourse.getSelectedItem(),
            (String) cbGender.getSelectedItem(),
            tfEmail.getText().trim(),
            tfPhone.getText().trim(),
            tfAddress.getText().trim()
        );
        students.add(s);
        saveToFile();
        filterStudents();
        clearFields();
        JOptionPane.showMessageDialog(this, 
            isHindi ? "छात्र सफलतापूर्वक जोड़ा गया!" : "Student added successfully!");
    }

    private void updateStudent() {
        if (!validateInput()) return;
        int roll = Integer.parseInt(tfRollNo.getText().trim());
        Student existing = students.stream().filter(st -> st.rollNo == roll).findFirst().orElse(null);
        if (existing == null) {
            JOptionPane.showMessageDialog(this, 
                isHindi ? "अपडेट के लिए छात्र नहीं मिला!" : "Student not found for update!", 
                isHindi ? "त्रुटि" : "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        existing.name = tfName.getText().trim();
        existing.age = Integer.parseInt(tfAge.getText().trim());
        existing.course = (String) cbCourse.getSelectedItem();
        existing.gender = (String) cbGender.getSelectedItem();
        existing.email = tfEmail.getText().trim();
        existing.phone = tfPhone.getText().trim();
        existing.address = tfAddress.getText().trim();
        saveToFile();
        filterStudents();
        clearFields();
        JOptionPane.showMessageDialog(this, 
            isHindi ? "छात्र सफलतापूर्वक अपडेट हो गया!" : "Student updated successfully!");
    }

    private void deleteStudent() {
        if (table.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, 
                isHindi ? "हटाने के लिए तालिका से एक छात्र का चयन करें!" : "Select a student from table to delete!", 
                isHindi ? "त्रुटि" : "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
        Student toRemove = filteredStudents.get((currentPage-1)*ROWS_PER_PAGE + modelRow);
        students.remove(toRemove);
        saveToFile();
        filterStudents();
        clearFields();
        JOptionPane.showMessageDialog(this, 
            isHindi ? "छात्र सफलतापूर्वक हटा दिया गया!" : "Student deleted successfully!");
    }

    private boolean validateInput() {
        try {
            int roll = Integer.parseInt(tfRollNo.getText().trim());
            if (roll <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                isHindi ? "रोल नंबर धनात्मक पूर्णांक होना चाहिए!" : "Roll No must be positive integer!", 
                isHindi ? "त्रुटि" : "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (tfName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                isHindi ? "नाम खाली नहीं हो सकता!" : "Name cannot be empty!", 
                isHindi ? "त्रुटि" : "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            int age = Integer.parseInt(tfAge.getText().trim());
            if (age <= 0 || age > 120) {
                JOptionPane.showMessageDialog(this, 
                    isHindi ? "आयु 1 और 120 के बीच होनी चाहिए!" : "Age must be between 1 and 120!", 
                    isHindi ? "त्रुटि" : "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                isHindi ? "आयु एक संख्या होनी चाहिए!" : "Age must be a number!", 
                isHindi ? "त्रुटि" : "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!tfEmail.getText().trim().isEmpty() && !tfEmail.getText().trim().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            JOptionPane.showMessageDialog(this, 
                isHindi ? "अमान्य ईमेल पता!" : "Invalid email address!", 
                isHindi ? "त्रुटि" : "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void filterStudents() {
        String keyword = tfSearch.getText().trim().toLowerCase();
        filteredStudents = students.stream()
            .filter(st -> 
                String.valueOf(st.rollNo).contains(keyword) || 
                st.name.toLowerCase().contains(keyword) ||
                st.course.toLowerCase().contains(keyword) ||
                st.email.toLowerCase().contains(keyword) ||
                st.phone.toLowerCase().contains(keyword) ||
                st.gender.toLowerCase().contains(keyword))
            .collect(Collectors.toList());
        currentPage = 1;
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        int start = (currentPage - 1) * ROWS_PER_PAGE;
        int end = Math.min(start + ROWS_PER_PAGE, filteredStudents.size());
        for (int i = start; i < end; i++) {
            Student s = filteredStudents.get(i);
            tableModel.addRow(new Object[]{
                s.rollNo, 
                s.name, 
                s.age, 
                s.course,
                s.gender,
                s.email,
                s.phone
            });
        }
        int maxPage = Math.max(1, (int) Math.ceil(filteredStudents.size() / (double) ROWS_PER_PAGE));
        lblPage.setText((isHindi ? "पृष्ठ " : "Page ") + currentPage + " / " + maxPage);
        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < maxPage);
    }

    // CSV export and import
    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        fc.setSelectedFile(new File("students.csv"));
        int option = fc.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".csv")) {
                f = new File(f.getAbsolutePath() + ".csv");
            }
            try (PrintWriter pw = new PrintWriter(f)) {
                pw.println("RollNo,Name,Age,Course,Gender,Email,Phone,Address");
                for (Student s : students) {
                    pw.printf("%d,%s,%d,%s,%s,%s,%s,\"%s\"%n", 
                        s.rollNo, s.name, s.age, s.course, s.gender, s.email, s.phone, s.address);
                }
                JOptionPane.showMessageDialog(this, 
                    isHindi ? "CSV सफलतापूर्वक निर्यात किया गया!" : "CSV exported successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    isHindi ? "CSV निर्यात करने में त्रुटि: " : "Error exporting CSV: " + e.getMessage(), 
                    isHindi ? "त्रुटि" : "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        int option = fc.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line = br.readLine(); // header skip
                List<Student> imported = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    // Handle quoted fields with commas
                    String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    if (parts.length >= 4) {
                        int roll = Integer.parseInt(parts[0].trim());
                        String name = parts[1].trim();
                        int age = Integer.parseInt(parts[2].trim());
                        String course = parts[3].trim();
                        String gender = parts.length > 4 ? parts[4].trim() : "Other";
                        String email = parts.length > 5 ? parts[5].trim() : "";
                        String phone = parts.length > 6 ? parts[6].trim() : "";
                        String address = parts.length > 7 ? parts[7].trim().replace("\"", "") : "";
                        imported.add(new Student(roll, name, age, course, gender, email, phone, address));
                    }
                }
                // Replace all students
                students = imported;
                saveToFile();
                filterStudents();
                clearFields();
                JOptionPane.showMessageDialog(this, 
                    isHindi ? "CSV सफलतापूर्वक आयात किया गया!" : "CSV imported successfully!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    isHindi ? "CSV आयात करने में त्रुटि: " : "Error importing CSV: " + e.getMessage(), 
                    isHindi ? "त्रुटि" : "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Save/Load from local file (students.dat - now using serialization)
    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("students.dat"))) {
            oos.writeObject(students);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                isHindi ? "डेटा सहेजने में त्रुटि: " : "Error saving data: " + e.getMessage(), 
                isHindi ? "त्रुटि" : "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        File f = new File("students.dat");
        if (!f.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            students = (List<Student>) ois.readObject();
            filterStudents();
        } catch (Exception e) {
            // Fallback to old text format if serialization fails
            loadFromTextFile();
        }
    }
    
    private void loadFromTextFile() {
        File f = new File("students.txt");
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            students.clear();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    int roll = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    int age = Integer.parseInt(parts[2].trim());
                    String course = parts[3].trim();
                    String gender = parts.length > 4 ? parts[4].trim() : "Other";
                    String email = parts.length > 5 ? parts[5].trim() : "";
                    String phone = parts.length > 6 ? parts[6].trim() : "";
                    String address = parts.length > 7 ? parts[7].trim() : "";
                    students.add(new Student(roll, name, age, course, gender, email, phone, address));
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                isHindi ? "डेटा लोड करने में त्रुटि: " : "Error loading data: " + e.getMessage(), 
                isHindi ? "त्रुटि" : "Error", JOptionPane.ERROR_MESSAGE);
        }
        filterStudents();
    }

    // Dark mode toggle
    private void toggleDarkMode() {
        darkMode = !darkMode;
        setDarkMode(darkMode);
    }

    private void setDarkMode(boolean on) {
        Color bg = on ? new Color(45, 45, 48) : Color.WHITE;
        Color fg = on ? Color.WHITE : Color.BLACK;
        Color tableBg = on ? new Color(30, 30, 30) : Color.WHITE;
        Color tableFg = on ? Color.WHITE : Color.BLACK;
        Color headerBg = on ? new Color(60, 60, 60) : UIManager.getColor("TableHeader.background");
        
        getContentPane().setBackground(bg);

        // Update all components
        updateComponentColors(getContentPane(), bg, fg);
        
        // Special handling for table
        table.setBackground(tableBg);
        table.setForeground(tableFg);
        table.setGridColor(on ? Color.DARK_GRAY : Color.LIGHT_GRAY);
        table.getTableHeader().setBackground(headerBg);
        table.getTableHeader().setForeground(fg);
        
        btnDarkMode.setText(on ? (isHindi ? "लाइट मोड" : "Light Mode") : (isHindi ? "डार्क मोड" : "Dark Mode"));
        repaint();
    }

    private void updateComponentColors(Component comp, Color bg, Color fg) {
        if (comp instanceof JComponent) {
            ((JComponent)comp).setOpaque(true);
        }
        comp.setBackground(bg);
        comp.setForeground(fg);
        
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                updateComponentColors(child, bg, fg);
            }
        }
        
        if (comp instanceof JScrollPane) {
            JViewport vp = ((JScrollPane) comp).getViewport();
            vp.setBackground(bg);
            if (vp.getView() != null) {
                vp.getView().setBackground(bg);
            }
        }
    }

    // Print PDF / Print Table
    private void printTable() {
        try {
            MessageFormat header = new MessageFormat(
                isHindi ? "छात्र सूची - पृष्ठ {0}" : "Student List - Page {0}");
            boolean done = table.print(JTable.PrintMode.FIT_WIDTH, header, null);
            if (done) {
                JOptionPane.showMessageDialog(this, 
                    isHindi ? "मुद्रण पूरा हुआ" : "Printing completed");
            } else {
                JOptionPane.showMessageDialog(this, 
                    isHindi ? "मुद्रण रद्द" : "Printing cancelled");
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, 
                isHindi ? "मुद्रण विफल: " : "Printing failed: " + ex.getMessage(), 
                isHindi ? "त्रुटि" : "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Show statistics dialog
    private void showStatistics() {
        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                isHindi ? "कोई डेटा उपलब्ध नहीं है!" : "No data available!", 
                isHindi ? "आँकड़े" : "Statistics", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int total = students.size();
        double avgAge = students.stream().mapToInt(s -> s.age).average().orElse(0);
        
        // Find most common course
        Map<String, Long> courseCount = students.stream()
            .collect(Collectors.groupingBy(s -> s.course, Collectors.counting()));
        String mostCommonCourse = courseCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
        
        // Gender distribution
        Map<String, Long> genderCount = students.stream()
            .collect(Collectors.groupingBy(s -> s.gender, Collectors.counting()));
        
        // Create statistics message
        String[] lang = isHindi ? LANG_HI : LANG_EN;
        StringBuilder stats = new StringBuilder();
        stats.append(lang[29]).append(" ").append(total).append("\n");
        stats.append(lang[30]).append(" ").append(String.format("%.1f", avgAge)).append("\n");
        stats.append(lang[31]).append(" ").append(mostCommonCourse).append("\n\n");
        
        // Add gender distribution
        stats.append(isHindi ? "लिंग वितरण:\n" : "Gender Distribution:\n");
        genderCount.forEach((gender, count) -> {
            stats.append(gender).append(": ").append(count).append(" (")
                .append(String.format("%.1f", (count * 100.0 / total))).append("%)\n");
        });
        
        JOptionPane.showMessageDialog(this, stats.toString(), 
            isHindi ? "आँकड़े" : "Statistics", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Show about dialog
    private void showAbout() {
        String[] lang = isHindi ? LANG_HI : LANG_EN;
        String aboutMsg = String.format(
            "<html><center><h2>%s</h2>" +
            "<b>%s</b> ROYAL VERMA <br><br>" +
            "<b>%s</b><br>" +
            "<ul>" +
            "<li>%s</li>" +
            "<li>%s</li>" +
            "<li>%s</li>" +
            "<li>%s</li>" +
            "<li>%s</li>" +
            "<li>%s</li>" +
            "</ul></center></html>",
            lang[32], lang[33], lang[34], 
            lang[35], lang[36], lang[37], 
            lang[38], lang[39], lang[40]
        );
        
        JOptionPane.showMessageDialog(this, aboutMsg, 
            isHindi ? "के बारे में" : "About", JOptionPane.INFORMATION_MESSAGE);
    }

    // Main method
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            StudentGUIPro app = new StudentGUIPro();
            app.setVisible(true);
        });
    }
}
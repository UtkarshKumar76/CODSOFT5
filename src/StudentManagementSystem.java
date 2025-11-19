import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Student Management System
 * Features:
 * - Modern Swing GUI with SplitPane and GridBagLayout
 * - Enhanced Student Data
 * - Menu Bar & Status Bar
 * - Data Persistence (CSV)
 * - Search
 */
public class StudentManagementSystem extends JFrame {

    // ===========================
    // 1. Student Class (Inner)
    // ===========================
    static class Student {
        private String name;
        private String rollNumber;
        private String course;
        private String grade;
        private String email;
        private String contact;

        public Student(String name, String rollNumber, String course, String grade, String email, String contact) {
            this.name = name;
            this.rollNumber = rollNumber;
            this.course = course;
            this.grade = grade;
            this.email = email;
            this.contact = contact;
        }

        // Getters & Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRollNumber() { return rollNumber; }
        public String getCourse() { return course; }
        public void setCourse(String course) { this.course = course; }
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getContact() { return contact; }
        public void setContact(String contact) { this.contact = contact; }

        // CSV Serialization
        public String toCSV() {
            return String.join(",", name, rollNumber, course, grade, email, contact);
        }

        // CSV Deserialization
        public static Student fromCSV(String csvLine) {
            String[] parts = csvLine.split(",");
            // Ensure we have enough parts to prevent IndexOutOfBounds
            if (parts.length >= 6) {
                return new Student(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
            }
            return null;
        }
    }

    // ===========================
    // 2. System Components
    // ===========================
    private ArrayList<Student> studentList;
    private final String FILE_NAME = "students.txt";

    // GUI Components
    private JTextField txtName, txtRoll, txtGrade, txtEmail, txtContact, txtSearch;
    private JComboBox<String> comboCourse;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblStatus;

    // Colors & Fonts
    private final Color PRIMARY_COLOR = new Color(0, 128, 128); // Teal
    private final Color ACCENT_COLOR = new Color(0, 102, 102);  // Darker Teal
    private final Color BG_COLOR = new Color(240, 255, 250);    // Mint Cream
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);

    // ===========================
    // 3. Constructor (GUI Build)
    // ===========================
    public StudentManagementSystem() {
        studentList = new ArrayList<>();

        // Window Settings
        setTitle("EduTrack - Student Management System");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Menu Bar ---
        setupMenuBar();

        // --- Main Layout ---
        setLayout(new BorderLayout());

        // 1. Top Toolbar / Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(PRIMARY_COLOR);
        topPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("Student Management Dashboard");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        topPanel.add(lblTitle, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // 2. Split Pane (Left: Form, Right: Table)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(350);
        splitPane.setDividerSize(5);

        // Left Panel: Input Form
        JPanel formPanel = createFormPanel();
        splitPane.setLeftComponent(formPanel);

        // Right Panel: Table Data
        JPanel tablePanel = createTablePanel();
        splitPane.setRightComponent(tablePanel);

        add(splitPane, BorderLayout.CENTER);

        // 3. Status Bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        statusPanel.setBackground(Color.WHITE);
        lblStatus = new JLabel("Ready");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusPanel.add(lblStatus, BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);

        // Load Data
        loadDataFromFile();
        updateStatus();
    }

    // ===========================
    // GUI Helper Methods
    // ===========================

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem itemSave = new JMenuItem("Save Data");
        JMenuItem itemExit = new JMenuItem("Exit");
        itemSave.addActionListener(e -> { saveDataToFile(); JOptionPane.showMessageDialog(this, "Data Saved!"); });
        itemExit.addActionListener(e -> System.exit(0));
        fileMenu.add(itemSave);
        fileMenu.addSeparator();
        fileMenu.add(itemExit);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem itemAbout = new JMenuItem("About");
        itemAbout.addActionListener(e -> JOptionPane.showMessageDialog(this, "Student Management System v2.0\nDeveloped in Java Swing."));
        helpMenu.add(itemAbout);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Title for Form
        JLabel lblFormTitle = new JLabel("Student Details");
        lblFormTitle.setFont(HEADER_FONT);
        lblFormTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        panel.add(lblFormTitle, BorderLayout.NORTH);

        // Form Fields Container (GridBagLayout)
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_COLOR); // Match background
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // -- Add Fields --
        txtName = new JTextField(20);
        txtRoll = new JTextField(20);
        String[] courses = {
                "Bachelor of Computer Applications",
                "Bachelor of Arts",
                "Bachelor of Technology",
                "Master of Computer Applications",
                "Master of Arts",
                "Master of Technology",
                "Psychology"
        };
        comboCourse = new JComboBox<>(courses);
        comboCourse.setEditable(true); // Allow custom course entry
        txtGrade = new JTextField(10);
        txtEmail = new JTextField(20);
        txtContact = new JTextField(20);

        addFormField(form, gbc, 0, "Roll Number (ID):", txtRoll);
        addFormField(form, gbc, 1, "Full Name:", txtName);
        addFormField(form, gbc, 2, "Course / Major:", comboCourse);
        addFormField(form, gbc, 3, "Grade / GPA:", txtGrade);
        addFormField(form, gbc, 4, "Email Address:", txtEmail);
        addFormField(form, gbc, 5, "Phone / Contact:", txtContact);

        panel.add(form, BorderLayout.CENTER);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        btnPanel.setBackground(BG_COLOR);
        btnPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Button Colors
        JButton btnAdd = createStyledButton("Add Student", new Color(39, 174, 96)); // Darker Green
        JButton btnUpdate = createStyledButton("Update", new Color(41, 128, 185)); // Strong Blue
        JButton btnDelete = createStyledButton("Delete", new Color(192, 57, 43));  // Dark Red
        JButton btnClear = createStyledButton("Reset", new Color(127, 140, 141));  // Gray

        btnAdd.addActionListener(e -> addStudent());
        btnUpdate.addActionListener(e -> updateStudent());
        btnDelete.addActionListener(e -> deleteStudent());
        btnClear.addActionListener(e -> clearFields());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel label = new JLabel(labelText);
        label.setFont(MAIN_FONT);
        panel.add(label, gbc);

        gbc.gridx = 1;
        field.setFont(MAIN_FONT);
        if(field instanceof JTextField) {
            ((JTextField)field).setMargin(new Insets(4,4,4,4));
        }
        panel.add(field, gbc);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Search Bar
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(Color.WHITE);
        JLabel lblSearch = new JLabel("Search: ");
        lblSearch.setFont(MAIN_FONT);
        txtSearch = new JTextField();
        txtSearch.setFont(MAIN_FONT);
        txtSearch.putClientProperty("JTextField.placeholderText", "Type name or roll number...");

        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterStudents(txtSearch.getText());
            }
        });

        searchPanel.add(lblSearch, BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.NORTH);

        // Table Setup
        String[] columns = {"Roll No", "Name", "Course", "Grade", "Email", "Contact"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(MAIN_FONT);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Table Header Styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(240, 240, 240));

        // Center Align specific columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Roll
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Grade

        // Selection Logic
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                fillFormFromSelection();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);

        // === CRITICAL FIX FOR VISIBILITY ===
        btn.setOpaque(true);
        btn.setBorderPainted(false);

        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Simple Hover Effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    // ===========================
    // Logic Methods
    // ===========================

    private void addStudent() {
        if (!validateInput()) return;

        String roll = txtRoll.getText().trim();

        // Check Duplicate
        for (Student s : studentList) {
            if (s.getRollNumber().equalsIgnoreCase(roll)) {
                showError("Roll Number '" + roll + "' already exists!");
                return;
            }
        }

        Student s = new Student(
                txtName.getText().trim(),
                roll,
                (String) comboCourse.getSelectedItem(),
                txtGrade.getText().trim(),
                txtEmail.getText().trim(),
                txtContact.getText().trim()
        );

        studentList.add(s);
        refreshTable();
        saveDataToFile();
        clearFields();
        updateStatus();
        JOptionPane.showMessageDialog(this, "Student Added Successfully!");
    }

    private void updateStudent() {
        String roll = txtRoll.getText().trim();
        if (roll.isEmpty()) { showError("No student selected."); return; }

        Student found = findStudentByRoll(roll);
        if (found != null) {
            found.setName(txtName.getText().trim());
            found.setCourse((String) comboCourse.getSelectedItem());
            found.setGrade(txtGrade.getText().trim());
            found.setEmail(txtEmail.getText().trim());
            found.setContact(txtContact.getText().trim());

            refreshTable();
            saveDataToFile();
            clearFields();
            updateStatus();
            JOptionPane.showMessageDialog(this, "Student Updated!");
        } else {
            showError("Could not find student with Roll No: " + roll);
        }
    }

    private void deleteStudent() {
        String roll = txtRoll.getText().trim();
        if (roll.isEmpty()) { showError("No student selected."); return; }

        int opt = JOptionPane.showConfirmDialog(this, "Delete student " + roll + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            studentList.removeIf(s -> s.getRollNumber().equalsIgnoreCase(roll));
            refreshTable();
            saveDataToFile();
            clearFields();
            updateStatus();
        }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row != -1) {
            row = table.convertRowIndexToModel(row);
            txtRoll.setText(tableModel.getValueAt(row, 0).toString());
            txtName.setText(tableModel.getValueAt(row, 1).toString());
            comboCourse.setSelectedItem(tableModel.getValueAt(row, 2).toString());
            txtGrade.setText(tableModel.getValueAt(row, 3).toString());
            txtEmail.setText(tableModel.getValueAt(row, 4).toString());
            txtContact.setText(tableModel.getValueAt(row, 5).toString());

            txtRoll.setEditable(false); // Lock ID key
        }
    }

    private boolean validateInput() {
        if (txtRoll.getText().trim().isEmpty() || txtName.getText().trim().isEmpty()) {
            showError("Roll Number and Name are required!");
            return false;
        }
        return true;
    }

    private Student findStudentByRoll(String roll) {
        for (Student s : studentList) {
            if (s.getRollNumber().equalsIgnoreCase(roll)) return s;
        }
        return null;
    }

    private void clearFields() {
        txtRoll.setEditable(true);
        txtRoll.setText("");
        txtName.setText("");
        comboCourse.setSelectedIndex(0);
        txtGrade.setText("");
        txtEmail.setText("");
        txtContact.setText("");
        table.clearSelection();
    }

    private void filterStudents(String query) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        if (query.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Student s : studentList) {
            tableModel.addRow(new Object[]{
                    s.getRollNumber(), s.getName(), s.getCourse(), s.getGrade(), s.getEmail(), s.getContact()
            });
        }
    }

    private void updateStatus() {
        lblStatus.setText("Total Students: " + studentList.size());
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ===========================
    // File I/O
    // ===========================
    private void saveDataToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Student s : studentList) {
                writer.write(s.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            showError("Save Failed: " + e.getMessage());
        }
    }

    private void loadDataFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            studentList.clear();
            while ((line = reader.readLine()) != null) {
                Student s = Student.fromCSV(line);
                if (s != null) studentList.add(s);
            }
            refreshTable();
        } catch (IOException e) {
            showError("Load Failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentManagementSystem().setVisible(true));
    }
}
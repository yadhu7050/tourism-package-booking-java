import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * BookingModalDemo - Complete Swing booking system
 */
public class BookingModalDemo {

    private JFrame frame;
    private JDialog bookingDialog;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private int currentStep = 1;

    // Form fields
    private JComboBox<PackageOption> packageTypeCombo;
    private JComboBox<String> guestsCombo;
    private JTextField checkinField;
    private JTextField checkoutField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField cardHolderField;
    private JTextField cardNumberField;
    private JTextField expiryField;
    private JTextField cvvField;
    private JTextArea specialRequestsArea;

    // Summary labels
    private JLabel summaryPackageLabel;
    private JLabel summaryGuestsLabel;
    private JLabel summaryDatesLabel;

    // Buttons
    private JButton prevBtn;
    private JButton nextBtn;
    private JButton submitBtn;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BookingModalDemo demo = new BookingModalDemo();
            demo.createAndShowGUI();
        });
    }

    private void createAndShowGUI() {
        frame = new JFrame("TourismPro - Travel & Tourism System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(249, 250, 251));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top panel with buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        topPanel.setBackground(new Color(249, 250, 251));

        JButton openBookingBtn = createStyledButton("Open Booking Modal", new Color(37, 99, 235));
        openBookingBtn.addActionListener(e -> openBookingModal(null));

        JButton viewRecentBtn = createStyledButton("View Recent Bookings", new Color(16, 185, 129));
        viewRecentBtn.addActionListener(e -> showRecentBookingsDialog());

        JButton testDbBtn = createStyledButton("Test DB Connection", new Color(139, 92, 246));
        testDbBtn.addActionListener(e -> testDatabaseConnection());

        topPanel.add(openBookingBtn);
        topPanel.add(viewRecentBtn);
        topPanel.add(testDbBtn);

        // Info area
        JTextArea info = new JTextArea();
        info.setEditable(false);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        info.setBackground(Color.WHITE);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setText("Welcome to TourismPro - Travel & Tourism Management System\n\n" +
                "Features:\n" +
                "• Complete booking workflow with 3-step process\n" +
                "• Package selection with live database integration\n" +
                "• Personal information collection\n" +
                "• Payment details (simulated)\n" +
                "• View recent bookings from database\n\n" +
                "Click 'Open Booking Modal' to start a new booking\n" +
                "Click 'View Recent Bookings' to see all bookings\n" +
                "Click 'Test DB Connection' to verify database connectivity");
        info.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(info), BorderLayout.CENTER);

        frame.setContentPane(mainPanel);
        frame.setVisible(true);

        // Test DB on startup
        testDatabaseConnection();
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(220, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void testDatabaseConnection() {
        if (DatabaseConnection.testConnection()) {
            JOptionPane.showMessageDialog(frame,
                    "Database connection successful!",
                    "Connection Test", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame,
                    "Database connection failed!\nPlease check your database.properties file and MySQL server.",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openBookingModal(Integer packageId) {
        bookingDialog = new JDialog(frame, "Book a Package", Dialog.ModalityType.APPLICATION_MODAL);
        bookingDialog.setSize(800, 600);
        bookingDialog.setLocationRelativeTo(frame);

        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setBorder(new EmptyBorder(15, 15, 15, 15));
        container.setBackground(Color.WHITE);

        // Progress indicator
        container.add(createProgressPanel(), BorderLayout.NORTH);

        // Card layout with steps
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Color.WHITE);
        cardPanel.add(createStep1Panel(), "step1");
        cardPanel.add(createStep2Panel(), "step2");
        cardPanel.add(createStep3Panel(), "step3");
        container.add(cardPanel, BorderLayout.CENTER);

        // Bottom navigation
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);
        
        JPanel navPanel = createNavButtonsPanel();
        bottom.add(navPanel, BorderLayout.CENTER);
        
        JPanel summaryPanel = createSummaryPanel();
        bottom.add(summaryPanel, BorderLayout.EAST);

        container.add(bottom, BorderLayout.SOUTH);

        bookingDialog.setContentPane(container);

        resetBookingForm();
        loadPackagesIntoCombo();
        if (packageId != null) selectPackageById(packageId);

        bookingDialog.setVisible(true);
    }

    private JPanel createProgressPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        p.setBackground(new Color(249, 250, 251));
        p.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel step1 = createProgressLabel("1", "Package Info", true);
        JLabel step2 = createProgressLabel("2", "Personal Details", false);
        JLabel step3 = createProgressLabel("3", "Confirmation", false);

        step1.setName("prog1");
        step2.setName("prog2");
        step3.setName("prog3");

        p.add(step1);
        p.add(new JLabel("→"));
        p.add(step2);
        p.add(new JLabel("→"));
        p.add(step3);

        return p;
    }

    private JLabel createProgressLabel(String number, String text, boolean active) {
        JLabel label = new JLabel(number + ". " + text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(active ? new Color(37, 99, 235) : new Color(156, 163, 175));
        return label;
    }

    private JPanel createStep1Panel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(Color.WHITE);
        
        JLabel title = new JLabel("Select Your Package");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(10, 0, 15, 0));
        p.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        packageTypeCombo = new JComboBox<>();
        guestsCombo = new JComboBox<>(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});
        guestsCombo.setSelectedIndex(1);
        checkinField = new JTextField(15);
        checkoutField = new JTextField(15);
        firstNameField = new JTextField(15);
        lastNameField = new JTextField(15);
        emailField = new JTextField(15);
        phoneField = new JTextField(15);

        c.gridx = 0; c.gridy = 0; fields.add(createLabel("Package:"), c);
        c.gridx = 1; fields.add(packageTypeCombo, c);
        
        c.gridx = 0; c.gridy = 1; fields.add(createLabel("Guests:"), c);
        c.gridx = 1; fields.add(guestsCombo, c);
        
        c.gridx = 0; c.gridy = 2; fields.add(createLabel("Check-in (YYYY-MM-DD):"), c);
        c.gridx = 1; fields.add(checkinField, c);
        
        c.gridx = 0; c.gridy = 3; fields.add(createLabel("Check-out (YYYY-MM-DD):"), c);
        c.gridx = 1; fields.add(checkoutField, c);
        
        c.gridx = 0; c.gridy = 4; fields.add(createLabel("First Name:"), c);
        c.gridx = 1; fields.add(firstNameField, c);
        
        c.gridx = 0; c.gridy = 5; fields.add(createLabel("Last Name:"), c);
        c.gridx = 1; fields.add(lastNameField, c);
        
        c.gridx = 0; c.gridy = 6; fields.add(createLabel("Email:"), c);
        c.gridx = 1; fields.add(emailField, c);
        
        c.gridx = 0; c.gridy = 7; fields.add(createLabel("Phone:"), c);
        c.gridx = 1; fields.add(phoneField, c);

        p.add(new JScrollPane(fields), BorderLayout.CENTER);
        return p;
    }

    private JPanel createStep2Panel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(Color.WHITE);
        
        JLabel title = new JLabel("Payment Information");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(10, 0, 15, 0));
        p.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        cardHolderField = new JTextField(20);
        cardNumberField = new JTextField(20);
        expiryField = new JTextField(10);
        cvvField = new JTextField(5);
        specialRequestsArea = new JTextArea(4, 20);
        specialRequestsArea.setLineWrap(true);
        specialRequestsArea.setWrapStyleWord(true);

        c.gridx = 0; c.gridy = 0; fields.add(createLabel("Card Holder:"), c);
        c.gridx = 1; fields.add(cardHolderField, c);
        
        c.gridx = 0; c.gridy = 1; fields.add(createLabel("Card Number:"), c);
        c.gridx = 1; fields.add(cardNumberField, c);
        
        c.gridx = 0; c.gridy = 2; fields.add(createLabel("Expiry (MM/YY):"), c);
        c.gridx = 1; fields.add(expiryField, c);
        
        c.gridx = 0; c.gridy = 3; fields.add(createLabel("CVV:"), c);
        c.gridx = 1; fields.add(cvvField, c);
        
        c.gridx = 0; c.gridy = 4; c.anchor = GridBagConstraints.NORTH;
        fields.add(createLabel("Special Requests:"), c);
        c.gridx = 1; c.fill = GridBagConstraints.BOTH; c.weighty = 1.0;
        fields.add(new JScrollPane(specialRequestsArea), c);

        p.add(fields, BorderLayout.CENTER);
        return p;
    }

    private JPanel createStep3Panel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(Color.WHITE);
        
        JLabel title = new JLabel("<html><h2>Confirm Your Booking</h2></html>");
        title.setBorder(new EmptyBorder(10, 0, 10, 0));
        p.add(title, BorderLayout.NORTH);

        JTextArea confirmText = new JTextArea();
        confirmText.setEditable(false);
        confirmText.setBackground(new Color(249, 250, 251));
        confirmText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        confirmText.setLineWrap(true);
        confirmText.setWrapStyleWord(true);
        confirmText.setText("Please review your booking details in the summary panel on the right.\n\n" +
                "By clicking 'Submit Booking', you agree to:\n" +
                "• Our terms and conditions\n" +
                "• Payment processing\n" +
                "• Cancellation policy\n\n" +
                "You will receive a confirmation email with all booking details.");
        confirmText.setBorder(new EmptyBorder(15, 15, 15, 15));

        p.add(confirmText, BorderLayout.CENTER);
        return p;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return label;
    }

    private JPanel createNavButtonsPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        p.setBackground(Color.WHITE);

        prevBtn = new JButton("← Previous");
        prevBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        prevBtn.setBackground(new Color(229, 231, 235));
        prevBtn.setForeground(new Color(55, 65, 81));
        prevBtn.setFocusPainted(false);
        prevBtn.setBorderPainted(false);
        prevBtn.setPreferredSize(new Dimension(130, 40));
        prevBtn.addActionListener(e -> prevStep());

        nextBtn = new JButton("Next →");
        nextBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nextBtn.setBackground(new Color(37, 99, 235));
        nextBtn.setForeground(Color.WHITE);
        nextBtn.setFocusPainted(false);
        nextBtn.setBorderPainted(false);
        nextBtn.setPreferredSize(new Dimension(130, 40));
        nextBtn.addActionListener(e -> nextStep());

        submitBtn = new JButton("Complete Booking");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        submitBtn.setBackground(new Color(16, 185, 129));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorderPainted(false);
        submitBtn.setPreferredSize(new Dimension(170, 40));
        submitBtn.addActionListener(e -> submitBooking());

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        closeBtn.setBackground(new Color(156, 163, 175));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setPreferredSize(new Dimension(100, 40));
        closeBtn.addActionListener(e -> bookingDialog.dispose());

        p.add(prevBtn);
        p.add(nextBtn);
        p.add(submitBtn);
        p.add(closeBtn);

        return p;
    }

    private JPanel createSummaryPanel() {
        JPanel summary = new JPanel(new GridBagLayout());
        summary.setBackground(new Color(219, 234, 254));
        summary.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(147, 197, 253), 2),
                new EmptyBorder(15, 15, 15, 15)
        ));
        summary.setPreferredSize(new Dimension(280, 0));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        JLabel summaryTitle = new JLabel("Booking Summary");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        summaryTitle.setForeground(new Color(30, 64, 175));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        summary.add(summaryTitle, c);

        c.gridwidth = 1;
        c.gridy++;
        summary.add(createLabel("Package:"), c);
        summaryPackageLabel = new JLabel("");
        summaryPackageLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        c.gridx = 1;
        summary.add(summaryPackageLabel, c);

        c.gridx = 0; c.gridy++;
        summary.add(createLabel("Guests:"), c);
        summaryGuestsLabel = new JLabel("");
        summaryGuestsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        c.gridx = 1;
        summary.add(summaryGuestsLabel, c);

        c.gridx = 0; c.gridy++;
        summary.add(createLabel("Dates:"), c);
        summaryDatesLabel = new JLabel("");
        summaryDatesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        c.gridx = 1;
        summary.add(summaryDatesLabel, c);

        return summary;
    }

    private void resetBookingForm() {
        currentStep = 1;
        cardLayout.show(cardPanel, "step1");

        if (packageTypeCombo.getItemCount() > 0) packageTypeCombo.setSelectedIndex(0);
        guestsCombo.setSelectedIndex(1);
        checkinField.setText("");
        checkoutField.setText("");
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        cardHolderField.setText("");
        cardNumberField.setText("");
        expiryField.setText("");
        cvvField.setText("");
        specialRequestsArea.setText("");

        updateBookingSummary();
        updateButtonsVisibility();
    }

    private void updateButtonsVisibility() {
        prevBtn.setVisible(currentStep > 1);
        nextBtn.setVisible(currentStep < 3);
        submitBtn.setVisible(currentStep == 3);
    }

    private void nextStep() {
        if (validateCurrentStep()) {
            if (currentStep < 3) currentStep++;
            cardLayout.show(cardPanel, "step" + currentStep);
            updateBookingSummary();
            updateButtonsVisibility();
            updateProgressIndicator();
        }
    }

    private void prevStep() {
        if (currentStep > 1) currentStep--;
        cardLayout.show(cardPanel, "step" + currentStep);
        updateButtonsVisibility();
        updateProgressIndicator();
    }

    private void updateProgressIndicator() {
        Container parent = cardPanel.getParent();
        Component[] components = ((JPanel)parent.getComponent(0)).getComponents();
        
        for (Component comp : components) {
            if (comp instanceof JLabel && comp.getName() != null) {
                JLabel label = (JLabel) comp;
                String name = label.getName();
                if (name.equals("prog1")) {
                    label.setForeground(currentStep >= 1 ? new Color(37, 99, 235) : new Color(156, 163, 175));
                } else if (name.equals("prog2")) {
                    label.setForeground(currentStep >= 2 ? new Color(37, 99, 235) : new Color(156, 163, 175));
                } else if (name.equals("prog3")) {
                    label.setForeground(currentStep >= 3 ? new Color(37, 99, 235) : new Color(156, 163, 175));
                }
            }
        }
    }

    private boolean validateCurrentStep() {
        if (currentStep == 1) {
            if (firstNameField.getText().trim().isEmpty()
                    || lastNameField.getText().trim().isEmpty()
                    || emailField.getText().trim().isEmpty()
                    || phoneField.getText().trim().isEmpty()
                    || checkinField.getText().trim().isEmpty()
                    || checkoutField.getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(bookingDialog,
                        "Please fill in all required fields on this step.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            try {
                LocalDate checkin = LocalDate.parse(checkinField.getText().trim());
                LocalDate checkout = LocalDate.parse(checkoutField.getText().trim());
                if (!checkout.isAfter(checkin)) {
                    JOptionPane.showMessageDialog(bookingDialog,
                            "Check-out date must be after check-in date.",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(bookingDialog,
                        "Invalid date format. Please use YYYY-MM-DD format.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }

        } else if (currentStep == 2) {
            if (cardHolderField.getText().trim().isEmpty()
                    || cardNumberField.getText().trim().isEmpty()
                    || expiryField.getText().trim().isEmpty()
                    || cvvField.getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(bookingDialog,
                        "Please fill in all payment details.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private void updateBookingSummary() {
        PackageOption pkgOpt = (PackageOption) packageTypeCombo.getSelectedItem();
        String guests = (String) guestsCombo.getSelectedItem();
        String checkin = checkinField.getText().trim();
        String checkout = checkoutField.getText().trim();

        summaryPackageLabel.setText(pkgOpt != null ? pkgOpt.title : "");
        summaryGuestsLabel.setText(guests != null ? guests : "");
        
        if (!checkin.isEmpty() && !checkout.isEmpty()) {
            summaryDatesLabel.setText("<html>" + checkin + "<br>to " + checkout + "</html>");
        } else {
            summaryDatesLabel.setText("");
        }
    }

    private void submitBooking() {
        if (!validateCurrentStep()) return;

        PackageOption sel = (PackageOption) packageTypeCombo.getSelectedItem();
        int pkgId = sel != null ? sel.id : 1;
        int guestsNum = Integer.parseInt((String) guestsCombo.getSelectedItem());
        LocalDate ci = LocalDate.parse(checkinField.getText().trim());
        LocalDate co = LocalDate.parse(checkoutField.getText().trim());

        String sql = "INSERT INTO bookings (first_name, last_name, email, phone, checkin_date, checkout_date, guests, package_id, special_requests, status) VALUES (?,?,?,?,?,?,?,?,?,'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, firstNameField.getText().trim());
            ps.setString(2, lastNameField.getText().trim());
            ps.setString(3, emailField.getText().trim());
            ps.setString(4, phoneField.getText().trim());
            ps.setDate(5, java.sql.Date.valueOf(ci));
            ps.setDate(6, java.sql.Date.valueOf(co));
            ps.setInt(7, guestsNum);
            ps.setInt(8, pkgId);
            ps.setString(9, specialRequestsArea.getText().trim().isEmpty() ? null : specialRequestsArea.getText().trim());

            ps.executeUpdate();

            int id = -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) id = keys.getInt(1);
            }

            JOptionPane.showMessageDialog(bookingDialog,
                    "Booking submitted successfully!\nBooking ID: " + id +
                    "\n\nYou will receive a confirmation email shortly.",
                    "Booking Success", JOptionPane.INFORMATION_MESSAGE);

            bookingDialog.dispose();
            showRecentBookingsDialog();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(bookingDialog,
                    "Failed to save booking: " + ex.getMessage() +
                    "\n\nPlease check your database connection.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPackagesIntoCombo() {
        List<PackageOption> options = new ArrayList<>();
        String sql = "SELECT id, title FROM packages WHERE status='ACTIVE' ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                options.add(new PackageOption(rs.getInt("id"), rs.getString("title")));
            }

        } catch (Exception ex) {
            System.err.println("Failed to load packages from database: " + ex.getMessage());
            // Fallback data
            options.add(new PackageOption(1, "Luxury Beach Resort Package"));
            options.add(new PackageOption(2, "Adventure Mountain Expedition"));
            options.add(new PackageOption(3, "Cultural Heritage Discovery"));
        }

        DefaultComboBoxModel<PackageOption> model = new DefaultComboBoxModel<>(options.toArray(new PackageOption[0]));
        packageTypeCombo.setModel(model);
    }

    private void selectPackageById(int packageId) {
        ComboBoxModel<PackageOption> model = packageTypeCombo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            PackageOption po = model.getElementAt(i);
            if (po.id == packageId) {
                packageTypeCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void showRecentBookingsDialog() {
        JDialog dlg = new JDialog(frame, "Recent Bookings", Dialog.ModalityType.MODELESS);
        dlg.setSize(1000, 500);
        dlg.setLocationRelativeTo(frame);

        String[] cols = {"ID", "Name", "Email", "Phone", "Check-in", "Check-out", "Guests", "Package ID", "Status"};
        Object[][] rows = fetchRecentBookings();

        DefaultTableModel model = new DefaultTableModel(rows, cols) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        dlg.setContentPane(scrollPane);
        dlg.setVisible(true);
    }

    private Object[][] fetchRecentBookings() {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT b.id, CONCAT(b.first_name,' ',b.last_name) AS name, b.email, b.phone, b.checkin_date, b.checkout_date, b.guests, b.package_id, b.status FROM bookings b ORDER BY b.booking_date DESC LIMIT 50";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                data.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getDate("checkin_date"),
                        rs.getDate("checkout_date"),
                        rs.getInt("guests"),
                        rs.getInt("package_id"),
                        rs.getString("status")
                });
            }

        } catch (Exception ex) {
            System.err.println("Failed to fetch bookings: " + ex.getMessage());
        }

        return data.toArray(new Object[0][]);
    }

    private static class PackageOption {
        final int id;
        final String title;

        PackageOption(int id, String title) {
            this.id = id;
            this.title = title == null ? ("Package " + id) : title;
        }

        public String toString() {
            return title;
        }
    }
}

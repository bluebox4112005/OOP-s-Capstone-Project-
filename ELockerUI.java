import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ELockerUI extends JFrame {
    private ELockerSystem system;

    public ELockerUI() {
        system = new ELockerSystem();
        showLoginScreen();
    }

    private void showLoginScreen() {
        JFrame frame = new JFrame("E-Locker Login");
        frame.setSize(350, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        JLabel userLabel = new JLabel("User ID:");
        JTextField userText = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordText = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton signUpButton = new JButton("Sign Up");

        panel.add(userLabel);
        panel.add(userText);
        panel.add(passwordLabel);
        panel.add(passwordText);
        panel.add(loginButton);
        panel.add(signUpButton);

        frame.add(panel);
        frame.setVisible(true);

        loginButton.addActionListener(e -> {
            String userId = userText.getText();
            String password = new String(passwordText.getPassword());
            if (system.signInUser(userId, password)) {
                frame.dispose();
                showDashboard();
            } else {
                JOptionPane.showMessageDialog(frame, "Login failed!");
            }
        });

        signUpButton.addActionListener(e -> {
            String userId = userText.getText();
            String name = JOptionPane.showInputDialog("Enter name:");
            String password = new String(passwordText.getPassword());
            if (name != null && !name.isEmpty()) {
                system.signUpUser(userId, name, password);
                JOptionPane.showMessageDialog(frame, "Sign-up successful!");
            }
        });
    }

    private void showDashboard() {
        JFrame dash = new JFrame("E-Locker Dashboard");
        dash.setSize(400, 300);
        dash.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dash.setLocationRelativeTo(null);
        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));

        JButton uploadDoc = new JButton("Upload Document");
        JButton viewDocs = new JButton("View My Documents");
        JButton viewFamilyDocs = new JButton("View Family Documents");
        JButton createFamily = new JButton("Create Family");
        JButton addMember = new JButton("Add Member to Family");
        JButton logout = new JButton("Logout");

        panel.add(uploadDoc);
        panel.add(viewDocs);
        panel.add(viewFamilyDocs);
        panel.add(createFamily);
        panel.add(addMember);
        panel.add(logout);

        dash.add(panel);
        dash.setVisible(true);

        uploadDoc.addActionListener(e -> {
            JTextField name = new JTextField();
            JTextField type = new JTextField();
            JTextArea content = new JTextArea(5, 20);

            Object[] inputs = {
                    "Document Name:", name,
                    "Document Type:", type,
                    "Content:", new JScrollPane(content)
            };

            int result = JOptionPane.showConfirmDialog(null, inputs, "Upload Document", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                system.uploadDocumentForUser(name.getText(), type.getText(), content.getText());
                JOptionPane.showMessageDialog(dash, "Document uploaded successfully!");
            }
        });

        viewDocs.addActionListener(e -> {
            List<Document> docs = system.listDocumentsForUser();
            if (docs != null && !docs.isEmpty()) {
                StringBuilder sb = new StringBuilder("Your Documents:\n");
                for (Document doc : docs) {
                    sb.append("ID: ").append(doc.getDocumentId()).append(", Name: ").append(doc.getDocumentName()).append(", Type: ").append(doc.getDocumentType()).append("\n");
                }
                JOptionPane.showMessageDialog(dash, sb.toString());
            } else {
                JOptionPane.showMessageDialog(dash, "No documents found.");
            }
        });

        viewFamilyDocs.addActionListener(e -> {
            List<Document> docs = system.listFamilyDocuments();
            if (docs != null && !docs.isEmpty()) {
                StringBuilder sb = new StringBuilder("Family Documents:\n");
                for (Document doc : docs) {
                    sb.append("ID: ").append(doc.getDocumentId()).append(", Name: ").append(doc.getDocumentName()).append(", Type: ").append(doc.getDocumentType()).append(", Owner: ").append(doc.getOwnerId()).append("\n");
                }
                JOptionPane.showMessageDialog(dash, sb.toString());
            } else {
                JOptionPane.showMessageDialog(dash, "No family documents found or you are not in a family.");
            }
        });

        createFamily.addActionListener(e -> {
            String famName = JOptionPane.showInputDialog("Enter family name:");
            if (famName != null && !famName.isEmpty()) {
                system.createFamily(famName);
                JOptionPane.showMessageDialog(dash, "Family created successfully!");
            }
        });

        addMember.addActionListener(e -> {
            String inviteId = JOptionPane.showInputDialog("Enter user ID to invite:");
            if (inviteId != null && !inviteId.isEmpty()) {
                system.addUserToFamily(inviteId);
                JOptionPane.showMessageDialog(dash, "Invitation sent!");
            }
        });

        logout.addActionListener(e -> {
            system.loggedInUser = null;
            dash.dispose();
            showLoginScreen();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ELockerUI());
    }
}
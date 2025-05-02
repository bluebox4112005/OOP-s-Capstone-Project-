import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

class User {
    private String userId;
    private String name;
    private String password;
    private Family family;

    public User(String userId, String name, String password) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.family = null;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void setFamily(Family family) {
        this.family = family;
    }

    public Family getFamily() {
        return family;
    }
}

class Family {
    private String familyId;
    private String familyName;
    private List<User> familyMembers;
    private List<Document> familyDocuments;

    public Family(String familyId, String familyName) {
        this.familyId = familyId;
        this.familyName = familyName;
        this.familyMembers = new ArrayList<>();
        this.familyDocuments = new ArrayList<>();
    }

    public String getFamilyId() {
        return familyId;
    }

    public String getFamilyName() {
        return familyName;
    }

    public List<User> getFamilyMembers() {
        return familyMembers;
    }

    public List<Document> getFamilyDocuments() {
        return familyDocuments;
    }

    public void addFamilyMember(User member) {
        this.familyMembers.add(member);
        member.setFamily(this);
    }

    public void shareDocument(Document document) {
        this.familyDocuments.add(document);
    }
}

class Document {
    private String documentId;
    private String documentName;
    private String documentType;
    private Date uploadDate;
    private String content;
    private String ownerId; // Changed to String

    public Document(String documentId, String documentName, String documentType, String content, String ownerId) {
        this.documentId = documentId;
        this.documentName = documentName;
        this.documentType = documentType;
        this.uploadDate = new Date();
        this.content = content;
        this.ownerId = ownerId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public String getContent() {
        return content;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void viewDocumentContent() {
        System.out.println("Document Name: " + documentName);
        System.out.println("Document Type: " + documentType);
        System.out.println("Uploaded Date: " + uploadDate);
        System.out.println("Content: \n" + content);
    }
}

public class ELockerSystem {
    private static final String DB_URL = "jdbc:sqlite:elocker.db";

    public User loggedInUser;

    public ELockerSystem() {
        this.loggedInUser = null;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Create users table
            String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "userId TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "familyId TEXT, " +
                    "FOREIGN KEY (familyId) REFERENCES families(familyId))";
            stmt.execute(sqlUsers);

            // Create families table
            String sqlFamilies = "CREATE TABLE IF NOT EXISTS families (" +
                    "familyId TEXT PRIMARY KEY, " +
                    "familyName TEXT NOT NULL)";
            stmt.execute(sqlFamilies);

            // Create documents table
            String sqlDocuments = "CREATE TABLE IF NOT EXISTS documents (" +
                    "documentId TEXT PRIMARY KEY, " +
                    "documentName TEXT NOT NULL, " +
                    "documentType TEXT NOT NULL, " +
                    "uploadDate INTEGER NOT NULL, " +
                    "content TEXT, " +
                    "ownerId TEXT NOT NULL, " +
                    "FOREIGN KEY (ownerId) REFERENCES users(userId))";
            stmt.execute(sqlDocuments);

            // Create family_documents table for sharing
            String sqlFamilyDocuments = "CREATE TABLE IF NOT EXISTS family_documents (" +
                    "familyId TEXT NOT NULL, " +
                    "documentId TEXT NOT NULL, " +
                    "PRIMARY KEY (familyId, documentId), " +
                    "FOREIGN KEY (familyId) REFERENCES families(familyId), " +
                    "FOREIGN KEY (documentId) REFERENCES documents(documentId))";
            stmt.execute(sqlFamilyDocuments);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void signUpUser(String userId, String name, String password) {
        if (findUserById(userId) != null) {
            System.out.println("User ID already exists. Please choose another.");
            return;
        }
        String hashedPassword = hashPassword(password); // Basic hashing - REPLACE WITH BCRYPT
        String sql = "INSERT INTO users(userId, name, password) VALUES(?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, name);
            pstmt.setString(3, hashedPassword);
            pstmt.executeUpdate();
            System.out.println("Sign-up successful!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean signInUser(String userId, String password) {
        String sql = "SELECT userId, name, password, familyId FROM users WHERE userId = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (verifyPassword(password, storedHash)) { // Basic verification - REPLACE WITH BCRYPT
                    loggedInUser = new User(rs.getString("userId"), rs.getString("name"), storedHash);
                    String familyId = rs.getString("familyId");
                    if (familyId != null) {
                        loggedInUser.setFamily(new Family(familyId, "")); // Family name not needed here
                    }
                    return true;
                } else {
                    System.out.println("Invalid password.");
                }
            } else {
                System.out.println("User not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void createFamily(String familyName) {
        if (loggedInUser == null) {
            System.out.println("Please sign in first to create a family.");
            return;
        }
        String familyId = generateUniqueFamilyId();
        String sqlFamily = "INSERT INTO families(familyId, familyName) VALUES(?, ?)";
        String sqlUser = "UPDATE users SET familyId = ? WHERE userId = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmtFamily = conn.prepareStatement(sqlFamily);
             PreparedStatement pstmtUser = conn.prepareStatement(sqlUser)) {

            pstmtFamily.setString(1, familyId);
            pstmtFamily.setString(2, familyName);
            pstmtFamily.executeUpdate();

            pstmtUser.setString(1, familyId);
            pstmtUser.setString(2, loggedInUser.getUserId());
            pstmtUser.executeUpdate();

            loggedInUser.setFamily(new Family(familyId, familyName));

            System.out.println("Family '" + familyName + "' created successfully with ID: " + familyId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addUserToFamily(String userIdToInvite) {
        if (loggedInUser == null) {
            System.out.println("Please sign in first.");
            return;
        }
        if (loggedInUser.getFamily() == null) {
            System.out.println("You must be part of a family to invite members. Create or join a family first.");
            return;
        }
        String findUserSql = "SELECT * FROM users WHERE userId = ?";
        User userToInvite = null;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(findUserSql)) {
            pstmt.setString(1, userIdToInvite);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                userToInvite = new User(
                    rs.getString("userId"),
                    rs.getString("name"), 
                    rs.getString("password")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (userToInvite == null) {
            System.out.println("User with ID '" + userIdToInvite + "' not found.");
            return;
        }
        if (userToInvite.getFamily() != null) {
            System.out.println("User '" + userIdToInvite + "' is already part of a family.");
            return;
        }

        String updateSql = "UPDATE users SET familyId = ? WHERE userId = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setString(1, loggedInUser.getFamily().getFamilyId());
            pstmt.setString(2, userIdToInvite);
            pstmt.executeUpdate();

            userToInvite.setFamily(loggedInUser.getFamily());
            loggedInUser.getFamily().addFamilyMember(userToInvite);

            System.out.println("User '" + userToInvite.getName() + "' added to your family '" + loggedInUser.getFamily().getFamilyName() + "'.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void shareAllDocumentsWithFamily(String userId, String familyId) throws SQLException {
        String sql = "INSERT INTO family_documents (familyId, documentId) SELECT ?, documentId FROM documents WHERE ownerId = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, familyId);
            pstmt.setString(2, userId);
            pstmt.executeUpdate();
        }
    }

    public void uploadDocumentForUser(String documentName, String documentType, String content) {
        if (loggedInUser == null) {
            System.out.println("Please sign in first to upload documents.");
            return;
        }
        String documentId = generateUniqueDocumentId();
        String sql = "INSERT INTO documents(documentId, documentName, documentType, uploadDate, content, ownerId) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, documentId);
            pstmt.setString(2, documentName);
            pstmt.setString(3, documentType);
            pstmt.setLong(4, new Date().getTime());
            pstmt.setString(5, content);
            pstmt.setString(6, loggedInUser.getUserId());
            pstmt.executeUpdate();

            if (loggedInUser.getFamily() != null) {
                shareDocumentWithFamily(documentId, loggedInUser.getFamily().getFamilyId());
            }

            System.out.println("Document '" + documentName + "' uploaded successfully with ID: " + documentId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void shareDocumentWithFamily(String documentId, String familyId) {
        String sql = "INSERT INTO family_documents(familyId, documentId) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, familyId);
            pstmt.setString(2, documentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String hashPassword(String password) {
        // Basic hashing - in production, use BCrypt or similar
        return String.valueOf(password.hashCode());
    }

    private boolean verifyPassword(String inputPassword, String storedHash) {
        return hashPassword(inputPassword).equals(storedHash);
    }

    private String generateUniqueFamilyId() {
        return UUID.randomUUID().toString();
    }

    private String generateUniqueDocumentId() {
        return UUID.randomUUID().toString();
    }

    private User findUserById(String userId) {
        String sql = "SELECT userId, name, password FROM users WHERE userId = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getString("userId"), 
                              rs.getString("name"), 
                              rs.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Document> listDocumentsForUser() {
        if (loggedInUser == null) {
            System.out.println("Please sign in first.");
            return new ArrayList<>();
        }
        List<Document> documents = new ArrayList<>();
        String sql = "SELECT * FROM documents WHERE ownerId = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loggedInUser.getUserId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                documents.add(new Document(
                    rs.getString("documentId"),
                    rs.getString("documentName"),
                    rs.getString("documentType"),
                    rs.getString("content"),
                    rs.getString("ownerId")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return documents;
    }

    public List<Document> listFamilyDocuments() {
        if (loggedInUser == null || loggedInUser.getFamily() == null) {
            System.out.println("Please sign in and join a family first.");
            return new ArrayList<>();
        }
        List<Document> documents = new ArrayList<>();
        String sql = "SELECT d.* FROM documents d JOIN family_documents fd ON d.documentId = fd.documentId WHERE fd.familyId = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loggedInUser.getFamily().getFamilyId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                documents.add(new Document(
                    rs.getString("documentId"),
                    rs.getString("documentName"),
                    rs.getString("documentType"),
                    rs.getString("content"),
                    rs.getString("ownerId")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return documents;
    }

    public Document viewDocumentForUser(String documentId) {
        if (loggedInUser == null) {
            System.out.println("Please sign in first.");
            return null;
        }
        String sql = "SELECT * FROM documents WHERE documentId = ? AND (ownerId = ? OR documentId IN (SELECT documentId FROM family_documents WHERE familyId = ?))";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, documentId);
            pstmt.setString(2, loggedInUser.getUserId());
            pstmt.setString(3, loggedInUser.getFamily() != null ? loggedInUser.getFamily().getFamilyId() : "");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Document(
                    rs.getString("documentId"),
                    rs.getString("documentName"),
                    rs.getString("documentType"),
                    rs.getString("content"),
                    rs.getString("ownerId")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
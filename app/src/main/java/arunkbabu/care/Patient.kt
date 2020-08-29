package arunkbabu.care;

/**
 * Patient Data Model class. Used for storing patient details
 */
public class Patient {

    private String id;
    private String documentId;
    private String reportId;
    private String name;
    private String profilePicturePath;
    private int reportType;

    public Patient() {
        // Required Empty Constructor
    }

    public Patient(String id, String documentId, String reportId, String name, String profilePicturePath, int reportType) {
        this.id = id;
        this.documentId = documentId;
        this.reportId = reportId;
        this.name = name;
        this.profilePicturePath = profilePicturePath;
        this.reportType = reportType;
    }

    /**
     * Returns the id of the patient
     * @return int ID of the patient
     */
    public String getPatientId() {
        return id;
    }

    /**
     * Set the id of the patient
     * @param id The patient id
     */
    public void setPatientId(String id) {
        this.id = id;
    }

    /**
     * Returns the id of the document of the patient's request
     * @return String The document id
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * Set the patient's request document id
     * @param documentId The document id of the patient's request
     */
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    /**
     * Returns the name of the patient
     * @return String name of the patient
     */
    public String getPatientName() {
        return name;
    }


    /**
     * Set the name of the patient
     * @param name The patient name
     */
    public void setPatientName(String name) {
        this.name = name;
    }

    /**
     * Returns the type of report the patient has generated (Ex: Pain, Fever etc..)
     * @return String  Type of report the patient has generated (Ex: Pain, Fever etc..)
     */
    public int getReportType() {
        return reportType;
    }

    /**
     * Set the reportType of the patient
     * @param reportType int The type of report the patient has generated (Ex: Pain, Fever etc..)
     */
    public void setReportType(int reportType) {
        this.reportType = reportType;
    }

    /**
     * Returns the location of the profile picture of the patient
     * @return String  Path of profile picture
     */
    public String getProfilePicture() {
        return profilePicturePath;
    }

    /**
     * Set the profile picture path of the patient
     * @param profilePicturePath int The type of report the patient has generated (Ex: Pain, Fever etc..)
     */
    public void setProfilePicture(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }


    /**
     * The report id of the problem report in patient's database
     * @param reportId The problem report's id
     */
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    /**
     * Get the id of the problem report in patient's database
     * @return String  The id of the problem report
     */
    public String getReportId() {
        return reportId;
    }
}
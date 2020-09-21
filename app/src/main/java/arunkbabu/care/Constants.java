package arunkbabu.care;

/**
 * Common Public constants that are shared across the app is stored here
 */
public class Constants {
    public static final int NULL_INT = -1;

    // Request Activity
    public static final String PATIENT_REPORT_TYPE_KEY = "key_patient_report_type";
    public static final String PATIENT_ID_KEY = "key_patient_id";
    public static final String PATIENT_REPORT_ID_KEY = "key_patient_report_id";
    public static final String PATIENT_REQUEST_ID_KEY = "key_patient_request_id";
    public static final String DOCTOR_NAME_ID_KEY = "key_doctor_name_id";
    public static final String DOCTOR_SPECIALITY_QUALIFICATION_KEY = "key_doctor_speciality_qualification_extra";
    public static final String DOCTOR_HOSPITAL_NAME_KEY = "key_doctor_hospital_name_extra";

    public static final int REPORT_TYPE_OTHER = 1004;

    // Database Related
    public static final String COLLECTION_USERS = "Users";
    public static final String COLLECTION_PATIENT_REQUEST = "PatientRequest";
    public static final String COLLECTION_DOCTOR_REPORT = "DoctorReports";
    public static final String COLLECTION_PROBLEM_REPORT = "ProblemReports";
    public static final String COLLECTION_REPORT_DETAILS = "ReportDetails";
    public static final String COLLECTION_DOCTORS_LIST = "DoctorsList";

    public static final String ROOT_CHATS = "Chats";
    public static final String ROOT_MESSAGES = "Messages";

    public static final String FIELD_MESSAGE = "msg";
    public static final String FIELD_SENDER_ID = "senderId";
    public static final String FIELD_RECEIVER_ID = "receiverId";
    public static final String FIELD_MSG_TIMESTAMP = "msgTimestamp";
    public static final String FIELD_CHAT_TIMESTAMP = "chatTimestamp";
    public static final String FIELD_LAST_MESSAGE = "lastMessage";
    public static final String FIELD_STATUS = "status";

    public static final String FIELD_USER_TYPE = "user_type";
    public static final String FIELD_FULL_NAME = "full_name";
    public static final String FIELD_SEX = "sex";
    public static final String FIELD_CONTACT_NUMBER = "contact_number";
    public static final String FIELD_DOB = "dob";
    public static final String FIELD_HEIGHT = "height";
    public static final String FIELD_WEIGHT = "weight";
    public static final String FIELD_PROFILE_PICTURE = "profilePicture";
    public static final String FIELD_DOC_REG_ID = "doc_reg_id";
    public static final String FIELD_PREFERRED_DOCTOR = "preferredDoctorId";

    public static final String FIELD_PATIENT_ID = "patientId";
    public static final String FIELD_PATIENT_NAME = "patientName";
    public static final String FIELD_REPORT_TYPE = "reportType";
    public static final String FIELD_REPORT_ID = "reportId";
    public static final String FIELD_REQUEST_TIMESTAMP = "requestTimestamp";
    public static final String FIELD_IS_A_VALID_REQUEST = "isValid";
    public static final String FIELD_IS_A_VALID_DOCTOR_REPORT = "isValidReport";
    public static final String FIELD_REPORT_TIMESTAMP = "reportTimestamp";
    public static final String FIELD_PROBLEM_DESCRIPTION = "problemDescription";
    public static final String FIELD_IMAGE_UPLOADS = "uploadedImages";

    public static final String FIELD_DOCTOR_MEDICINES = "medicines";
    public static final String FIELD_DOCTOR_MEDICATION_INSTRUCTIONS = "medication_instructions";
    public static final String FIELD_DOCTOR_SPECIALITY = "speciality";
    public static final String FIELD_DOCTOR_EXPERIENCE = "experience";
    public static final String FIELD_DOCTOR_QUALIFICATIONS = "qualification";
    public static final String FIELD_DOCTOR_FELLOWSHIPS = "fellowships";
    public static final String FIELD_WORKING_HOSPITAL_NAME = "hospital";
    public static final String FIELD_SEARCH_NAME = "searchName";
    public static final String FIELD_SPECIALITY_QUALIFICATIONS = "sq";

    // Cloud Storage Related
    public static final String DIRECTORY_PROFILE_PICTURE = "/ProfilePictures/";
    public static final String DIRECTORY_SENT_IMAGES = "/SentImages/";
    public static final String PROFILE_PICTURE_FILE_NAME = "IMG_USER_PROFILE_PICTURE";

    public static final int SEX_FEMALE = 0;
    public static final int SEX_MALE = 1;

    public static final String IMG_FORMAT_JPG = ".jpg";
    public static final int JPG_QUALITY = 80;
    public static final int DP_UPLOAD_SIZE = 960;

    public static final int USER_TYPE_PATIENT = 50;
    public static final int USER_TYPE_DOCTOR = 51;

    public static final String FIELD_ACCOUNT_VERIFIED = "activated";
}

package arunkbabu.care;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import arunkbabu.care.dialogs.ErrorDialog;

public class Utils {
    /**
     * App lvl variable used to indicate the User Type
     */
    public static int userType;

    /**
     * App lvl variable used to indicate that the User is currently in VerificationEmailFragment
     */
    public static boolean inVerificationEmailFragment;

    /**
     * Checks whether the provided email is valid
     * @param email The email id to be verified
     * @return True if the email is valid
     */
    public static boolean verifyEmail(String email) {
        String mailFormat = "^([a-zA-B 0-9.-]+)@([a-zA-B 0-9]+)\\.([a-zA-Z]{2,8})(\\.[a-zA-Z]{2,8})?$";
        return !email.matches(mailFormat);
    }

    /**
     * Creates an image file with the supplied format in the apps Private directory
     * @param context The context application context
     * @param fileFormat The format of the image file
     * @return File The Image file
     * @throws IOException If the file was not created
     */
    public static File createImageFile(Context context, String fileFormat) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(new Date());
        String fileName = "IMG_" + timeStamp + "_";
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(fileName, fileFormat, directory);
    }

    /**
     * Deletes all the files in the app's private directory of PICTURES
     * @param context The Application Context
     * @return TRUE if file deletion is success
     */
    public static boolean deleteAllPrivateFiles(Context context) {
        boolean isDeletionSuccess = false;

        File file = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (file != null && file.isDirectory() && file.exists()) {
            // Get the list of files in the directory
            String[] children = file.list();
            if (children != null) {
                for (String child : children) {
                    // Delete all files
                    isDeletionSuccess = new File(file, child).delete();
                }
            }
        }
        return isDeletionSuccess;
    }

    /**
     * Deletes all the files in the directory of the specified file's path
     * @param filePath The path of the file in which all files in the directory needs to be deleted (can include path separators)
     * @return TRUE if file deletion is success
     */
    public static boolean deleteAllFiles(String filePath) {
        boolean isDeletionSuccess = false;
        if (filePath != null) {
            // Get the directory path excluding any actual files if supplied
            int lastIndex = filePath.lastIndexOf("/");
            filePath = filePath.substring(0, lastIndex);

            File file = new File(filePath);
            if (file.isDirectory() && file.exists()) {
                // Get the list of files in the directory
                String[] children = file.list();
                if (children != null) {
                    for (String child : children) {
                        // Delete all files
                        isDeletionSuccess = new File(file, child).delete();
                    }
                }
            }
        }
        return isDeletionSuccess;
    }

    /**
     * Converts the sex value to corresponding human readable String (Ex. Male, Female)
     * @param sexValue The sex value
     * @return String  Sex string like Male, Female...
     */
    public static String toSexString(int sexValue) {
        String sex;
        switch (sexValue) {
            case Constants.SEX_MALE:
                sex = "Male";
                break;
            case Constants.SEX_FEMALE:
                sex = "Female";
                break;
            default:
                sex = "";
        }
        return sex;
    }

    /**
     * Converts the given sex value to corresponding human readable String (Ex. Male, Female)
     * @param sexString The sex String
     * @return int  One of {@link Constants#SEX_MALE}, {@link Constants#SEX_MALE}, {@link Constants#NULL_INT}
     */
    public static int toSexInt(String sexString) {
        int sex;
        switch (sexString.toLowerCase()) {
            case "male":
                sex = Constants.SEX_MALE;
                break;
            case "female":
                sex = Constants.SEX_FEMALE;
                break;
            default:
                sex = Constants.NULL_INT;
        }
        return sex;
    }

    /**
     * Converts the report type integer to corresponding human readable String
     * @param reportType The report type integer
     * @return String  The human readable form of report type
     */
    public static String toReportTypeString(int reportType) {
        String reportString;
        switch (reportType) {
            case Constants.REPORT_TYPE_OTHER:
                reportString = "Other Untoward Event";
                break;
            default:
                reportString = "Unknown";
        }
        return reportString;
    }

    /**
     * Converts the given specialityId to the corresponding Speciality Name
     * @param specialityId The speciality id integer
     * One of: {@link Constants#SPECIALITY_GENERAL}, {@link Constants#SPECIALITY_CARDIOLOGIST},
     * {@link Constants#SPECIALITY_DENTIST}, {@link Constants#SPECIALITY_ENDOCRINOLOGIST},
     * {@link Constants#SPECIALITY_ENT}, {@link Constants#SPECIALITY_NEUROLOGIST},
     * {@link Constants#SPECIALITY_HEPATOLOGIST}, {@link Constants#SPECIALITY_NEPHROLOGIST},
     * {@link Constants#SPECIALITY_ONCOLOGIST}, {@link Constants#SPECIALITY_OPHTHALMOLOGIST},
     * {@link Constants#SPECIALITY_PSYCHOLOGIST}, {@link Constants#SPECIALITY_PULMONOLOGIST},
     * {@link Constants#SPECIALITY_RHEUMATOLOGIST}, {@link Constants#SPECIALITY_UROLOGIST},
     * {@link Constants#SPECIALITY_PEDIATRICIAN}, {@link Constants#SPECIALITY_OTHER}
     * @return String  The human readable form of report type
     */
    public static String toSpecialityName(int specialityId) {
        String specialityString;
        switch (specialityId) {
            case Constants.SPECIALITY_GENERAL:
                specialityString = "General Medicine";
                break;
            case Constants.SPECIALITY_ONCOLOGIST:
                specialityString = "Oncologist";
                break;
            case Constants.SPECIALITY_PEDIATRICIAN:
                specialityString = "Pediatrician";
                break;
            case Constants.SPECIALITY_ENDOCRINOLOGIST:
                specialityString = "Endocrinologist";
                break;
            case Constants.SPECIALITY_HEPATOLOGIST:
                specialityString = "Hepatologist";
                break;
            case Constants.SPECIALITY_ENT:
                specialityString = "ENT";
                break;
            case Constants.SPECIALITY_UROLOGIST:
                specialityString = "Urologist";
                break;
            case Constants.SPECIALITY_NEPHROLOGIST:
                specialityString = "Nephrologist";
                break;
            case Constants.SPECIALITY_NEUROLOGIST:
                specialityString = "Neurologist";
                break;
            case Constants.SPECIALITY_CARDIOLOGIST:
                specialityString = "Cardiologist";
                break;
            case Constants.SPECIALITY_PULMONOLOGIST:
                specialityString = "Pulmonologist";
                break;
            case Constants.SPECIALITY_PSYCHOLOGIST:
                specialityString = "Psychologist";
                break;
            case Constants.SPECIALITY_OPHTHALMOLOGIST:
                specialityString = "Ophthalmologist";
                break;
            case Constants.SPECIALITY_DENTIST:
                specialityString = "Dentist";
                break;
            case Constants.SPECIALITY_RHEUMATOLOGIST:
                specialityString = "Rheumatologist";
                break;
            case Constants.SPECIALITY_OTHER:
                specialityString = "Other";
                break;
            default:
                specialityString = "Unknown";
        }
        return specialityString;
    }

    /**
     * Hides the virtual keyboard from the activity
     * @param activity The current activity where the virtual keyboard exists
     */
    public static void closeSoftInput(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View v = activity.getCurrentFocus();
        if (v == null)
            v = new View(activity);

        if (inputMethodManager != null)
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    /**
     * Calculates the age of the person relative to the birth date
     * @param dayOfMonth The day in which the person was born
     * @param month The month in which the person was born
     * @param year The year in which the person was born
     * @return String  The current age of the person in years relative to his date of birth as a String
     */
    public static int calculateAge(int dayOfMonth, int month, int year) {
        Calendar dateOfBirth = Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        dateOfBirth.set(year, month, dayOfMonth);
        int age =  now.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);

        // Take the Birth Day in account. So that the person is still 1 year less compared to the
        // birth year unless his birthday is reached
        if (now.get(Calendar.DAY_OF_YEAR) < dateOfBirth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

    /**
     * Converts the time as UTC milliseconds from the epoch to a human readable date String
     * @return String: The Date as a human readable String
     */
    public static String convertEpochToDateString(long epoch) {
        Date date = new Date(epoch);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

        return sdf.format(date);
    }

    /**
     * Get the time in (HH:mm) or (hh:mm) format from the given minutes
     * @param minutes The minutes past midnight of the time
     * @param is24HrFormat Whether to return 24 hr or 12 hr format.
     *                     True to return it in 24 hr format; false it in 12 hr format
     * @return String: The human readable time string in (HH:mm) or (hh:mm) format
     */
    public static String getTimeString(int minutes, boolean is24HrFormat) {
        int hour = minutes / 60;
        int minute = minutes % 60;

        // Format the time properly first
        StringBuilder timeBuilder = new StringBuilder();
        if (hour < 10)
            timeBuilder.append(0);

        timeBuilder.append(hour);
        timeBuilder.append(" : ");

        if (minute < 10)
            timeBuilder.append(0);

        timeBuilder.append(minute);


        String time = timeBuilder.toString();
        if (!is24HrFormat) {
            // 12 Hr Format
            try {
            SimpleDateFormat sdf24Hr = new SimpleDateFormat("HH : mm", Locale.UK);
            SimpleDateFormat sdf12Hr = new SimpleDateFormat("hh : mm a", Locale.UK);
            final Date date24Hr = sdf24Hr.parse(time);
            time = sdf12Hr.format(date24Hr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return time;
    }

    /**
     * Calculates the Body Mass Index from the given height and weight
     * @param weight Weight in Kilograms
     * @param height Height in Centimeters
     * @return String: The Body Mass Index (BMI), with at-most 2 decimal places
     */
    public static String calculateBMI(String weight, String height) {
        // Calculate BMI if height and weight are present
        double weightInKg = Double.parseDouble(weight);
        double heightInMetre = Double.parseDouble(height) / 100;
        double bmi = weightInKg / Math.pow(heightInMetre, 2);

        DecimalFormat f = new DecimalFormat("##.##");
        return f.format(bmi);
    }

    /**
     * Calculates the size in MegaPixels from the given image Width & Height
     * @param height Height of the image
     * @param width Width of the image
     * @return Size in MegaPixels
     */
    private static float effectiveMegaPixels(float height, float width) {
        return (height*width) / 1000000;
    }

    /**
     * Get the real path from a uri returned by the Storage Access Framework
     * @param context The application context
     * @param imageUri The Uri to be parsed
     * @return The Real path of the image
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String getStorageAccessFrameworkUri(Context context, Uri imageUri){
        // Will return "image:x*"
        String wholeID = DocumentsContract.getDocumentId(imageUri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        column, sel, new String[]{ id }, null);

        String filePath = "";

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }

        cursor.close();
        return filePath;
    }

    /**
     * Retrieves the real path from a MediaStore Uri (content://)
     * @param context The application Context
     * @param contentUri The Uri to be converted to path
     * @return The real path of the MediaStore Uri
     */
    private static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Returns the image's resize dimensions from a MediaStore (content://) Uri or file (file://) Uri so
     * that it can be used to load the image as 2MP FHD image or in efficient size
     * @param context The Application Context
     * @param imageUri The Uri of the image
     * @return The resize dimensions for the image
     */
    public static Dimension getResizeDimensions(Context context, Uri imageUri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // Intelligently executes appropriate form of getting resize dimensions respective to the type of Uri
        if (imageUri.toString().startsWith("content://com.android.providers")) {
            BitmapFactory.decodeFile(new File(getStorageAccessFrameworkUri(context, imageUri)).getAbsolutePath(), options);
        } else if (imageUri.toString().startsWith("content://")) {
            BitmapFactory.decodeFile(new File(getRealPathFromURI(context, imageUri)).getAbsolutePath(), options);
        } else {
            BitmapFactory.decodeFile(new File(imageUri.getPath()).getAbsolutePath(), options);
        }
        float height = options.outHeight;
        float width = options.outWidth;
        options.inJustDecodeBounds = false;

        float megaPixels = effectiveMegaPixels(height, width);
        float newHeight, newWidth;

        // IF the Original Image dimensions is less than 2 MP then don't scale
        if (megaPixels > 0.912) {
            if (width > height) {
                // landscape
                newHeight = (height / width)*950;
                newWidth = 950;
            } else if (height > width) {
                // portrait
                newHeight = 960;
                newWidth = (width / height)*960;
            } else {
                newHeight = height/2;
                newWidth = width/2;
            }
        } else {
            newHeight = height;
            newWidth = width;
        }

        return new Dimension((int)newHeight, (int)newWidth);
    }

    /**
     * Show the error dialog
     * @param message The error message to be shown
     * @param positiveButtonLabel The label of the positive button. If empty, the button will be disabled
     * @param negativeButtonLabel The label of the negative button. If empty, the button will be disabled
     * @param activity The activity to show this dialog
     * @return An instance of the ErrorDialog Fragment
     */
    public static ErrorDialog showErrorDialog(AppCompatActivity activity, @NonNull String message,
                                              String positiveButtonLabel, String negativeButtonLabel) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        Fragment prev = activity.getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        ErrorDialog dialog = new ErrorDialog(activity, message, positiveButtonLabel, negativeButtonLabel);
        dialog.show(ft, "dialog");
        return dialog;
    }

    /**
     * Check for internet availability
     * @return True: if internet is available; False otherwise
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null)
            return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
        else
            return false;
    }
}
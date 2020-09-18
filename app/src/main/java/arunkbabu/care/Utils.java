package arunkbabu.care;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import arunkbabu.care.dialogs.ErrorDialog;
import arunkbabu.care.views.CircularImageView;

public class Utils {
    private static CustomTarget<Drawable> mTarget;

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
     * One of: Constants in {@link Speciality}
     * @return String  The human readable form of report type
     */
    public static String toSpecialityName(int specialityId) {
        String specialityString;
        switch (specialityId) {
            case Speciality.GENERAL:
                specialityString = "General Medicine";
                break;
            case Speciality.ONCOLOGIST:
                specialityString = "Oncologist";
                break;
            case Speciality.PEDIATRICIAN:
                specialityString = "Pediatrician";
                break;
            case Speciality.ENDOCRINOLOGIST:
                specialityString = "Endocrinologist";
                break;
            case Speciality.HEPATOLOGIST:
                specialityString = "Hepatologist";
                break;
            case Speciality.ENT:
                specialityString = "ENT";
                break;
            case Speciality.UROLOGIST:
                specialityString = "Urologist";
                break;
            case Speciality.NEPHROLOGIST:
                specialityString = "Nephrologist";
                break;
            case Speciality.NEUROLOGIST:
                specialityString = "Neurologist";
                break;
            case Speciality.CARDIOLOGIST:
                specialityString = "Cardiologist";
                break;
            case Speciality.PULMONOLOGIST:
                specialityString = "Pulmonologist";
                break;
            case Speciality.PSYCHOLOGIST:
                specialityString = "Psychologist";
                break;
            case Speciality.OPHTHALMOLOGIST:
                specialityString = "Ophthalmologist";
                break;
            case Speciality.DENTIST:
                specialityString = "Dentist";
                break;
            case Speciality.RHEUMATOLOGIST:
                specialityString = "Rheumatologist";
                break;
            case Speciality.OTHER:
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy", Locale.UK);
        sdf.setTimeZone(TimeZone.getDefault());

        return sdf.format(date);
    }

    /**
     * Converts the timestamp to a descriptive logical date string. Logical means the dates will
     * be like "Today", "Yesterday", "Tue, 19 Jun 1997" depending on the current date
     * @param timestamp The epoch timestamp to be converted
     * @return String: The date string (Ex: Tuesday, 10 June 1998)
     */
    public static String getLogicalDateString(long timestamp) {
        String logicalDate;
        Date date = new Date(timestamp);
        Date currentDate = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MM yyyy", Locale.UK);
        SimpleDateFormat df = new SimpleDateFormat("dd", Locale.UK);
        SimpleDateFormat myf = new SimpleDateFormat("MM yyyy", Locale.UK);
        sdf.setTimeZone(TimeZone.getDefault());

        String dateStr = sdf.format(date);
        String currentStr = sdf.format(currentDate);
        int day = Integer.parseInt(df.format(date));
        int currDay = Integer.parseInt(df.format(currentDate));
        String mnthYr = myf.format(date);
        String currMnthYr = myf.format(currentDate);

        if (dateStr.equals(currentStr)) {
            logicalDate = "Today";
        } else if ((day == currDay - 1) && mnthYr.equals(currMnthYr)) {
            logicalDate = "Yesterday";
        } else {
            logicalDate = dateStr;
        }

        return logicalDate;
    }

    /**
     * Converts the timestamp to a descriptive logical short date string. Logical means the dates
     * will be like "Tue", "18 Mar" depending on the current date
     * @param timestamp The epoch timestamp to be converted
     * @return String: The date string
     */
    public static String getLogicalShortDate(long timestamp) {
        Date date = new Date(timestamp);
        Calendar c1 = Calendar.getInstance(TimeZone.getDefault());
        Calendar c2 = Calendar.getInstance(TimeZone.getDefault());
        c1.setTimeInMillis(timestamp);
        c2.setTimeInMillis(System.currentTimeMillis());

        SimpleDateFormat sdf;
        if (c1.get(Calendar.WEEK_OF_YEAR) == c2.get(Calendar.WEEK_OF_YEAR)) {
            // If same week then show only the day name (Ex: Mon, Tue)
            sdf = new SimpleDateFormat("EEE", Locale.UK);
        } else {
            // Else show Date & Month (Ex: 10 Sep)
            sdf = new SimpleDateFormat("dd MMM", Locale.UK);
        }
        sdf.setTimeZone(TimeZone.getDefault());

        return sdf.format(date);
    }

    /**
     * Converts the time as UTC milliseconds from the epoch to a day
     * @param timestamp The timestamp to convert to a day
     * @return int: The Day
     */
    public static int getDay(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.UK);
        sdf.setTimeZone(TimeZone.getDefault());

        return Integer.parseInt(sdf.format(date));
    }

    /**
     * Get the time in (HH:mm) or (hh:mm) format based on system preferences
     * (ie 24Hr/12Hr format based on the settings in Android System)
     * @param epoch The epoch timestamp
     * @param applicationContext The Application Context
     * @return String: The human readable time string in (HH:mm) or (hh:mm) format
     */
    public static String getTimeString(Context applicationContext, long epoch) {
        Date d = new Date(epoch);
        DateFormat tf = android.text.format.DateFormat.getTimeFormat(applicationContext);
        return tf.format(d);
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

    /**
     * Starts the Reveal Layout Animation on recycler view items
     * @param context The context
     * @param recyclerView The recyclerview to run the animation on
     * @param reverseAnimation Whether to reverse the animation effect.
     *                        If True then the animation will play in the reverse order
     */
    public static void runStackedRevealAnimation(Context context, RecyclerView recyclerView, boolean reverseAnimation) {
        LayoutAnimationController controller;
        if (reverseAnimation) {
            controller = AnimationUtils.loadLayoutAnimation(context, R.anim.stacked_reveal_layout_animation_reverse);
        } else {
            controller = AnimationUtils.loadLayoutAnimation(context, R.anim.stacked_reveal_layout_animation);
        }
        recyclerView.setLayoutAnimation(controller);
        recyclerView.scheduleLayoutAnimation();
    }

    /**
     * Starts the Pull-Down Layout Animation on recycler view items
     * @param context The context
     * @param recyclerView The recyclerview to run the animation on
     */
    public static void runPullDownAnimation(Context context, RecyclerView recyclerView) {
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.pull_down_layout_animation);
        recyclerView.setLayoutAnimation(controller);
        recyclerView.scheduleLayoutAnimation();
    }

    /**
     * Loads the image to image view from the given String path
     * @param imageURL String: The URL of the image to load
     * @param context The context
     * @param circularImageView The custom CircularImageView where the profile picture needs to be loaded
     */
    public static void loadDpToView(Context context, String imageURL, CircularImageView circularImageView) {
        mTarget = new CustomTarget<Drawable>() {
            @Override
            public void onLoadStarted(@Nullable Drawable placeholder) {
                circularImageView.showProgressBar();
            }

            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                circularImageView.hideProgressBar();
                circularImageView.setImageDrawable(resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                circularImageView.hideProgressBar();
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                circularImageView.setImageDrawable(null);
            }
        };

        Glide.with(context).load(imageURL).into(mTarget);
    }

    /**
     * Loads the image to image view from the given Uri
     * @param imageUri Uri of the image to load
     * @param context The context
     * @param circularImageView The custom CircularImageView where the profile picture needs to be loaded
     */
    public static void loadDpToView(Context context, Uri imageUri, CircularImageView circularImageView) {
        mTarget = new CustomTarget<Drawable>() {
            @Override
            public void onLoadStarted(@Nullable Drawable placeholder) {
                circularImageView.showProgressBar();
            }

            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                circularImageView.hideProgressBar();
                circularImageView.setImageDrawable(resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                circularImageView.hideProgressBar();
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                circularImageView.setImageDrawable(null);
            }
        };

        Glide.with(context).load(imageUri).into(mTarget);
    }
}
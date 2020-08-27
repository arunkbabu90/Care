package arunkbabu.care.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerDialog extends DialogFragment implements android.app.DatePickerDialog.OnDateSetListener {

    private DateChangeListener mDateChangeListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new android.app.DatePickerDialog(getActivity(), this, year, month, day);
    }

    /**
     * Register a callback to be invoked when a new date is picked using the Date Picker
     * @param listener The callback that will be run
     */
    public void setDateChangeListener(DateChangeListener listener) {
        mDateChangeListener = listener;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, dayOfMonth + 1);

        mDateChangeListener.onDateSet(c.getTimeInMillis());
    }

    /**
     * Interface definition for a callback to be invoked when a date is picked using the Date Picker
     */
    public interface DateChangeListener {
        /**
         * Called when a date is set in the date picker
         * @param epoch The epoch timestamp at the selected date
         */
        void onDateSet(long epoch);
    }
}
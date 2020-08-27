package arunkbabu.care.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimePickerDialog extends DialogFragment
        implements android.app.TimePickerDialog.OnTimeSetListener {

    private TimeChangeListener mTimeChangeListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Use the current time as the default time in the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new android.app.TimePickerDialog(getContext(), this, hour, minute, false);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        int timeInMinutes = (hourOfDay * 60) + minute;
        mTimeChangeListener.onTimeSet(timeInMinutes);
    }

    /**
     * Register a callback to be invoked when a new time is picked using the Time Picker
     * @param listener The callback that will be run
     */
    public void setTimeChangeListener(TimeChangeListener listener) {
        mTimeChangeListener = listener;
    }

    /**
     * Interface definition for a callback to be invoked when a time is picked using the Time Picker
     */
    public interface TimeChangeListener {
        /**
         * Called when a time is set in the time picker
         * @param minutes The selected time in minutes past midnight
         */
        void onTimeSet(int minutes);
    }
}

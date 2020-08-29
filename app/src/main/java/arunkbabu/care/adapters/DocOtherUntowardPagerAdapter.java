package arunkbabu.care.adapters;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import arunkbabu.care.fragments.DocAddMedicineFragment;
import arunkbabu.care.fragments.DocInstructionFragment;
import arunkbabu.care.fragments.DocPatientsReportFragment;
import arunkbabu.care.fragments.UploadFileFragment;

public class DocOtherUntowardPagerAdapter extends FragmentStatePagerAdapter {
    private Context mContext;
    private Activity mActivity;
    public static final int NUM_PAGES = 4;

    public DocOtherUntowardPagerAdapter(@NonNull FragmentManager fm, Context context, Activity activity) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mContext = context;
        mActivity = activity;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new DocPatientsReportFragment(mContext);
            case 1:
                return new UploadFileFragment();
            case 2:
                return new DocAddMedicineFragment();
            case 3:
                return new DocInstructionFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}
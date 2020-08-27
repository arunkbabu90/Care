package arunkbabu.care.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import arunkbabu.care.fragments.PatientReportDescriptionFragment;
import arunkbabu.care.fragments.UploadFileFragment;

public class OtherUntowardPagerAdapter extends FragmentStatePagerAdapter {

    public static final int NUM_PAGES = 2;

    public OtherUntowardPagerAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new PatientReportDescriptionFragment();
            case 1:
                return new UploadFileFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}
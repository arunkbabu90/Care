package arunkbabu.care.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import arunkbabu.care.R;
import arunkbabu.care.activities.DoctorActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class DocPatientsReportFragment extends Fragment {
    @BindView(R.id.lv_patients_report) ListView mPatientReportListView;
    private Context mContext;

    public DocPatientsReportFragment(Context context) {
        // Required public constructor
        mContext = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_doc_patients_report, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DoctorActivity da  = (DoctorActivity) getActivity();
        if (da != null) {
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add("Name: " + DoctorActivity.getPatientName());
            arrayList.add("Age: " + DoctorActivity.getPatientAge());
            arrayList.add("Past Medications:\n" + DoctorActivity.getPastMedications());
            arrayList.add("Description:\n" + DoctorActivity.getReportDescription());

            ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.item_doc_patients_report, arrayList);
            mPatientReportListView.setAdapter(adapter);
        }
    }
}
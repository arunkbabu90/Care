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
import arunkbabu.care.Utils;
import arunkbabu.care.activities.ViewPatientReportActivity;
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

        ViewPatientReportActivity vp  = (ViewPatientReportActivity) getActivity();
        if (vp != null) {
            String sex = Utils.toSexString(vp.getPatientSex());
            if (sex.equals(""))
                sex = getString(R.string.not_provided);

            String weight = vp.getPatientWeight();
            String height = vp.getPatientHeight();

            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add("Name\n" + vp.getPatientName());
            arrayList.add("Age\n" + vp.getPatientAge());
            arrayList.add("Sex\n" + sex);
            arrayList.add("Height\n" + height + " cm");
            arrayList.add("Weight\n" + weight + " Kg");
            arrayList.add("BMI\n" + Utils.calculateBMI(weight, height));
            arrayList.add("Description\n" + vp.getReportDescription());

            ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.item_doc_patients_report, arrayList);
            mPatientReportListView.setAdapter(adapter);
        }
    }
}
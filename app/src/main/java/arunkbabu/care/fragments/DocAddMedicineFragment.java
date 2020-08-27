package arunkbabu.care.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import arunkbabu.care.R;
import arunkbabu.care.adapters.MedicineAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class DocAddMedicineFragment extends Fragment implements View.OnClickListener {

    @BindView(R.id.et_add_medicine_temp) EditText mMedicineNameEditText;
    @BindView(R.id.btn_add_medicine_temp) MaterialButton mAddMedicineButton;
    @BindView(R.id.rv_temp_add_medicine) RecyclerView mMedicineRecyclerView;

    private final String KEY_TEMP_DOC_MEDICINE_LIST = "key_temp_doc_medicine_list";

    public static ArrayList<String> mMedicineList;
    private MedicineAdapter mAdapter;
    private Activity a;
    private int cPosition;
    private String cUndoMedicineNameCache;

    public DocAddMedicineFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_doc_add_medicine, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        a = getActivity();
        mMedicineList = new ArrayList<>();

        if (a != null) {
            mAdapter = new MedicineAdapter(mMedicineList);
            mMedicineRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            mMedicineRecyclerView.setAdapter(mAdapter);
            mAddMedicineButton.setOnClickListener(this);

            // Swipe behaviour
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    cPosition = viewHolder.getAdapterPosition();
                    cUndoMedicineNameCache = mMedicineList.get(cPosition);

                    mMedicineList.remove(cPosition);
                    mAdapter.notifyDataSetChanged();

                    Snackbar.make(view.findViewById(R.id.add_medicine_layout),
                            cUndoMedicineNameCache + " Removed from list", Snackbar.LENGTH_LONG)
                            .setAction(getResources().getString(R.string.undo), v -> {
                                mMedicineList.add(cPosition, cUndoMedicineNameCache);
                                mMedicineRecyclerView.smoothScrollToPosition(cPosition);
                                cPosition = -1;
                                cUndoMedicineNameCache = null;
                                mAdapter.notifyDataSetChanged();
                            })
                            .setActionTextColor(getResources().getColor(android.R.color.holo_orange_dark))
                            .show();
                }
            }).attachToRecyclerView(mMedicineRecyclerView);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_add_medicine_temp) {
            addMedicine();
        }
    }

    private void addMedicine() {
        String medicine = mMedicineNameEditText.getText().toString().trim();
        if (!medicine.equals("")) {
            mMedicineList.add(medicine);
            mMedicineNameEditText.setText("");
            mAdapter.notifyDataSetChanged();
            mMedicineRecyclerView.smoothScrollToPosition(mMedicineList.size());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(KEY_TEMP_DOC_MEDICINE_LIST, mMedicineList);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            mMedicineList = savedInstanceState.getStringArrayList(KEY_TEMP_DOC_MEDICINE_LIST);

            if (a != null) {
                mAdapter = new MedicineAdapter(mMedicineList);
                mMedicineRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                mMedicineRecyclerView.setAdapter(mAdapter);
            }
        }
    }
}
package arunkbabu.care.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arunkbabu.care.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {
    private ArrayList<String> mMedicineList;
    private ItemClickListener mListener;

    public MedicineAdapter(ArrayList<String> medicineList) {
        mMedicineList = medicineList;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        holder.mMedicineTextView.setText(mMedicineList.get(position));
    }

    @Override
    public int getItemCount() {
        return mMedicineList.size();
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_item_medicine) TextView mMedicineTextView;

        MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> {

            });
        }
    }

    /**
     * Register a callback to be invoked when the view is clicked
     * @param listener The callback that will be run
     */
    public void setItemClickListener(ItemClickListener listener) {
        mListener = listener;
    }

    /**
     * Interface definition for a callback to be invoked when the view is clicked
     */
    public interface ItemClickListener {
        /**
         * Called when the item is clicked
         * @param position The position of the item clicked
         */
        void onItemClick(int position);
    }
}
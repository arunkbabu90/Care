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

public class SelectedFilesAdapter extends RecyclerView.Adapter<SelectedFilesAdapter.SelectedFilesViewHolder> {
    private ArrayList<String> mFileNameList;
    private ItemClickListener mItemClickListener;

    public SelectedFilesAdapter(ArrayList<String> fileNameList) {
        mFileNameList = fileNameList;
    }

    @NonNull
    @Override
    public SelectedFilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_file, parent, false);
        return new SelectedFilesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedFilesViewHolder holder, int position) {
        String fileName = mFileNameList.get(position);
        int extStartIndex = fileName.lastIndexOf(".") + 1;
        holder.mTopTextView.setText(fileName.substring(0, extStartIndex - 1)); // Extract & set the file name
        holder.mBottomTextView.setText(fileName.substring(extStartIndex));  // Extract & set the file extension
    }

    @Override
    public int getItemCount() {
        return mFileNameList.size();
    }

    class SelectedFilesViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_top_text) TextView mTopTextView;
        @BindView(R.id.tv_bottom_text) TextView mBottomTextView;

        SelectedFilesViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> {
                // The TwoLineListItem is clicked
                mItemClickListener.onItemClick(getAdapterPosition());
            });
        }
    }

    public void setOnItemClickListener(ItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(int position);
    }
}
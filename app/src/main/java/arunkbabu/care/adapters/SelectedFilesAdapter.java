package arunkbabu.care.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import arunkbabu.care.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectedFilesAdapter extends RecyclerView.Adapter<SelectedFilesAdapter.SelectedFilesViewHolder> {
    private ItemClickListener mItemClickListener;
    private ArrayList<Uri> mPathList;
    private Context mContext;

    public SelectedFilesAdapter(Context context, ArrayList<Uri> pathList) {
        mPathList = pathList;
        mContext = context;
    }

    @NonNull
    @Override
    public SelectedFilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_file, parent, false);
        return new SelectedFilesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedFilesViewHolder holder, int position) {
        Uri path = mPathList.get(position);
        Glide.with(mContext).load(path).error(R.drawable.ic_broken_image).into(holder.mPhotoView);
    }

    @Override
    public int getItemCount() {
        return mPathList.size();
    }

    class SelectedFilesViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.selectedFile_thumbnail) ImageView mPhotoView;

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
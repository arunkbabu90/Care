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

public class SimpleListItemAdapter extends RecyclerView.Adapter<SimpleListItemAdapter.ItemViewHolder> {
    private ArrayList<String> mItemList;
    private ItemClickListener mListener;

    public SimpleListItemAdapter(ArrayList<String> list) {
        mItemList = list;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_list_with_underline, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.mItemNameTextView.setText(mItemList.get(position));
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    /**
     * Supply the updated ArrayList of items and refresh the list with those updated items
     * @param list The list of items
     */
    public void setItemList(ArrayList<String> list) {
        mItemList = list;
        notifyDataSetChanged();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_item_name) TextView mItemNameTextView;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener((v -> mListener.onItemClick(getAdapterPosition())));
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

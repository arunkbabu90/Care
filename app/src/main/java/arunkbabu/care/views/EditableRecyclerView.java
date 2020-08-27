package arunkbabu.care.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

import arunkbabu.care.R;
import arunkbabu.care.adapters.SimpleListItemAdapter;

public class EditableRecyclerView extends LinearLayout {

    private boolean mIsInflationSuccess;
    private int mBackgroundColor;
    private String mTitleText;
    private Context mContext;

    private MaterialTextView mTitleTextView;
    private RecyclerView mRecyclerView;
    private TextView mAddItemTextView;
    private SimpleListItemAdapter mAdapter;
    private ArrayList<String> mItemList;
    private int cPosition;
    private String cUndoItemCache;

    private AddButtonClickListener mAddListener;
    private ItemClickListener mItemListener;
    private ItemSwipeListener mSwipeListener;

    public EditableRecyclerView(Context context) {
        super(context);
        mContext = context;
        initializeViews(context);
    }

    public EditableRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EditableRecyclerView, 0, 0);

        try {
            mTitleText = ta.getString(R.styleable.EditableRecyclerView_titleText);
            mBackgroundColor = ta.getColor(R.styleable.EditableRecyclerView_backgroundColor, getResources().getColor(android.R.color.transparent));
        } finally {
            ta.recycle();
        }
        initializeViews(context);
    }

    public EditableRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EditableRecyclerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (mIsInflationSuccess) {
            mTitleTextView = findViewById(R.id.erv_title_text_view);
            mRecyclerView = findViewById(R.id.erv_recycler_view);
            mAddItemTextView = findViewById(R.id.erv_add_item_text_view);

            mItemList = new ArrayList<>();

            mAdapter = new SimpleListItemAdapter(mItemList);
            mAdapter.setItemClickListener(position -> {
                // Item Clicked
                if (mItemListener != null)
                    mItemListener.onItemClick(position, mItemList.get(position));
            });
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext,LinearLayoutManager.VERTICAL, false));

            mTitleTextView.setText(mTitleText);
            mAddItemTextView.setOnClickListener((v -> {
                // Add Clicked
                if (mAddListener != null)
                    mAddListener.onAddClick();
            }));
        }
    }

    /**
     * Enables the swipe to delete functionality. When active you can swipe either left or right
     * to delete the item swiped
     * @param view The parent layout where this view belongs
     */
    public void addSwipeToDeleteListener(View view) {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                cPosition = viewHolder.getAdapterPosition();
                cUndoItemCache = mItemList.get(cPosition);

                mItemList.remove(cPosition);
                mAdapter.notifyDataSetChanged();
                if (mSwipeListener != null) {
                    mSwipeListener.onItemSwiped(cUndoItemCache);
                }

                Snackbar.make(view,cUndoItemCache + " Removed", Snackbar.LENGTH_LONG)
                        .setAction(getResources().getString(R.string.undo), v -> {
                            if (mSwipeListener != null) {
                                mSwipeListener.onItemSwiped(cUndoItemCache);
                            }
                            mItemList.add(cPosition, cUndoItemCache);
                            mRecyclerView.smoothScrollToPosition(cPosition);
                            cPosition = -1;
                            cUndoItemCache = null;
                            mAdapter.notifyDataSetChanged();
                        })
                        .setActionTextColor(getResources().getColor(android.R.color.holo_green_light))
                        .show();
            }
        }).attachToRecyclerView(mRecyclerView);
    }

    /**
     * Inflates the layout and set its properties
     * @param context The Context where this view belongs
     */
    private void initializeViews(Context context) {
        setOrientation(LinearLayout.VERTICAL);
        setBackgroundColor(mBackgroundColor);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            inflater.inflate(R.layout.view_editable_recycler_view, this, true);
            mIsInflationSuccess = true;
        }  else {
            Toast.makeText(context, getResources().getString(R.string.err_unexpected), Toast.LENGTH_LONG).show();
            mIsInflationSuccess = false;
        }
    }

    /**
     * Set the title text
     * @param title String: The title
     */
    public void setTitleText(String title) {
        mTitleTextView.setText(title);
    }

    /**
     * Set the background color of the view from a color resource
     * @param colorResourceId int: The resource id of the background color
     */
    public void setBackgroundColorFromResource(int colorResourceId) {
        setBackgroundColor(getResources().getColor(colorResourceId));
    }

    /**
     * Set the background color of the view from a color's hex value
     * @param colorHex String: The Hex value of the background color
     */
    public void setBackgroundColorFromHex(String colorHex) {
        setBackgroundColor(Color.parseColor(colorHex));
    }

    /**
     * Adds the new item to the list and updates the recycler view
     * @param item The item to be added
     */
    public void addNewItemToList(String item) {
        mItemList.add(item);
        mAdapter.setItemList(mItemList);
        mRecyclerView.smoothScrollToPosition(mItemList.size());
    }

    /**
     * Adds ALL the items to list and updates the recycler view
     * @param items The list of items to be added
     */
    public void addAllItemsToList(ArrayList<String> items) {
        mItemList.addAll(items);
        mAdapter.setItemList(mItemList);
    }

    /**
     * Edit the item entry at the specified position
     * @param position The position of the item to edit
     * @param newEntry The edited entry
     */
    public void editItemAtPosition(int position, String newEntry) {
        mItemList.remove(position);
        mItemList.add(position, newEntry);
        mAdapter.setItemList(mItemList);
        mRecyclerView.smoothScrollToPosition(position);
    }

    /**
     * Returns all the items shown in the List
     * @return ArrayList<String>: All items as a String array list
     */
    public ArrayList<String> getAllItemsInList() {
        return mItemList;
    }

    /**
     * Register a callback to be invoked when the add button in this view is clicked
     * @param listener The callback that will be run
     */
    public void setAddButtonClickListener(AddButtonClickListener listener) {
        mAddListener = listener;
    }

    /**
     * Interface definition for a callback to be invoked when the add button in this view is clicked
     */
    public interface AddButtonClickListener {
        /**
         * Called when the add button in this view is clicked
         */
        void onAddClick();
    }

    /**
     * Register a callback to be invoked when an item in the recycler view is clicked
     * @param listener The callback that will be run
     */
    public void setItemClickListener(ItemClickListener listener) {
        mItemListener = listener;
    }

    /**
     * Register a callback to be invoked when an item is swiped off
     * @param listener The callback that will be run
     */
    public void setSwipeListener(ItemSwipeListener listener) {
        mSwipeListener = listener;
    }

    /**
     * Interface definition for a callback to be invoked when an item in the recycler view is clicked
     */
    public interface ItemClickListener {
        /**
         * Called when an item in the recycler view is clicked
         * @param item The item clicked
         * @param position The position of the item clicked
         */
        void onItemClick(int position, String item);
    }

    /**
     * Interface definition for a callback to be invoked when an item is swiped off
     */
    public interface ItemSwipeListener {
        /**
         * Called when an item is swiped off
         * @param item The item that is swiped off
         */
        void onItemSwiped(String item);
    }
}
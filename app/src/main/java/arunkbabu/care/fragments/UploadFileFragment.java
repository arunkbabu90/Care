package arunkbabu.care.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import arunkbabu.care.Constants;
import arunkbabu.care.R;
import arunkbabu.care.Utils;
import arunkbabu.care.activities.ViewPatientReportActivity;
import arunkbabu.care.adapters.SelectedFilesAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class UploadFileFragment extends Fragment implements View.OnClickListener {
    @BindView(R.id.btn_take_photo) MaterialButton mTakePhotoButton;
    @BindView(R.id.btn_choose_file) MaterialButton mChooseFileButton;
    @BindView(R.id.rv_uploaded_files) RecyclerView mSelectedFilesRecyclerView;
    @BindView(R.id.iv_selected_photo) ImageView mSelectedPhotoImageView;
    @BindView(R.id.pb_upload_file) ProgressBar mProgressCircle;
    @BindView(R.id.tv_err_upload) TextView mErrorTextView;
    @BindView(R.id.tv_upload_title) TextView mTitleTextView;

    private final int REQUEST_IMAGE_CAPTURE = 3000;
    private final int REQUEST_OPEN_FILE = 3001;
    private final String KEY_UPLOAD_FILE_PATHS = "key_upload_file_path_list";
    private final String KEY_UPLOAD_FILE_NAMES = "key_upload_file_name_list";
    private final String KEY_LAST_SELECTED_IMAGE_POSITION = "key_last_selected_image_position";

    private Activity a;
    private Uri mFileUri;
    private int mSelectedPosition;
    private SelectedFilesAdapter mAdapter;
    public static ArrayList<Uri> sPathList;
    public static ArrayList<String> sFileNameList;
    private String cUndoFileNameCache;
    private Uri cUndoPathCache;
    private int cPosition;
    private CustomTarget<Drawable> mTarget;

    public UploadFileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_upload_file, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        a = getActivity();

        // Make these null in-case if there is cached data and to prevent duplicate data
        sFileNameList = null;
        sPathList = null;

        // Initialize the array for holding the selected files name
        sFileNameList = new ArrayList<>();
        // Initialize the array for holding the selected files path
        sPathList = new ArrayList<>();

        if ((a != null) && !(a.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))) {
            // The device doesn't have a camera so disable "Take Photo" button
            mTakePhotoButton.setEnabled(false);
            mTakePhotoButton.setVisibility(View.GONE);
        } else {
            mTakePhotoButton.setEnabled(true);
            mTakePhotoButton.setVisibility(View.VISIBLE);
            mTakePhotoButton.setOnClickListener(this);
        }

        // If the user is a DOCTOR disable the upload buttons to prevent doctor from uploading files
        // Also set the title to uploaded files
        if (Utils.userType == Constants.USER_TYPE_DOCTOR) {
            ViewPatientReportActivity vp = (ViewPatientReportActivity) getActivity();

            if (vp != null) {
                mTakePhotoButton.setEnabled(false);
                mChooseFileButton.setEnabled(false);
                mChooseFileButton.setVisibility(View.GONE);
                mTakePhotoButton.setVisibility(View.GONE);
                mTitleTextView.setText(getString(R.string.uploaded_files));

                // Get all the uploaded image paths and convert it to uri. Also get its file name
                ArrayList<String> paths = vp.getImagePaths();
                for (int i = 0; i < paths.size(); i++) {
                    sPathList.add(Uri.parse(paths.get(i)));
                    sFileNameList.add(getFileName(paths.get(i)));
                }

                if (sPathList != null && !sPathList.isEmpty()) {
                    // Load the images only if there is images uploaded. Otherwise don't even think
                    // about it
                    loadImageToView(0);
                } else {
                    mErrorTextView.setVisibility(View.VISIBLE);
                }
            }
        } else {
            mTitleTextView.setText(getString(R.string.attach_photo));
            mChooseFileButton.setOnClickListener(this);
        }

        // Load the selected files to the recycler view
        if (a != null) {
            mSelectedFilesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                    LinearLayoutManager.HORIZONTAL, false));
            mAdapter = new SelectedFilesAdapter(getContext(), sPathList);
            mAdapter.setOnItemClickListener(position -> {
                // List item is clicked so load the clicked image file into the image view
                mSelectedPosition = position;
                loadImageToView(position);
            });
            mSelectedFilesRecyclerView.setAdapter(mAdapter);

            if (Utils.userType == Constants.USER_TYPE_PATIENT) {
                // Disable the swipe to delete functionality if the user is a doctor
                new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.UP) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        cPosition = viewHolder.getAdapterPosition();
                        cUndoFileNameCache = sFileNameList.get(cPosition);
                        cUndoPathCache = sPathList.get(cPosition);

                        sFileNameList.remove(cPosition);
                        sPathList.remove(cPosition);
                        mAdapter.notifyDataSetChanged();

                        if (sPathList != null && !sPathList.isEmpty()) {
                            // If there are paths in the arrayList then add it
                            loadImageToView(0);
                        } else {
                            // If the list is empty hide the image view to don't show any images
                            mSelectedPhotoImageView.setVisibility(View.INVISIBLE);
                        }

                        Snackbar.make(view.findViewById(R.id.upload_files_layout),
                                cUndoFileNameCache + " Removed from list", Snackbar.LENGTH_LONG)
                                .setAction(getResources().getString(R.string.undo), v -> {
                                    sFileNameList.add(cPosition, cUndoFileNameCache);
                                    sPathList.add(cPosition, cUndoPathCache);
                                    cPosition = -1;
                                    cUndoFileNameCache = null;
                                    mAdapter.notifyDataSetChanged();

                                    if (sPathList != null && !sPathList.isEmpty()) {
                                        loadImageToView(0);
                                    } else {
                                        mSelectedPhotoImageView.setVisibility(View.INVISIBLE);
                                    }
                                })
                                .setActionTextColor(getResources().getColor(android.R.color.holo_green_light))
                                .show();
                    }
                }).attachToRecyclerView(mSelectedFilesRecyclerView);
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.err_files_list_load), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_take_photo:
                takePhoto();
                break;
            case R.id.btn_choose_file:
                chooseFile();
                break;
        }
    }

    /**
     * Loads an image at the specified position from the sPathList to the ImageView
     * @param position The position of the image to be loaded from sPathList
     */
    private void loadImageToView(int position) {
        if (mSelectedPhotoImageView.getVisibility() == View.INVISIBLE || mSelectedPhotoImageView.getVisibility() == View.GONE) {
            mSelectedPhotoImageView.setVisibility(View.VISIBLE);
        }

        mTarget = new CustomTarget<Drawable>() {
            @Override
            public void onLoadStarted(@Nullable Drawable placeholder) {
                mProgressCircle.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                mSelectedPhotoImageView.setImageDrawable(resource);
                mProgressCircle.setVisibility(View.GONE);

                if (Utils.userType == Constants.USER_TYPE_PATIENT) {
                    // Show the swipe up to delete message
                    mTitleTextView.setText(R.string.swipe_to_delete);
                    mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                }
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                mProgressCircle.setVisibility(View.GONE);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                mSelectedPhotoImageView.setImageDrawable(null);
            }
        };

        Glide.with(this).load(sPathList.get(position)).into(mTarget);
    }

    /**
     * Loads an image from the specified URI to the ImageView
     * @param imageUri The URI of the image to be loaded
     */
    private void loadImageToView(Uri imageUri) {
        if (mSelectedPhotoImageView.getVisibility() == View.INVISIBLE || mSelectedPhotoImageView.getVisibility() == View.GONE) {
            mSelectedPhotoImageView.setVisibility(View.VISIBLE);
        }

        mTarget = new CustomTarget<Drawable>() {
            @Override
            public void onLoadStarted(@Nullable Drawable placeholder) {
                mProgressCircle.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                mSelectedPhotoImageView.setImageDrawable(resource);
                mProgressCircle.setVisibility(View.GONE);

                if (Utils.userType == Constants.USER_TYPE_PATIENT) {
                    // Show the swipe up to delete message
                    mTitleTextView.setText(R.string.swipe_to_delete);
                    mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                }
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                mProgressCircle.setVisibility(View.GONE);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                mSelectedPhotoImageView.setImageDrawable(null);
            }
        };

        Glide.with(this).load(imageUri).into(mTarget);
    }

    /**
     * Invoked when Take-Photo button is clicked
     */
    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (a != null && takePictureIntent.resolveActivity(a.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = Utils.createImageFile(a.getApplicationContext(), Constants.IMG_FORMAT_JPG);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(a, getString(R.string.err_file_creation), Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mFileUri = FileProvider.getUriForFile(a.getApplicationContext(), a.getString(R.string.file_provider_authority), photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * Invoked when Choose-File button is clicked
     */
    private void chooseFile() {
        Intent chooseFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        chooseFile.setType("image/*");
        startActivityForResult(chooseFile, REQUEST_OPEN_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // In-case if the ArrayList is null reinitialize it
            if (sFileNameList == null) sFileNameList = new ArrayList<>();
            if (sPathList == null) sPathList = new ArrayList<>();

            sFileNameList.add(getFileName(mFileUri));
            sPathList.add(mFileUri);
            mSelectedPosition = sPathList.size() - 1;
            mAdapter.notifyDataSetChanged();

            loadImageToView(mFileUri);
        }

        if (requestCode == REQUEST_OPEN_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                // In-case if the ArrayList is null reinitialize it
                if (sFileNameList == null) sFileNameList = new ArrayList<>();
                if (sPathList == null) sPathList = new ArrayList<>();

                mFileUri = data.getData();
                if (mFileUri != null) {
                    sFileNameList.add(getFileName(mFileUri));
                    sPathList.add(mFileUri);
                    mSelectedPosition = sPathList.size() - 1;
                    mAdapter.notifyDataSetChanged();

                    loadImageToView(mFileUri);
                }
            }
        }
    }

    /**
     * Extracts the file name from an absolute path from network
     * @param path The absolute string path of the file
     * @return String  The file name extracted from the path
     */
    private String getFileName(String path) {
        int beginIndex = path.lastIndexOf("SentImages%2F") + 13;
        int endIndex = path.lastIndexOf("?alt");
        return path.substring(beginIndex, endIndex);
    }

    /**
     * Extracts the file name from content:// URI
     * @param uri The content:// URI
     * @return String  The file name extracted from the URI
     */
    private String getFileName(Uri uri) {
        String fileName = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = a.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (fileName == null) {
            fileName = uri.getPath();
        }
        return fileName;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArrayList(KEY_UPLOAD_FILE_NAMES, sFileNameList);
        outState.putParcelableArrayList(KEY_UPLOAD_FILE_PATHS, sPathList);
        outState.putInt(KEY_LAST_SELECTED_IMAGE_POSITION, mSelectedPosition);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            sFileNameList = savedInstanceState.getStringArrayList(KEY_UPLOAD_FILE_NAMES);
            sPathList = savedInstanceState.getParcelableArrayList(KEY_UPLOAD_FILE_PATHS);
            mSelectedPosition = savedInstanceState.getInt(KEY_LAST_SELECTED_IMAGE_POSITION);

            mAdapter = new SelectedFilesAdapter(getContext(), sPathList);
            mSelectedFilesRecyclerView.setAdapter(mAdapter);
            if (sPathList != null && sPathList.size() > 0) {
                Glide.with(this).load(sPathList.get(mSelectedPosition)).into(mSelectedPhotoImageView);
            }
        }
    }
}
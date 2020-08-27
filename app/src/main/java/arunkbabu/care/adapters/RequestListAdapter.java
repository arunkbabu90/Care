package arunkbabu.care.adapters;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import arunkbabu.care.Patient;
import arunkbabu.care.R;
import arunkbabu.care.Utils;
import arunkbabu.care.views.CircularImageView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RequestListAdapter extends FirestoreRecyclerAdapter<Patient, RequestListAdapter.RequestViewHolder> {

    private ItemClickListener mItemClickListener;
    private TextView mNoRequestTextView;
    private RecyclerView mRequestRecyclerView;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     * @param options The FirestoreRecyclerOptions
     * @param noRequestTextView The TextView containing the text "No Requests Available"
     */
    public RequestListAdapter(@NonNull FirestoreRecyclerOptions<Patient> options, TextView noRequestTextView, RecyclerView requestRecyclerView) {
        super(options);
        mNoRequestTextView = noRequestTextView;
        mRequestRecyclerView = requestRecyclerView;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Patient model) {
        holder.bind(model);
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();

        if (getItemCount() <= 0) {
            // List is empty so show no request text view
            mRequestRecyclerView.setVisibility(View.GONE);
            mNoRequestTextView.setVisibility(View.VISIBLE);
        } else {
            // List has items so hide no request text view
            mRequestRecyclerView.setVisibility(View.VISIBLE);
            mNoRequestTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onError(@NonNull FirebaseFirestoreException e) {
        super.onError(e);
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_patient_dp) CircularImageView mPatientDpView;
        @BindView(R.id.btn_accept) Button mAcceptButton;
        @BindView(R.id.tv_patient_report_type) TextView mPatientReportTypeTextView;
        @BindView(R.id.tv_patient_name) TextView mPatientNameTextView;

        private Target mTarget;

        RequestViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bind(final Patient patient) {
            mPatientReportTypeTextView.setText(String.format("Report on %s", Utils.toReportTypeString(patient.getReportType())));
            mPatientNameTextView.setText(patient.getPatientName());
            // Load the patient dp
            loadImageToView(Uri.parse(patient.getProfilePicture()));

            mAcceptButton.setOnClickListener(v -> {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(v, patient, getAdapterPosition());
                }
            });
        }

        /**
         * Loads an image from the specified URI to the ImageView
         * @param imageUri The URI of the image to be loaded
         */
        private void loadImageToView(Uri imageUri) {
            mTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    mPatientDpView.setImageBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) { }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) { }
            };
            Picasso.get().load(imageUri).resize(240, 0).into(mTarget);
        }
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.mItemClickListener = clickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View v, Patient patient, int position);
    }
}

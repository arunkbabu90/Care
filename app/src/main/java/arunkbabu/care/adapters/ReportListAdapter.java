package arunkbabu.care.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.Date;

import arunkbabu.care.DoctorReport;
import arunkbabu.care.R;
import arunkbabu.care.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportListAdapter extends FirestoreRecyclerAdapter<DoctorReport, ReportListAdapter.ReportViewHolder> {
    private ItemClickListener mItemClickListener;
    private TextView mNoReportTextView;
    private RecyclerView mReportRecyclerView;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query. Populates the DoctorsReportFragment
     * See {@link FirestoreRecyclerOptions} for configuration options.
     * @param options
     */
    public ReportListAdapter(@NonNull FirestoreRecyclerOptions<DoctorReport> options, RecyclerView reportRecyclerView, TextView noReportTextView) {
        super(options);
        mNoReportTextView = noReportTextView;
        mReportRecyclerView = reportRecyclerView;
        onDataChanged();
    }

    @Override
    protected void onBindViewHolder(@NonNull ReportViewHolder holder, int position, @NonNull DoctorReport model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctors_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();

        if (getItemCount() <= 0) {
            // List is empty so show no request text view
            mReportRecyclerView.setVisibility(View.GONE);
            mNoReportTextView.setVisibility(View.VISIBLE);
        } else {
            // List has items so hide no request text view
            mReportRecyclerView.setVisibility(View.VISIBLE);
            mNoReportTextView.setVisibility(View.GONE);
        }
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_doctor_report_type) TextView mReportTypeTextView;
        @BindView(R.id.tv_doctor_report_doc_name) TextView mDoctorNameTextView;
        @BindView(R.id.doctor_report_item_layout) LinearLayout mDocReportLayout;

        ReportViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bind(DoctorReport report) {
            Date date = report.getReportTimestamp().toDate();
            String properDate = Utils.convertDateToString(date, "dd-MMM-yyyy hh:mm a");

            mDoctorNameTextView.setText(report.getFull_name());
            mReportTypeTextView.setText(String.format("Report you sent on %s", properDate));

            mDocReportLayout.setOnClickListener(v -> mItemClickListener.onItemClick(v, report, getAdapterPosition()));
        }
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.mItemClickListener = clickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View v, DoctorReport report, int position);
    }
}
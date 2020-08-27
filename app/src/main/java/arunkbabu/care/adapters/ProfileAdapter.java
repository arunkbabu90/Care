package arunkbabu.care.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arunkbabu.care.Detail;
import arunkbabu.care.views.TitleDetailCardView;
import arunkbabu.care.views.TitleRadioCardView;

public class ProfileAdapter extends RecyclerView.Adapter {

    private CardClickListener mListener;

    public static ArrayList<Detail> sDetails;

    /**
     * An adapter used to populate the items of the PatientProfileActivity
     * @param details The array list of Details. Each Detail object represents an item in the profile
     */
    public ProfileAdapter(ArrayList<Detail> details) {
        sDetails = details;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case Detail.TYPE_TITLE_DETAIL_EDITABLE:
                view = new TitleDetailCardView(parent.getContext());
                return new TitleDetailEditableViewHolder(view);
            case Detail.TYPE_TITLE_DETAIL_NON_EDITABLE:
                view = new TitleDetailCardView(parent.getContext());
                return new TitleDetailNonEditableViewHolder(view);
            case Detail.TYPE_TITLE_RADIO:
                view = new TitleRadioCardView(parent.getContext());
                return new TitleRadioViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        switch (sDetails.get(position).getType()) {
            case 20:
                return Detail.TYPE_TITLE_DETAIL_EDITABLE;
            case 21:
                return Detail.TYPE_TITLE_DETAIL_NON_EDITABLE;
            case 22:
                return Detail.TYPE_TITLE_RADIO;
        }
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Detail detail = sDetails.get(position);
        switch (detail.getType()) {
            case Detail.TYPE_TITLE_DETAIL_EDITABLE: {
                TitleDetailEditableViewHolder tdevh = (TitleDetailEditableViewHolder) holder;
                tdevh.mTitleDetailCardView.setTopText(detail.getTopText());
                tdevh.mTitleDetailCardView.setBottomText(detail.getBottomText());
            }
            break;
            case Detail.TYPE_TITLE_DETAIL_NON_EDITABLE: {
                TitleDetailNonEditableViewHolder tdnevh = (TitleDetailNonEditableViewHolder) holder;
                tdnevh.mNonEditableCardView.setTopText(detail.getTopText());
                tdnevh.mNonEditableCardView.setBottomText(detail.getBottomText());
            }
            break;
            case Detail.TYPE_TITLE_RADIO: {
                TitleRadioViewHolder trvh = (TitleRadioViewHolder) holder;
                trvh.mTitleRadioCardView.setTitleText(detail.getTopText());
                trvh.mTitleRadioCardView.setRadioButton1Text(detail.getRadio1Text());
                trvh.mTitleRadioCardView.setRadioButton2Text(detail.getRadio2Text());

                if (detail.isRadio1Checked()) {
                    trvh.mTitleRadioCardView.setRadio1Checked(true);
                } else if (detail.isRadio2Checked()) {
                    trvh.mTitleRadioCardView.setRadio2Checked(true);
                }
            }
            break;
        }
    }

    @Override
    public int getItemCount() {
        return sDetails.size();
    }

    /**
     * The view holder for Editable TitleDetailCardView
     */
    static class TitleDetailEditableViewHolder extends RecyclerView.ViewHolder {
        private TitleDetailCardView mTitleDetailCardView;

        public TitleDetailEditableViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitleDetailCardView = (TitleDetailCardView) itemView;
        }
    }

    /**
     * The view holder for Non-Editable TitleDetailCardView
     */
    static class TitleDetailNonEditableViewHolder extends RecyclerView.ViewHolder {
        private TitleDetailCardView mNonEditableCardView;

        public TitleDetailNonEditableViewHolder(@NonNull View itemView) {
            super(itemView);
            mNonEditableCardView = (TitleDetailCardView) itemView;
            mNonEditableCardView.disableEdits();
        }
    }

    /**
     * The view holder for TitleRadioCardView
     */
    static class TitleRadioViewHolder extends RecyclerView.ViewHolder {
        private TitleRadioCardView mTitleRadioCardView;

        public TitleRadioViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitleRadioCardView = (TitleRadioCardView) itemView;
        }
    }

    /**
     * Register a callback to be invoked when the card is clicked
     * @param listener The callback that will be run
     */
    public void setCardClickListener(CardClickListener listener) {
        mListener = listener;
    }

    /**
     * Interface definition for a callback to be invoked when the card view is clicked
     */
    public interface CardClickListener {

        /**
         * Called when a card is clicked
         * @param position The position of the view clicked
         */
        void onCardClick(int position);
    }
}
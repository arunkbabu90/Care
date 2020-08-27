package arunkbabu.care;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model class representing a single item
 */
public class Detail implements Parcelable {
    /**
     * The item will have a title, footer, an edit pen icon and will receive touch events
     * Use with {@link #setType}
     */
    public static final int TYPE_TITLE_DETAIL_EDITABLE = 20;

    /**
     * The item will have a title, footer, an edit pen icon and will NOT receive touch events
     * Use with {@link #setType}
     */
    public static final int TYPE_TITLE_DETAIL_NON_EDITABLE = 21;

    /**
     * The item will have a title and two Radio Buttons
     * Use with {@link #setType}
     */
    public static final int TYPE_TITLE_RADIO = 22;

    private String mTopText;
    private String mBottomText;
    private String mRadio1Text;
    private String mRadio2Text;
    private boolean mIsRadio1Checked;
    private boolean mIsRadio2Checked;
    private boolean mIsEditable;
    private int mType;

    /**
     * Detail object representing a single item. This represents an item with a Heading and its
     * corresponding Description
     * @param topText The heading text
     * @param bottomText The description text
     * @param isEditable Whether to show the Edit Pen icon. Also disables touches
     */
    public Detail(String topText, String bottomText, boolean isEditable) {
        mTopText = topText;
        mBottomText = bottomText;
        mIsEditable = isEditable;
        if (isEditable) {
            mType = TYPE_TITLE_DETAIL_EDITABLE;
        } else {
            mType = TYPE_TITLE_DETAIL_NON_EDITABLE;
        }
    }

    /**
     * Detail object representing a single item. This represents an item with a Heading and
     * two Radio buttons
     * @param topText The heading text
     * @param radio1Text The text of the Radio Button 1
     * @param radio2Text The text of the Radio Button 2
     */
    public Detail(String topText, String radio1Text, String radio2Text) {
        mTopText = topText;
        mRadio1Text = radio1Text;
        mRadio2Text = radio2Text;
        mType = TYPE_TITLE_RADIO;
    }

    /**
     * Returns the title text of the item
     * @return String: The title
     */
    public String getTopText() {
        return (mTopText != null) ? mTopText : "";
    }

    /**
     * Sets the title text of the item
     * @param topText The text to be applied
     */
    public void setTopText(String topText) {
        this.mTopText = topText;
    }

    /**
     * Returns the footer text of the item
     * @return String: The footer
     */
    public String getBottomText() {
        return (mBottomText != null) ? mBottomText : "";
    }

    /**
     * Sets the footer text of the item
     * @param bottomText The text to be applied
     */
    public void setBottomText(String bottomText) {
        this.mBottomText = bottomText;
    }

    /**
     * Returns the text of Radio Button 1
     * @return String: Text
     */
    public String getRadio1Text() {
        return (mRadio1Text != null) ? mRadio1Text : "";
    }

    /**
     * Sets the text of Radio Button 1
     * @param radio1Text The text to be applied
     */
    public void setRadio1Text(String radio1Text) {
        this.mRadio1Text = radio1Text;
    }

    /**
     * Returns the text of Radio Button 2
     * @return String: Text
     */
    public String getRadio2Text() {
        return (mRadio2Text != null) ? mRadio2Text : "";
    }

    /**
     * Sets the text of Radio Button 2
     * @param radio2Text The text to be applied
     */
    public void setRadio2Text(String radio2Text) {
        this.mRadio2Text = radio2Text;
    }

    /**
     * Returns whether Radio Button 1 is Checked
     * @return True if checked; False if unchecked
     */
    public boolean isRadio1Checked() {
        return mIsRadio1Checked;
    }

    /**
     * Changes the checked state of Radio Button 1
     * @param isRadio1Checked true: to check the Radio Button; false: to uncheck it
     */
    public void setIsRadio1Checked(boolean isRadio1Checked) {
        this.mIsRadio1Checked = isRadio1Checked;
    }

    /**
     * Returns whether Radio Button 2 is Checked
     * @return True if checked; False if unchecked
     */
    public boolean isRadio2Checked() {
        return mIsRadio2Checked;
    }

    /**
     * Changes the checked state of Radio Button 2
     * @param isRadio2Checked true: to check the Radio Button; false to uncheck it
     */
    public void setIsRadio2Checked(boolean isRadio2Checked) {
        this.mIsRadio2Checked = isRadio2Checked;
    }

    /**
     * Returns whether the view is editable
     * @return True if editable; false otherwise
     */
    public boolean isEditable() {
        return mIsEditable;
    }

    /**
     * Changes the editable state of the view
     * @param isEditable true: to make it editable; false: to make it uneditable
     */
    public void setIsEditable(boolean isEditable) {
        this.mIsEditable = isEditable;
    }

    /**
     * Returns the type of the view currently loaded.
     * @return One of {@link #TYPE_TITLE_DETAIL_EDITABLE}, {@link #TYPE_TITLE_DETAIL_NON_EDITABLE}, {@link #TYPE_TITLE_RADIO}
     */
    public int getType() {
        return mType;
    }

    /**
     * Sets the type of the view to be loaded
     * @param type One of {@link #TYPE_TITLE_DETAIL_EDITABLE}, {@link #TYPE_TITLE_DETAIL_NON_EDITABLE}, {@link #TYPE_TITLE_RADIO}
     */
    public void setType(int type) {
        this.mType = type;
    }

    /**
     * Returns whether the footer or bottom text view has data or text in it
     * @return True if it has data
     */
    public boolean hasFooterData() {
        // Desc field is not empty or choices is not empty
        return (mBottomText != null && !mBottomText.equals(""));
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTopText);
        dest.writeString(mBottomText);
        dest.writeString(mRadio1Text);
        dest.writeString(mRadio2Text);
        dest.writeInt(mType);
        dest.writeByte((byte) (mIsEditable ? 1 : 0));
        dest.writeByte((byte) (mIsRadio1Checked ? 1 : 0));
        dest.writeByte((byte) (mIsRadio2Checked ? 1 : 0));
    }

    private Detail(Parcel in) {
        mTopText = in.readString();
        mBottomText = in.readString();
        mRadio1Text = in.readString();
        mRadio2Text = in.readString();
        mType = in.readInt();

        // Convert byte to boolean
        mIsEditable = in.readByte() != 0;
        mIsRadio1Checked = in.readByte() != 0;
        mIsRadio2Checked = in.readByte() != 0;
    }

    public static final Creator<Detail> CREATOR = new Creator<Detail>() {
        @Override
        public Detail createFromParcel(Parcel in) {
            return new Detail(in);
        }

        @Override
        public Detail[] newArray(int size) {
            return new Detail[size];
        }
    };
}

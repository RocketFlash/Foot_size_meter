package com.tryfit.camera;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Rauf Yagfarov on 07/09/2017.
 */

public class TryFitLibResult implements Parcelable {

    public static final int TRY_FIT_LIB_TYPE_TOP_PICTURE = 0;
    public static final int TRY_FIT_LIB_TYPE_LEFT_PICTURE = 1;
    public static final int TRY_FIT_LIB_TYPE_RIGHT_PICTURE = 2;
    public static final int TRY_FIT_LIB_RESULT_RESULT_POSITIVE = 0;
    public static final int TRY_FIT_LIB_RESULT_PAPER_CONTOUR_NOT_FOUND = 1;
    public static final int TRY_FIT_LIB_RESULT_FOOT_CONTOUR_NOT_FOUND = 2;
    public static final Creator<TryFitLibResult> CREATOR = new Creator<TryFitLibResult>() {
        @Override
        public TryFitLibResult createFromParcel(Parcel in) {
            return new TryFitLibResult(in);
        }

        @Override
        public TryFitLibResult[] newArray(int size) {
            return new TryFitLibResult[size];
        }
    };
    /*
    ** Type of image being processed
    **/
    private int type;
    /*
    ** Result returned by native tryfitlib
    **/
    private int result;
    /*
    ** Left/right feet indicator
    **/
    private String feet;
    /*
    ** Processed image
    **/
    private Bitmap processed;
    /*
     ** Original image
     **/
    private Bitmap original;
    /*
    ** Ball width of the feet in mm
    **/
    private int ballWidth;
    /*
    ** Stick length of the feet in mm
    **/
    private int stickLength;

    public TryFitLibResult(int type, int result) {
        this.type = type;
        this.result = result;
    }

    protected TryFitLibResult(Parcel in) {
        type = in.readInt();
        result = in.readInt();
        processed = in.readParcelable(Bitmap.class.getClassLoader());
        ballWidth = in.readInt();
        stickLength = in.readInt();
        feet = in.readString();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public Bitmap getProcessedBitmap() {
        return processed;
    }

    public void setProcessedBitmap(Bitmap processed) {

        this.processed = processed;
    }

    public Bitmap getOriginalBitmap() {
        return original;
    }

    public void setOriginalBitmap(Bitmap original) {

        this.original = original;
    }

    public int getBallWidth() {
        return ballWidth;
    }

    public void setBallWidth(int ballWidth) {
        this.ballWidth = ballWidth;
    }

    public int getStickLength() {
        return stickLength;
    }

    public void setStickLength(int stickLength) {
        this.stickLength = stickLength;
    }

    public String getFootParamsAsString() {
        return String.valueOf(stickLength) + " " + String.valueOf(ballWidth);
    }

    public String getFeet() {
        return feet;
    }

    public void setFeet(String feet) {
        this.feet = feet;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(type);
        parcel.writeInt(result);
        parcel.writeParcelable(processed, i);
        parcel.writeInt(ballWidth);
        parcel.writeInt(stickLength);
        parcel.writeString(feet);
    }
}

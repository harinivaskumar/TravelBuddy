package com.hari.aund.travelbuddy.data;

import android.content.ContentValues;
import android.database.Cursor;

import com.hari.aund.travelbuddy.data.provider.PlaceColumns;

import java.util.ArrayList;

/**
 * Created by Hari Nivas Kumar R P on 8/17/2016.
 */
public class PlaceDetail {

    private static final String PLACE_INFO_SUFFIX = " - Place Information";
    private static final String NAME_PREFIX = "Name - ";
    private static final String ADDRESS_PREFIX = "\nAddress - ";
    private static final String PHONE_PREFIX = "\nContact No. - ";
    private static final String WEBSITE_PREFIX = "\nWebsite - ";
    private static final String NEARBY_PREFIX = "\n\nVicinity - ";
    private static final String EMPTY_STR = "";

    private String mId;
    private String mName;
    private String mVicinity;
    private String mWebsite;
    private String mPhoneNumber;
    private String mAddress;
    private Double mRating;
    private double mLatitude;
    private double mLongitude;
    private ArrayList<String> mPhotoReference;
    private ArrayList<String> mTimetable;
    private ArrayList<ReviewDetail> mReviewDetails;

    public PlaceDetail(String id) {
        mId = id;
        mPhotoReference = new ArrayList<>();
        mTimetable = new ArrayList<>();
        mReviewDetails = new ArrayList<>();
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getVicinity() {
        return mVicinity;
    }

    public void setVicinity(String vicinity) {
        mVicinity = vicinity;
    }

    public String getWebsite() {
        return mWebsite;
    }

    public void setWebsite(String website) {
        mWebsite = website;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public Double getRating() {
        return mRating;
    }

    public void setRating(Double rating) {
        mRating = rating;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public ArrayList<String> getPhotoReference() {
        return mPhotoReference;
    }

    public void setPhotoReference(ArrayList<String> photoReference) {
        mPhotoReference = photoReference;
    }

    public String getPhotoRef(int position) {
        return getPhotoReference().get(position);
    }

    public void addPhotoRef(String photoRef) {
        getPhotoReference().add(photoRef);
    }

    public ArrayList<String> getTimetable() {
        return mTimetable;
    }

    public void setTimetable(ArrayList<String> timetable) {
        mTimetable = timetable;
    }

    public String getTimeEntry(int position) {
        return getTimetable().get(position);
    }

    public void addTimeEntry(String timeEntry) {
        getTimetable().add(timeEntry);
    }

    public ArrayList<ReviewDetail> getReviewDetails() {
        return mReviewDetails;
    }

    public void setReviewDetails(ArrayList<ReviewDetail> reviewDetails) {
        mReviewDetails = reviewDetails;
    }

    public ReviewDetail getReviewEntry(int position) {
        return getReviewDetails().get(position);
    }

    public void addReviewEntry(ReviewDetail reviewDetail) {
        getReviewDetails().add(reviewDetail);
    }

    public boolean hasVicinity() {
        return getVicinity() != null;
    }

    public boolean hasWebsite() {
        return getWebsite() != null;
    }

    public boolean hasPhoneNumber() {
        return getPhoneNumber() != null;
    }

    public boolean hasAddress() {
        return getAddress() != null;
    }

    public boolean hasRating() {
        return mRating != null;
    }

    public boolean hasPhotoReference() {
        return !getPhotoReference().isEmpty();
    }

    public boolean hasTimeTable() {
        return !getTimetable().isEmpty();
    }

    public boolean hasReviews() {
        return !getReviewDetails().isEmpty();
    }

    public String toString() {
        String placeInfoStr = getShareName();
        placeInfoStr += getShareAddress();
        placeInfoStr += getShareContact();
        placeInfoStr += getShareWebsite();
        placeInfoStr += getShareVicinity();
        return placeInfoStr;
    }

    public String getPlaceTitle() {
        return getName() + PLACE_INFO_SUFFIX;
    }

    private String getShareName() {
        return NAME_PREFIX + getName();
    }

    private String getShareAddress() {
        return ADDRESS_PREFIX + getAddress();
    }

    private String getShareContact() {
        return hasPhoneNumber() ? PHONE_PREFIX + getPhoneNumber() : EMPTY_STR;
    }

    private String getShareWebsite() {
        return hasWebsite() ? WEBSITE_PREFIX + getWebsite() : EMPTY_STR;
    }

    private String getShareVicinity() {
        return hasVicinity() ? NEARBY_PREFIX + getVicinity() : EMPTY_STR;
    }

    public ContentValues getPlaceDetailsAsContentValues() {
        ContentValues placeContentValues = new ContentValues();

        placeContentValues.put(PlaceColumns.PLACE_ID, getId());
        placeContentValues.put(PlaceColumns.NAME, getName());
        placeContentValues.put(PlaceColumns.LATITUDE, getLatitude());
        placeContentValues.put(PlaceColumns.LONGITUDE, getLongitude());

        if (hasAddress())
            placeContentValues.put(PlaceColumns.ADDRESS, getAddress());

        if (hasVicinity())
            placeContentValues.put(PlaceColumns.VICINITY, getVicinity());

        if (hasPhotoReference())
            placeContentValues.put(PlaceColumns.PHOTO_COVER_REF, getPhotoRef(0));

        if (hasPhoneNumber())
            placeContentValues.put(PlaceColumns.PHONE_NUMBER, getPhoneNumber());

        if (hasWebsite())
            placeContentValues.put(PlaceColumns.WEBSITE, getWebsite());

        if (hasRating())
            placeContentValues.put(PlaceColumns.RATING, getRating());

        return placeContentValues;
    }

    public void updatePlaceDetailsFromCursor(Cursor placeCursor) {
        setName(placeCursor.getString(
                placeCursor.getColumnIndex(PlaceColumns.NAME)));

        setLatitude(Double.parseDouble(placeCursor.getString(
                placeCursor.getColumnIndex(PlaceColumns.LATITUDE))));

        setLongitude(Double.parseDouble(placeCursor.getString(
                placeCursor.getColumnIndex(PlaceColumns.LONGITUDE))));

        setAddress(placeCursor.getString(
                placeCursor.getColumnIndex(PlaceColumns.ADDRESS)));

        setVicinity(placeCursor.getString(
                placeCursor.getColumnIndex(PlaceColumns.VICINITY)));

        //TODO: add a new table for photo reference
        String photoRef = placeCursor.getString(
                placeCursor.getColumnIndex(PlaceColumns.PHOTO_COVER_REF));

        if (photoRef != null && !photoRef.isEmpty())
            addPhotoRef(photoRef);

        String phoneNumber = placeCursor.getString(
                placeCursor.getColumnIndex(PlaceColumns.PHONE_NUMBER));

        if (phoneNumber != null && !phoneNumber.isEmpty())
            setPhoneNumber(phoneNumber);

        String website = placeCursor.getString(
                placeCursor.getColumnIndex(PlaceColumns.WEBSITE));

        if (website != null && !website.isEmpty())
            setWebsite(website);

        String rating = placeCursor.getString(
                placeCursor.getColumnIndex(PlaceColumns.RATING));

        if (rating != null && !rating.isEmpty())
            setRating(Double.parseDouble(rating));
    }
}

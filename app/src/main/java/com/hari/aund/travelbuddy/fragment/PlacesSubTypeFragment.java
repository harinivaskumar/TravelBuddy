package com.hari.aund.travelbuddy.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hari.aund.travelbuddy.R;
import com.hari.aund.travelbuddy.adapter.PlacesListAdapter;
import com.hari.aund.travelbuddy.data.PlacesCategory;
import com.hari.aund.travelbuddy.data.PlacesListInfo;
import com.hari.aund.travelbuddy.parser.PlacesApiParser;
import com.hari.aund.travelbuddy.utils.DefaultValues;
import com.hari.aund.travelbuddy.utils.Utility;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;

/**
 * Created by Hari Nivas Kumar R P on 8/16/2016.
 */
public class PlacesSubTypeFragment extends Fragment
        implements DefaultValues {

    private static final String LOG_TAG = PlacesSubTypeFragment.class.getSimpleName();

    private static final int PREFERENCE_MODE_PRIVATE = 0;

    private int mCategoryId;
    private int mCategoryActivityId;
    private int mSectionNumber;
    private String mSectionName;
    private String mCategoryName;
    private Double mLatitude, mLongitude;
    private PlacesCategory mPlacesCategory;
    private PlacesListAdapter mPlacesListAdapter;
    private ArrayList<PlacesListInfo> mPlacesListInfoArray = new ArrayList<>();
    private ProgressWheel mProgressWheel;
    private SharedPreferences mSharedPreferences;
    private RecyclerView mRecyclerView;

    public PlacesSubTypeFragment() {
    }

    public static PlacesSubTypeFragment newInstance(int sectionNumber,
                                                    PlacesCategory placesCategory) {
        PlacesSubTypeFragment fragment = new PlacesSubTypeFragment();

        Bundle args = new Bundle();
        args.putInt(Utility.KEY_PLACE_SECTION_NUMBER, sectionNumber);
        args.putParcelable(Utility.KEY_PLACE_CATEGORY_INFO, placesCategory);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_places_sub_type, container, false);

        mSharedPreferences = getActivity().getPreferences(PREFERENCE_MODE_PRIVATE);
        readValues();

        setProgressWheel((ProgressWheel) rootView.findViewById(R.id.progress_wheel));
        getProgressWheel().spin();

        /* TODO: Set Category Activity name for Type 'C'
        if (mCategoryActivityId == PlacesCategoryValues.PLACES_CATEGORY_C_ACTIVITY) {
            PlacesSubTypeActivity placesCategoryActivity =
                    (PlacesSubTypeActivity) getActivity();
            ActionBar actionBar = placesCategoryActivity.getSupportActionBar();
            if (actionBar != null) {
                String activityNewTitle = mCategoryName +
                        " - " + mSectionName;
                actionBar.setTitle(activityNewTitle);
            }
        }
        */

        StaggeredGridLayoutManager sGridLayoutManager = new StaggeredGridLayoutManager(
                DEFAULT_COLUMN_COUNT_1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(sGridLayoutManager);

        if (mCategoryId != 0 && mCategoryName != null &&
                mPlacesListInfoArray.isEmpty() &&
                getLatitude() != null && getLongitude() != null) {
            createAndAddAdapterToView();
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor mPreferenceEditor = mSharedPreferences.edit();
        mPreferenceEditor.putInt(Utility.KEY_PLACE_SECTION_NUMBER,
                getSectionNumber());
        mPreferenceEditor.putInt(Utility.KEY_CATEGORY_ID,
                mPlacesCategory.getCategoryId());
        mPreferenceEditor.putString(Utility.KEY_PLACE_LATITUDE,
                getLatitude().toString());
        mPreferenceEditor.putString(Utility.KEY_PLACE_LONGITUDE,
                getLongitude().toString());
        mPreferenceEditor.apply();
        Log.d(LOG_TAG, "inside onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getSectionNumber() == 0 ||
                getLatitude() == null || getLongitude() == null) {
            Log.d(LOG_TAG, "section number | latitude | longitude is null");
            if (getSectionNumber() == 0)
                setSectionNumber(mSharedPreferences.getInt(
                        Utility.KEY_PLACE_SECTION_NUMBER, DEFAULT_SUB_TYPE_ID));

            int categoryId = mSharedPreferences.getInt(
                    Utility.KEY_CATEGORY_ID, DEFAULT_CATEGORY_ID);
            mPlacesCategory = new PlacesCategory(categoryId);

            mPlacesCategory.setLatitude(Double.valueOf(
                    mSharedPreferences.getString(
                            Utility.KEY_PLACE_LATITUDE,
                            DEFAULT_LATITUDE.toString())));
            mPlacesCategory.setLongitude(Double.valueOf(
                    mSharedPreferences.getString(
                            Utility.KEY_PLACE_LONGITUDE,
                            DEFAULT_LONGITUDE.toString())));
            initValues();
            createAndAddAdapterToView();
            Log.d(LOG_TAG, "inside onResume");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*
        if (savedInstanceState != null) {
            mSectionNumber = savedInstanceState.getInt(Utility.KEY_PLACE_SECTION_NUMBER);
            mPlacesCategory = savedInstanceState.getParcelable(Utility.KEY_PLACE_CATEGORY_INFO);
            Log.d(LOG_TAG, "onActivityCreated - savedInstanceState is Restored!");
            initValues();
        } else {
            Log.d(LOG_TAG, "onActivityCreated - savedInstanceState is Empty!");
        }
        */
        Log.d(LOG_TAG, "inside onActivityCreated");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*
        outState.putInt(Utility.KEY_PLACE_SECTION_NUMBER, mSectionNumber);
        outState.putParcelable(Utility.KEY_PLACE_CATEGORY_INFO, mPlacesCategory);
        Log.d(LOG_TAG, "onSaveInstanceState - savedInstanceState is Filled!");
        */
        Log.d(LOG_TAG, "inside onSavedInstanceState");
    }

    private void readValues() {
        if (getArguments() != null) {
            mSectionNumber = getArguments().getInt(Utility.KEY_PLACE_SECTION_NUMBER);
            mPlacesCategory = getArguments().getParcelable(Utility.KEY_PLACE_CATEGORY_INFO);
        }
        initValues();
    }

    private void initValues() {
        //Set Default Values if parcel is null; Def Place Kerala
        if (mPlacesCategory != null) {
            setCategoryActivityId();
            setSectionName();
            setCategoryId();
            setCategoryName();
            setLatitude();
            setLongitude();
        }
    }

    private void createAndAddAdapterToView() {
        new PlacesApiParser(this).getPlaceListDetails();

        mPlacesListAdapter = new PlacesListAdapter(getActivity(),
                mPlacesListInfoArray, mCategoryId, mCategoryName, mSectionName);
        mRecyclerView.setAdapter(mPlacesListAdapter);
    }

    public int getCategoryId() {
        return mCategoryId;
    }

    private void setCategoryId() {
        this.mCategoryId = mPlacesCategory.getCategoryId();
    }

    private void setCategoryId(int categoryId) {
        this.mCategoryId = categoryId;
    }

    public String getCategoryName() {
        return mCategoryName;
    }

    private void setCategoryName() {
        this.mCategoryName = mPlacesCategory.getCategoryName();
    }

    private void setCategoryName(String categoryName) {
        this.mCategoryName = categoryName;
    }

    public int getCategoryActivityId() {
        return mCategoryActivityId;
    }

    private void setCategoryActivityId() {
        this.mCategoryActivityId = mPlacesCategory.getCategoryActivityId();
    }

    public int getSectionNumber() {
        return mSectionNumber;
    }

    private void setSectionNumber(int sectionNumber) {
        this.mSectionNumber = sectionNumber;
    }

    public String getSectionName() {
        return mSectionName;
    }

    private void setSectionName() {
        this.mSectionName = mPlacesCategory.getSubTypeList().get(mSectionNumber - 1);
    }

    private void setSectionName(String sectionName) {
        this.mSectionName = sectionName;
    }

    public Double getLatitude() {
        return mLatitude;
    }

    private void setLatitude() {
        mLatitude = mPlacesCategory.getLatitude();
    }

    private void setLatitude(Double latitude) {
        mLatitude = latitude;
    }

    public Double getLongitude() {
        return mLongitude;
    }

    private void setLongitude() {
        mLongitude = mPlacesCategory.getLongitude();
    }

    private void setLongitude(Double longitude) {
        mLongitude = longitude;
    }

    public PlacesListAdapter getPlacesListAdapter() {
        return mPlacesListAdapter;
    }

    private void setPlacesListAdapter(PlacesListAdapter mPlacesListAdapter) {
        this.mPlacesListAdapter = mPlacesListAdapter;
    }

    public ArrayList<PlacesListInfo> getPlacesListInfoArray() {
        return mPlacesListInfoArray;
    }

    private void setPlacesListInfoArray(ArrayList<PlacesListInfo> mPlacesListInfoArray) {
        this.mPlacesListInfoArray = mPlacesListInfoArray;
    }

    public ProgressWheel getProgressWheel() {
        return mProgressWheel;
    }

    private void setProgressWheel(ProgressWheel mProgressWheel) {
        this.mProgressWheel = mProgressWheel;
    }
}

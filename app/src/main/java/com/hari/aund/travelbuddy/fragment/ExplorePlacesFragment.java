package com.hari.aund.travelbuddy.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.hari.aund.travelbuddy.R;
import com.hari.aund.travelbuddy.activity.MainActivity;
import com.hari.aund.travelbuddy.activity.PlacesCategoryActivity;
import com.hari.aund.travelbuddy.adapter.PlaceAutoCompleteAdapter;
import com.hari.aund.travelbuddy.utils.DefaultValues;
import com.hari.aund.travelbuddy.utils.Utility;
import com.hari.aund.travelbuddy.utils.gplaces.PlaceAutoComplete;

import java.util.ArrayList;

/**
 * Created by Hari Nivas Kumar R P on 8/13/2016.
 */
public class ExplorePlacesFragment extends Fragment
        implements GoogleApiClient.OnConnectionFailedListener,
        AdapterView.OnItemClickListener,
        View.OnClickListener, DefaultValues {

    private static final String LOG_TAG = ExplorePlacesFragment.class.getSimpleName();

    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int RESULTS_OK = -1;
    private static final int PREFERENCE_MODE_PRIVATE = 0;

    private int mNavSectionId;
    private String mNavSectionName;
    private String mNewPlaceId;
    private String mNewPlaceName;
    private static LatLngBounds mLatLngBounds = null;
    private static ArrayList<String> mPlacesIdAL = null, mPlacesNameAL = null;
    private static ArrayAdapter<String> mDefaultPlacesArrayAdapter = null;
    private static PlaceAutoCompleteAdapter mPlaceAutoCompleteAdapter = null;
    private ActionBar mActionBar = null;
    private GoogleApiClient mGoogleApiClient = null;
    private TextView mNewPlaceTextView;
    private SharedPreferences mSharedPreferences;

    public ExplorePlacesFragment() {
    }

    public static ExplorePlacesFragment getNewInstance(int navSectionId) {
        ExplorePlacesFragment explorePlacesFragment = new ExplorePlacesFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(Utility.KEY_NAVIGATION_SECTION_ID, navSectionId);
        explorePlacesFragment.setArguments(bundle);

        return explorePlacesFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        mActionBar = ((MainActivity) getActivity()).getSupportActionBar();

        mSharedPreferences = getActivity().getPreferences(PREFERENCE_MODE_PRIVATE);
        readValues();
        setFragmentTitle();

        mGoogleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .build();

        initValues();

        View rootView = inflater.inflate(R.layout.fragment_explore_places, container, false);

        setExplorePlacesVisibility(rootView);

        setDefaultPlacesArrayAdapter();
        ListView placesListView =
                (ListView) rootView.findViewById(R.id.places);
        placesListView.setAdapter(getDefaultPlacesArrayAdapter());
        placesListView.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor mPreferenceEditor = mSharedPreferences.edit();
        mPreferenceEditor.putInt(Utility.KEY_NAVIGATION_SECTION_ID,
                getNavSectionId());
        mPreferenceEditor.putString(Utility.KEY_PLACE_ID,
                getNewPlaceId());
        mPreferenceEditor.putString(Utility.KEY_PLACE_NAME,
                getNewPlaceName());
        mPreferenceEditor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getNavSectionId() == 0 || getNewPlaceId() == null ||
                getNewPlaceName() == null) {
            setNavSectionId(mSharedPreferences.getInt(
                    Utility.KEY_NAVIGATION_SECTION_ID, Utility.NAV_SECTION_EXPLORE_PLACES));
            setNewPlaceId(mSharedPreferences.getString(
                    Utility.KEY_PLACE_ID, DefaultValues.DEFAULT_PLACE_ID));
            setNewPlaceName(mSharedPreferences.getString(
                    Utility.KEY_PLACE_NAME, DefaultValues.DEFAULT_PLACE_NAME));
            setNavSectionName(Utility.getNavSectionName(getContext(), getNavSectionId()));
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null &&
                mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onClick(View view) {

        if (!Utility.isNetworkAvailable(getContext())){
            Log.d(LOG_TAG, "You are Offline! : onClick!");
            return;
        }

        if (view.getId() == R.id.find_place_on_map) {
            try {
                setNewPlaceId(getString(R.string.def_no_place_selected));
                setNewPlaceName(getString(R.string.def_no_place_selected));

                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Message - " + e.getMessage());
                e.printStackTrace();
            }
        } else if (view.getId() == R.id.new_place_found_on_map) {

            if (getNewPlaceName() == null ||
                    getNewPlaceName().equals(getResources()
                            .getString(R.string.def_no_place_selected))){
                Toast.makeText(getContext(),
                        "No New Place Selected.\nChoose one from the List!",
                        Toast.LENGTH_LONG)
                        .show();
                return;
            }

            Toast.makeText(getContext(), getNewPlaceName(), Toast.LENGTH_LONG)
                    .show();
            startPlacesCategoryActivity(getNewPlaceId(), getNewPlaceName());

        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        if (!Utility.isNetworkAvailable(getContext())){
            Log.d(LOG_TAG, "You are Offline! : onItemClick!");
            return;
        }

        String placeId = null, placeName = null;

        if (adapterView.getAdapter() instanceof PlaceAutoCompleteAdapter) {
            Log.d(LOG_TAG, "adapterView.getAdapter() instanceof PlaceAutoCompleteAdapter");

            PlaceAutoComplete place =
                    getPlaceAutoCompleteAdapter().getItem(position);

            placeId = place.getPlaceId();
            placeName = place.getPlaceDescription();

            setNewPlaceId(placeId);
            setNewPlaceName(placeName);
        } else if (adapterView.getAdapter() instanceof ArrayAdapter<?>) {
            Log.d(LOG_TAG, "adapterView.getAdapter() instanceof ArrayAdapter<String>");

            placeId = getDefaultPlaceId(position);
            placeName = getDefaultPlaceName(position);
        }

        if ((placeId != null) && (placeName != null)) {
            Log.d(LOG_TAG, "PlaceId - " + placeId + " Name - " + placeName);

            startPlacesCategoryActivity(placeId, placeName);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULTS_OK) {
                Place place = PlacePicker.getPlace(data, getContext());

                setNewPlaceId(place.getId());
                setNewPlaceName(place.getAddress().toString());

                Log.d(LOG_TAG, "PlaceId - " + place.getId());
                Log.d(LOG_TAG, "PlaceAddress - " + place.getAddress().toString());

                if (getView() != null) {
                    Snackbar.make(getView(), place.getAddress().toString(), Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        }
    }

    private void readValues() {
        if (getArguments() != null) {
            setNavSectionId(getArguments().getInt(
                    Utility.KEY_NAVIGATION_SECTION_ID));
            setNavSectionName(Utility.getNavSectionName(
                    getContext(), getNavSectionId()));
        }
    }

    private void setNavSectionId(int navSectionId) {
        this.mNavSectionId = navSectionId;
    }

    private int getNavSectionId() {
        return mNavSectionId;
    }

    private void setNavSectionName(String navSectionName) {
        this.mNavSectionName = navSectionName;
    }

    private String getNavSectionName() {
        return mNavSectionName;
    }

    private void setFragmentTitle() {
        if (mActionBar != null) {
            mActionBar.setTitle(getNavSectionName());
        }
    }

    private void initValues() {
        setDefaultPlacesIdAL();
        setUpDefaultPlacesNameAL();
        setBoundsGreaterSydney();
    }

    private void setBoundsGreaterSydney() {
        mLatLngBounds = new LatLngBounds(
                new LatLng(37.398160, -122.180831),
                new LatLng(37.430610, -121.972090)
        );
    }

    private LatLngBounds getBoundsGreaterSydney() {
        return mLatLngBounds;
    }

    private ArrayList<String> getDefaultPlacesIdAL() {
        return mPlacesIdAL;
    }

    private void setDefaultPlacesIdAL() {
        //TODO - move this to data
        mPlacesIdAL = new ArrayList<>();

        mPlacesIdAL.add("ChIJSdRbuoqEXjkRFmVPYRHdzk8");
        mPlacesIdAL.add("ChIJd7gN4_CECDsRZ7QW-3bwXco");
        mPlacesIdAL.add("ChIJbU60yXAWrjsR4E9-UejD3_g");
        mPlacesIdAL.add("ChIJpQoX1dIJGToRqD-zaCsOWPw");
        mPlacesIdAL.add("ChIJYTN9T-plUjoRM9RjaAunYW4");
        mPlacesIdAL.add("ChIJQbc2YxC6vzsRkkDzYv-H-Oo");
        mPlacesIdAL.add("ChIJlfcOXx8FWTkRLlJU7YfYG4Y");
        mPlacesIdAL.add("ChIJ9wH5Z8NTBDkRJXdLVsUE_nw");
        mPlacesIdAL.add("ChIJnaj_mSQJ4TgR8eeXRm16VgY");
        mPlacesIdAL.add("ChIJv8a-SlENCDsRkkGEpcqC1Qs");
        mPlacesIdAL.add("ChIJZ_YISduC-DkRvCxsj-Yw40M");
        mPlacesIdAL.add("ChIJW_Wc1P8SCDsRmXw47fuQvWQ");
        mPlacesIdAL.add("ChIJ5XOPmBBwrzsRCe4TG2b7kns");
        mPlacesIdAL.add("ChIJwe1EZjDG5zsRaYxkjY_tpF0");
        mPlacesIdAL.add("ChIJLbZ-NFv9DDkRzk0gTkm3wlI");
        mPlacesIdAL.add("ChIJARFGZy6_wjsRQ-Oenb9DjYI");
    }

    private String getDefaultPlaceId(int position) {
        return getDefaultPlacesIdAL().get(position);
    }

    private ArrayList<String> getDefaultPlacesNameAL() {
        return mPlacesNameAL;
    }

    private void setUpDefaultPlacesNameAL() {
        //TODO - move this to data
        mPlacesNameAL = new ArrayList<>();

        mPlacesNameAL.add("Ahmedabad, Gujarat, India");
        mPlacesNameAL.add("Alappuzha, Kerala, India");
        mPlacesNameAL.add("Bangalore, Karnataka, India");
        mPlacesNameAL.add("Bhubaneswar, Odisha, India");
        mPlacesNameAL.add("Chennai, Tamil Nadu, India");
        mPlacesNameAL.add("Goa, India");
        mPlacesNameAL.add("Gujarat, India");
        mPlacesNameAL.add("Himachal Pradesh, India");
        mPlacesNameAL.add("Jammu and Kashmir");
        mPlacesNameAL.add("Kochi, Kerala, India");
        mPlacesNameAL.add("Kolkata, West Bengal, India");
        mPlacesNameAL.add("Kerala, India");
        mPlacesNameAL.add("Mysuru, Karnataka, India");
        mPlacesNameAL.add("Mumbai, Maharashtra, India");
        mPlacesNameAL.add("New Delhi, India");
        mPlacesNameAL.add("Pune, Maharashtra, India");
    }

    private String getDefaultPlaceName(int position) {
        return getDefaultPlacesNameAL().get(position);
    }

    public ArrayAdapter<String> getDefaultPlacesArrayAdapter() {
        return mDefaultPlacesArrayAdapter;
    }

    private void setDefaultPlacesArrayAdapter() {
        mDefaultPlacesArrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                mPlacesNameAL);
    }

    private PlaceAutoCompleteAdapter getPlaceAutoCompleteAdapter() {
        return mPlaceAutoCompleteAdapter;
    }

    private void setPlaceAutoCompleteAdapter() {
        mPlaceAutoCompleteAdapter = new PlaceAutoCompleteAdapter(
                getActivity(),
                android.R.layout.simple_list_item_1,
                mGoogleApiClient,
                getBoundsGreaterSydney(),
                null);
    }

    private void setExplorePlacesVisibility(View rootView) {
        RelativeLayout relativeLayout =
                (RelativeLayout) rootView.findViewById(R.id.explore_places_layout);
        ImageView findPlace =
                (ImageView) rootView.findViewById(R.id.find_place_on_map);
        mNewPlaceTextView =
                (TextView) rootView.findViewById(R.id.new_place_found_on_map);
        AutoCompleteTextView searchPlace =
                (AutoCompleteTextView) rootView.findViewById(R.id.explore_place);

        //TODO: Read from Settings
        if (Utility.isPlacePickerSettingEnabled() &&
                getNavSectionId() == Utility.NAV_SECTION_EXPLORE_PLACES) {
            findPlace.setVisibility(View.VISIBLE);
            findPlace.setOnClickListener(this);

            mNewPlaceTextView.setVisibility(View.VISIBLE);
            mNewPlaceTextView.setOnClickListener(this);

            searchPlace.setVisibility(View.INVISIBLE);
        } else {
            findPlace.setVisibility(View.INVISIBLE);
            mNewPlaceTextView.setVisibility(View.INVISIBLE);

            setPlaceAutoCompleteAdapter();
            searchPlace.setVisibility(View.VISIBLE);
            searchPlace.setAdapter(getPlaceAutoCompleteAdapter());
            searchPlace.setOnItemClickListener(this);
        }
    }

    private void startPlacesCategoryActivity(String placeId, String placeName){

        if (!Utility.isNetworkAvailable(getContext())){
            Log.d(LOG_TAG, "You are Offline! : startPlacesCategoryActivity!");
            return;
        }

        Intent placesIntent = new Intent(getActivity(), PlacesCategoryActivity.class);
        placesIntent.putExtra(Utility.KEY_PLACE_ID, placeId);
        placesIntent.putExtra(Utility.KEY_PLACE_NAME, placeName);
        startActivity(placesIntent);
        getActivity().overridePendingTransition(
                R.animator.activity_open_translate, R.animator.activity_close_scale);
    }

    public String getNewPlaceName() {
        return mNewPlaceName;
    }

    public void setNewPlaceName(String newPlaceName) {
        mNewPlaceName = newPlaceName;
        setPlaceNameToTextView(getNewPlaceName());
    }

    public String getNewPlaceId() {
        return mNewPlaceId;
    }

    public void setNewPlaceId(String newPlaceId) {
        mNewPlaceId = newPlaceId;
    }

    private void setPlaceNameToTextView(String newPlace) {
        mNewPlaceTextView.setText(newPlace);
    }

    private String getPlaceNameFromTextView() {
        return mNewPlaceTextView.getText().toString();
    }
}

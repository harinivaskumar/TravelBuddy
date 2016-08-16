package com.hari.aund.travelbuddy.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hari.aund.travelbuddy.R;
import com.hari.aund.travelbuddy.adapter.PlacesCategoryAdapter;
import com.hari.aund.travelbuddy.app.TravelBuddyApp;
import com.hari.aund.travelbuddy.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class PlacesActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final String LOG_TAG = PlacesActivity.class.getSimpleName();

    private static final String TAG_RESULT = "result";
    private static final String TAG_GEOMETRY = "geometry";
    private static final String TAG_LOCATION = "location";

    private String mPlaceId = null;
    private String mPlaceName = null;
    private Double latitude = null;
    private Double longitude = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getIntent().getExtras() != null) {
            mPlaceId = getIntent().getStringExtra(Utility.KEY_PLACE_ID);
            mPlaceName = getIntent().getStringExtra(Utility.KEY_PLACE_NAME);
        }

        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setTitle(mPlaceName);
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        StaggeredGridLayoutManager sGridLayoutManager =
                new StaggeredGridLayoutManager(
                        Utility.PLACES_ACTIVITY_COLUMN_COUNT_PORTRAIT,
                        StaggeredGridLayoutManager.VERTICAL
                );

        PlacesCategoryAdapter placesCategoryAdapter = new PlacesCategoryAdapter(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(sGridLayoutManager);
        recyclerView.setAdapter(placesCategoryAdapter);

        FloatingActionButton favouriteFab =
                (FloatingActionButton) findViewById(R.id.fab_places_favourite);
        favouriteFab.setOnClickListener(this);

        getPlaceDetail();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab_places_favourite) {
            Snackbar.make(view, mPlaceName, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    public void getPlaceDetail() {
        String placesReqUrl = Utility.getPlacesUrl(mPlaceId);
        Log.d(LOG_TAG, "URL - " + placesReqUrl);
        JsonObjectRequest placesJsonObjReq =
                new JsonObjectRequest(
                        placesReqUrl,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                try {
                                    JSONObject results = jsonObject.getJSONObject(TAG_RESULT);
                                    JSONObject geometry = results.getJSONObject(TAG_GEOMETRY);
                                    JSONObject location = geometry.getJSONObject(TAG_LOCATION);

                                    latitude = Double.parseDouble(location.getString("lat"));
                                    longitude = Double.parseDouble(location.getString("lng"));

                                    Log.d(LOG_TAG, "PlaceId - " + mPlaceId);
                                    Log.d(LOG_TAG, "Place Name - " + mPlaceName);
                                    Log.d(LOG_TAG, "Latitude - " + latitude + " Longitude - " + longitude);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Log.e(LOG_TAG, "Volley : onErrorResponse - " + volleyError.getMessage());
                            }
                        }
                );

        TravelBuddyApp.getInstance()
                .addToRequestQueue(placesJsonObjReq);
    }
}
package directory.tripin.com.tripindirectory.formactivities.FormFragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import directory.tripin.com.tripindirectory.formactivities.PlacesViewHolder;
import directory.tripin.com.tripindirectory.R;
import directory.tripin.com.tripindirectory.helper.Logger;
import directory.tripin.com.tripindirectory.model.HubFetchedCallback;
import directory.tripin.com.tripindirectory.model.PartnerInfoPojo;
import directory.tripin.com.tripindirectory.model.RouteCityPojo;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class RouteFormFragment extends BaseFragment implements HubFetchedCallback {

    Query query;
    FirebaseAuth auth;
    DocumentReference mUserDocRef;
    FirestoreRecyclerOptions<PartnerInfoPojo> options;
    MyRecyclerViewAdapter adapterp, adapterd;
    List<String> listpickup, listdropoff;
    Map<String, Boolean> pickupHM;
    Map<String, Boolean> dropoffHM;
    Activity activity;
    View v;
    private TextView addPickUpCity, addDropOffCity;
    private Context mContext;
    private RecyclerView mPickUpList, mDropList;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    int mPlaceCode = 0;
    GeoDataClient mGeoDataClient;

    @Override
    public void onUpdate(PartnerInfoPojo partnerInfoPojo) {}

    public RouteFormFragment() {
        // Required empty public constructor
        listpickup = new ArrayList<>();
        listdropoff = new ArrayList<>();
        pickupHM = new HashMap<>();
        dropoffHM = new HashMap<>();
        adapterp = new MyRecyclerViewAdapter(listpickup, 1);
        adapterd = new MyRecyclerViewAdapter(listdropoff, 2);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        auth = FirebaseAuth.getInstance();
        mUserDocRef = FirebaseFirestore.getInstance()
                .collection("partners").document(auth.getUid());

        mUserDocRef.addSnapshotListener(getActivity(),new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if(documentSnapshot.exists()) {

                    PartnerInfoPojo partnerInfoPojo = documentSnapshot.toObject(PartnerInfoPojo.class);

                    Logger.v("Event Triggered, PICK DROP modified");
                    if (partnerInfoPojo.getmSourceCities() != null) {
                        pickupHM.clear();
                        pickupHM.putAll(partnerInfoPojo.getmSourceCities());
                        listpickup.clear();
                        listpickup.addAll(pickupHM.keySet());
                        adapterp.notifyDataSetChanged();
                        Logger.v("list pick up modified");
                    } else {
                        Logger.v("list pick up null");
                    }

                    if (partnerInfoPojo.getmDestinationCities() != null) {
                        dropoffHM.clear();
                        dropoffHM.putAll(partnerInfoPojo.getmDestinationCities());
                        listdropoff.clear();
                        listdropoff.addAll(dropoffHM.keySet());
                        adapterd.notifyDataSetChanged();
                        Logger.v("list drop modified");

                    } else {
                        Logger.v("list drop up null");
                    }
                }
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_route_form, container, false);

        addPickUpCity = v.findViewById(R.id.add_pickupcity);
        addDropOffCity = v.findViewById(R.id.add_dropcity);
        mPickUpList = v.findViewById(R.id.pickup_list);
        mDropList = v.findViewById(R.id.drop_list);
        mPickUpList.setLayoutManager(new LinearLayoutManager(activity));
        mDropList.setLayoutManager(new LinearLayoutManager(activity));
        mPickUpList.setAdapter(adapterp);
        mDropList.setAdapter(adapterd);
        setListners();

        return v;
    }

    private void setListners() {

        addPickUpCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlaceCode = 1;
                addPickUpCity.setText("Loading...");
                starttheplacesfragment();
            }
        });

        addDropOffCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlaceCode = 2;
                addDropOffCity.setText("Loading...");
                starttheplacesfragment();
            }
        });
    }

    @Override
    public void onDestinationHubFetched(String destinationhub, int operation) {
        switch (operation){
            case 0:{
                break;
            }
            case 1:{
                mUserDocRef.update("mDestinationHubs."+destinationhub.trim(),true);
                break;
            }
            case 2:{
                mUserDocRef.update("mDestinationHubs."+destinationhub.trim(),FieldValue.delete()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        for(String s: dropoffHM.keySet()){
                            updateDestinationHubss(s,1);
                        }
                    }
                });
                break;
            }
        }
    }

    @Override
    public void onSourceHubFetched(String sourcehub, int operation) {
        switch (operation){
            case 0:{
                break;
            }
            case 1:{
                mUserDocRef.update("mSourceHubs."+sourcehub.trim(),true);
                break;
            }
            case 2:{
                mUserDocRef.update("mSourceHubs."+sourcehub.trim(),FieldValue.delete()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        for(String s: pickupHM.keySet()){
                            updateSourceHubss(s,1);
                        }
                    }
                });
                break;
            }
        }
    }


    public class MyRecyclerViewAdapter extends RecyclerView.Adapter<PlacesViewHolder> {

        private List<String> mData;
        private int type = 0;

        // data is passed into the constructor
        public MyRecyclerViewAdapter(List<String> data, int type) {
            this.mData = data;
            this.type = type;
        }

        // inflates the row layout from xml when needed
        @Override
        public PlacesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_city, parent, false);
            PlacesViewHolder viewHolder = new PlacesViewHolder(view);
            return viewHolder;
        }

        // binds the data to the textview in each row
        @Override
        public void onBindViewHolder(final PlacesViewHolder holder, final int position) {
            String city = mData.get(position);
            holder.mCity.setText(city);
            holder.mRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.mCity.setText("Removing...");
                    //remove city
                    if (type == 1) {
                        //remove pickup
                        updateSourceHubss(mData.get(position),2);

                        mUserDocRef.update("mSourceCities." + mData.get(position),
                                FieldValue.delete()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getActivity(),"Pickup City Removed",Toast.LENGTH_SHORT).show();

                                notifyDataSetChanged();
                            }
                        });

                    }
                    if (type == 2) {
                        //remove drop off
                        updateDestinationHubss(mData.get(position),2);

                        mUserDocRef.update("mDestinationCities." + mData.get(position),
                                FieldValue.delete()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                notifyDataSetChanged();
                                Toast.makeText(getActivity(),"Drop City Removed",Toast.LENGTH_SHORT).show();

                            }
                        });

                    }

                }
            });
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return mData.size();
        }


    }



    private void starttheplacesfragment(){
        try {

            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                    .setCountry("IN")
                    .build();
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setFilter(typeFilter)
                            .build(getActivity());
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                if(data!=null){
                    final Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                    Logger.v("Place::: " + place.getName());
                    if(mPlaceCode==1){
                        pickupHM.put(place.getName().toString().toUpperCase(),true);
                        mUserDocRef.update("mSourceCities", pickupHM).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getActivity(),"Pick City Added",Toast.LENGTH_SHORT).show();
                                addPickUpCity.setText("Add More");
                                updateSourceHubs(place.getLatLng(),1);
                            }
                        });
//                    PartnerInfoPojo partnerInfoPojo = new PartnerInfoPojo();
//                    partnerInfoPojo.setmSourceCities(pickupHM);
//                    mUserDocRef.set(partnerInfoPojo, SetOptions.merge());

                    }
                    if(mPlaceCode==2){
                        dropoffHM.put(place.getName().toString().toUpperCase(),true);
                        mUserDocRef.update("mDestinationCities",dropoffHM).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getActivity(),"Drop City Added",Toast.LENGTH_SHORT).show();
                                addDropOffCity.setText("Add More");
                                updateDestinationHubs(place.getLatLng(),1);
                            }
                        });
//                    PartnerInfoPojo partnerInfoPojo = new PartnerInfoPojo();
//                    partnerInfoPojo.setDestinationCities(dropoffHM);
//                    mUserDocRef.set(partnerInfoPojo, SetOptions.merge());
                    }
                }


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                // TODO: Handle the error.

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                addPickUpCity.setText("Add");
                addDropOffCity.setText("Add");


            }
        }
    }

    private void updateDestinationHubs(LatLng city, int operation) {
           RouteCityPojo routeCityPojo = new RouteCityPojo(getActivity(),2, operation,this);
           routeCityPojo.setmLatLang(city);
    }
    private void updateDestinationHubss(String city, int operation) {
        RouteCityPojo routeCityPojo = new RouteCityPojo(getActivity(),2, operation,this);
        routeCityPojo.setmCityName(city);
    }

    private void updateSourceHubs(LatLng city, int operation) {
            RouteCityPojo routeCityPojo = new RouteCityPojo(getActivity(),1, operation,this);
            routeCityPojo.setmLatLang(city);
   }
    private void updateSourceHubss(String city, int operation) {
        RouteCityPojo routeCityPojo = new RouteCityPojo(getActivity(),1, operation,this);
        routeCityPojo.setmCityName(city);
    }
}

package com.driver.drowsers.fragments.map;

import android.Manifest;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.driver.drowsers.R;
import com.driver.drowsers.databinding.FragmentMapBinding;
import com.driver.drowsers.helper.Helper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

public class Map extends Fragment implements OnMapReadyCallback {

    private LinearLayout linearLayout;
    private ImageView menu, backPress;
    private TextView tone, homeText, profile, mapTop;

    private MapViewModel mViewModel;
    private FragmentMapBinding binding;

    Boolean isLocationPermissionGranted = false;

    private ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result ->
            {
                result.forEach((entry, status) -> {
                    if (entry.equals(Manifest.permission.ACCESS_FINE_LOCATION) || entry.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        isLocationPermissionGranted = status;
                    }

                });
                if (isLocationPermissionGranted) {
                    if (!Helper.isLocationEnabled(requireContext())) {
                        Helper.openGps(requireContext());
                    }
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(getLayoutInflater(), container, false);

        binding.currentMap.onCreate(savedInstanceState);
        binding.currentMap.getMapAsync(this);

        binding.currentMap.onResume();// needed to get the map to display immediately


        if (!Helper.isLocationPermissionGranted(requireContext()))
            requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
        else if (!Helper.isLocationEnabled(requireContext()))
            Helper.openGps(requireContext());


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        linearLayout = getActivity().findViewById(R.id.linearLayoutBottom);
        menu = getActivity().findViewById(R.id.menu_top);
        backPress = getActivity().findViewById(R.id.back_press);
        homeText = getActivity().findViewById(R.id.face_alert_top);
        tone = getActivity().findViewById(R.id.tone_top);
        profile = getActivity().findViewById(R.id.profile_top);
        mapTop = getActivity().findViewById(R.id.map_top);

        mapTop.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.VISIBLE);
        menu.setVisibility(View.VISIBLE);
        backPress.setVisibility(View.INVISIBLE);
        profile.setVisibility(View.INVISIBLE);
        homeText.setVisibility(View.INVISIBLE);
        tone.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        Log.i("MyValue", "onMapReady: called");
        // latitude and longitude
        double latitude = 17.385044;
        double longitude = 78.486671;

        // create marker
        MarkerOptions marker = new MarkerOptions().position(
                new LatLng(latitude, longitude)).title("Hello Maps");

        // Changing marker icon
        marker.icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

        // adding marker
        googleMap.addMarker(marker);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(33.6844, 73.0479)).zoom(12).build();
//        googleMap.animateCamera(CameraUpdateFactory
//                .newCameraPosition(cameraPosition));

        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.moveCamera(cameraUpdate);
    }

//    public void getLocation() {
//        double latitude = location.getLatitude();
//        double longitude = location.getLongitude();
//        Geocoder gc = new Geocoder(requireContext(), Locale.getDefault());
//        try {
//            List<Address> addresses = gc.getFromLocation(lat, lng, 1);
//            StringBuilder sb = new StringBuilder();
//            if (addresses.size() > 0) {
//                Address address = addresses.get(0);
//                for (int i = 0; i < address.getMaxAddressLineIndex(); i++)
//                    sb.append(address.getAddressLine(i)).append("\n");
//                sb.append(address.getLocality()).append("\n");
//                sb.append(address.getPostalCode()).append("\n");
//                sb.append(address.getCountryName());
//            }
//        }
//    }


    @Override
    public void onResume() {
        super.onResume();
        binding.currentMap.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.currentMap.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.currentMap.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.currentMap.onLowMemory();
    }
}
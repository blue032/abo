package com.example.myapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap gMap;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Marker cafeMarker;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.map);
        initGoogleMap(savedInstanceState);

        sharedPreferences = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);

        // 리스너 초기화
        // 리스너 초기화 및 등록
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if ("CongestionStatus".equals(key)) {
                    updateCongestionStatus(); // 값이 변경될 때마다 UI 업데이트
                }
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_home) {
                startActivity(new Intent(MapActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.action_board) {
                startActivity(new Intent(MapActivity.this, BoardActivity.class));
                return true;
            } else if (itemId == R.id.action_notification) {
                return true;
            } else if (itemId == R.id.action_mypage) {
                startActivity(new Intent(MapActivity.this, MypageActivity.class));
                return true;
            }
            return false;
        });
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        LatLng incheonUniversity = new LatLng(37.37502537368127, 126.63272006791813);
        gMap.addMarker(new MarkerOptions().position(incheonUniversity).title("Incheon University"));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(incheonUniversity, 15));
        checkLocationPermission();

        List<LatLng> locations = Arrays.asList(
                new LatLng(37.37500867159571, 126.63387174042461),
                new LatLng(37.37591543320427, 126.63281734018747),
                new LatLng(37.372401288059535, 126.6313160023207),
                new LatLng(37.37340586676641, 126.62985469283342),
                new LatLng(37.37439777449398, 126.63154896625312)
        );

        BitmapDescriptor icon = resizeMapIcons("location_green", 100, 100); // Adjust the size as needed

        for (LatLng location : locations) {
            gMap.addMarker(new MarkerOptions().position(location).icon(icon));
        }
        LatLng cafeLocation = new LatLng(37.37452483159567, 126.6332926552895); //0.0카페 위치
        cafeMarker = googleMap.addMarker(new MarkerOptions().position(cafeLocation).icon(icon));

        updateCongestionStatus();
    }

    private BitmapDescriptor resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resizedBitmap);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        canvas.drawBitmap(imageBitmap, null, new android.graphics.Rect(0, 0, width, height), paint);
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("위치 권한 거부됨")
                        .setMessage("위치 권한 없이는 현재 위치를 지도에 표시할 수 없습니다. 앱은 계속 작동하지만, 이 기능은 비활성화됩니다.")
                        .setPositiveButton("확인", (dialog, which) -> ActivityCompat.requestPermissions(MapActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                LOCATION_PERMISSION_REQUEST_CODE))
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            gMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    gMap.setMyLocationEnabled(true);
                }
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("위치 권한 거부됨")
                        .setMessage("위치 권한 없이는 현재 위치를 지도에 표시할 수 없습니다. 앱은 계속 작동하지만, 이 기능은 비활성화됩니다.")
                        .setPositiveButton("확인", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            }
        }
    }

    @Override
    protected void onResume() { //액티비티가 사용자에게 보여질 때마다 호출
        super.onResume();
        mapView.onResume();
        //혼잡도 정보 기반 아이콘 업뎃
        updateCongestionStatus();
    }

    private void updateCongestionStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);
        String status = sharedPreferences.getString("CongestionStatus", "icon_green"); // 기본값: icon_green
        String openStatus = sharedPreferences.getString("CafeOpenStatus", "open"); // 영업 상태 기본값: open


        BitmapDescriptor iconDescriptor;
        switch(status) {
            case "icon_green":
                iconDescriptor = resizeMapIcons("location_green", 100, 100);
                break;
            case "icon_blue":
                iconDescriptor = resizeMapIcons("location_blue", 100, 100);
                break;
            case "icon_red":
                iconDescriptor = resizeMapIcons("location_red", 100, 100);
                break;
            default:
                iconDescriptor = BitmapDescriptorFactory.defaultMarker(); // 기본 마커 아이콘
                break;
        }

        if (cafeMarker != null) {
            cafeMarker.setIcon(iconDescriptor);
        }

        ImageView imageView10 = findViewById(R.id.imageView10);
        int drawableId = getResources().getIdentifier(status, "drawable", getPackageName());
        imageView10.setImageResource(drawableId);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
        if (sharedPreferences != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}

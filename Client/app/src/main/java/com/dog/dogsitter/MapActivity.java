package com.dog.dogsitter;

import static androidx.constraintlayout.motion.widget.Debug.getLocation;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dog.dogsitter.structs.DataArticle;
import com.dog.dogsitter.structs.DataObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.ScreenPoint;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.location.FilteringMode;
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.LocationStatus;
import com.yandex.mapkit.map.BaseMapObjectCollection;
import com.yandex.mapkit.map.Callback;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CompositeIcon;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectDragListener;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkAnimation;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.TextStyle;
import com.yandex.mapkit.map.internal.PlacemarkMapObjectBinding;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.runtime.ui_view.ViewProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MapActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    MapView Map;
    MapKit MapKit;
    FloatingActionButton FabMyLocation, FabSearch;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    double MyLat, MyLng, NowLat, NowLng;
    String ApiKey="404a7e90-e6ff-4dbe-9706-9e0b4febab03";
    Boolean FlagPosition=true;
    ProgressDialog pd;
    List<DataObject> ListObjects;//список объектов на карте
    int CountQuery;//счетчик запросов

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_map);
        Map = findViewById(R.id.mapview);
        FabMyLocation=findViewById(R.id.fabMyLocationMap);
        FabSearch=findViewById(R.id.fabSearchMap);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        // убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }
        getLocation();
        MapKit = MapKitFactory.getInstance();
        MapKit.createLocationManager().subscribeForLocationUpdates(0,0, 0, true, FilteringMode.ON, new LocationListener() {
            @Override
            public void onLocationUpdated(@NonNull com.yandex.mapkit.location.Location location) {
                MyLng=location.getPosition().getLongitude();
                MyLat=location.getPosition().getLatitude();
                if(MyLng!=0 && MyLat!=0 && FlagPosition){
                    Map.getMap().move(
                            new CameraPosition(new Point(MyLat, MyLng), 14.0f, 0.0f, 0.0f),
                            new Animation(Animation.Type.SMOOTH, 0),
                            null);
                    FlagPosition=false;
                }
            }

            @Override
            public void onLocationStatusUpdated(@NonNull LocationStatus locationStatus) {
            }
        });
        //определение моего местоположения
        FabMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map.getMap().move(
                        new CameraPosition(new Point(MyLat, MyLng), 14.0f, 0.0f, 0.0f),
                        new Animation(Animation.Type.SMOOTH, 0),
                        null);
            }
        });
        //поиск объектов
        FabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map.getMap().getMapObjects().clear();//удаляем все маркеры
                //узнаем координаты текущего центра на карте
                float center_x=Map.getMapWindow().width() / 2f;
                float center_y=Map.getMapWindow().height() / 2f;
                ScreenPoint point = new ScreenPoint(center_x, center_y);
                Point point_m=Map.getMapWindow().screenToWorld(point);
                NowLat=point_m.getLatitude();
                NowLng=point_m.getLongitude();
                ListObjects=new ArrayList<>();
                CountQuery=0;
                pd = new ProgressDialog(MapActivity.this);
                pd.setTitle("Сообщение");
                pd.setMessage("Подождите, идет обработка данных.");
                // включаем анимацию ожидания
                pd.setIndeterminate(true);
                //не даем исчезнуть диалогу с сообщением
                pd.setCancelable(false);
                pd.show();
                ListMarkers=new ArrayList<>();
                SearchObjectsMap("груминг-салон, ветеринар");
            }
        });
    }

    //событие при активизации активности
    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        Map.onStart();
        Map.getMap().setRotateGesturesEnabled(true);
    }

    //событие при закрытии активности
    @Override
    protected void onStop() {
        Map.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    // Permission Denied
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //Get location
    public void getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (myLocation == null)myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
    }
    List<PlacemarkMapObject> ListMarkers;
    //поиск объектов на карте
    private void SearchObjectsMap(String name_object){
        RequestParams params = new RequestParams();
        params.put("apikey", ApiKey);
        params.put("text", name_object);
        params.put("lang", "ru_RU");
        params.put("type", "biz");
        params.put("results", 50);
        params.put("ll", Double.toString(NowLng)+","+Double.toString(NowLat));
        params.put("spn", "0.552069,0.400552");
        //отправка данных на сервер
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://search-maps.yandex.ru/v1/", params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                //получение ответа от сервера
                String response=new String(responseBody);
                response=response.trim();
                DataObject data;
                System.out.println("RESULT="+response);
                CountQuery++;
                try {
                    JSONObject obj=new JSONObject(response);
                    JSONArray array=obj.getJSONArray("features");
                    for(int i=0;i<array.length();i++){
                        data=new DataObject();
                        obj=array.getJSONObject(i);
                        JSONObject properties=obj.getJSONObject("properties");
                        JSONObject meta_data=properties.getJSONObject("CompanyMetaData");
                        String description=meta_data.getString("name")+"\nАдрес: "+meta_data.getString("address")+"\n";
                        if(!meta_data.isNull("Phones")) {
                            JSONArray phones = meta_data.getJSONArray("Phones");
                            if (phones.length() > 0) {
                                description += "Телефоны: ";
                                for (int j = 0; j < phones.length(); j++) {
                                    JSONObject phone = phones.getJSONObject(j);
                                    description += phone.getString("formatted");
                                    if (j < phones.length() - 1) description += ", ";
                                }
                            }
                        }
                        data.Description=description;
                        JSONObject geometry=obj.getJSONObject("geometry");
                        if(geometry.getString("type").equals("Point")){
                            JSONArray coordinates=geometry.getJSONArray("coordinates");
                            data.Lng=coordinates.getDouble(0);
                            data.Lat=coordinates.getDouble(1);
                        }
                        data.Category=CountQuery;
                        ListObjects.add(data);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                if(CountQuery==1)SearchObjectsMap("ветеринар");
                if(CountQuery==2)SearchObjectsMap("парк");
                if(CountQuery==3)ShowObjectMap();
            }
            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();
                System.out.println("ERROR="+error.toString());
                Toast.makeText(getApplicationContext(),"Произошла ошибка.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //вывод на карту объектов
    private void ShowObjectMap(){
        for(int i=0;i<ListObjects.size();i++){
            DataObject data=ListObjects.get(i);
            PlacemarkMapObject placemark=Map.getMap().getMapObjects().addPlacemark(new Point(data.Lat, data.Lng));
            if(data.Category==1)placemark.setIcon(ImageProvider.fromResource(getApplicationContext(), R.drawable.icon_saloon));
            if(data.Category==2)placemark.setIcon(ImageProvider.fromResource(getApplicationContext(), R.drawable.icon_medical));
            if(data.Category==3)placemark.setIcon(ImageProvider.fromResource(getApplicationContext(), R.drawable.icon_dog));
            placemark.setUserData(data.Description);
            placemark.addTapListener(new MapObjectTapListener() {
                @Override
                public boolean onMapObjectTap(MapObject mapObject, Point point) {
                    String text=(String)mapObject.getUserData();
                    Toast.makeText(getApplicationContext(),text, Toast.LENGTH_LONG).show();
                    return true;
                }});
            pd.dismiss();
        }
    }
}

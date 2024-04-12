package com.dog.dogsitter;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.dog.dogsitter.structs.DataObject;
import com.dog.dogsitter.structs.FavoritePlace;
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
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectDragListener;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkAnimation;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.TextStyle;
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
    MapObjectCollection MapObjects;
    MapKit MapKit;
    FloatingActionButton FabMyLocation, FabSearch;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    double MyLat, MyLng, NowLat, NowLng;
    String ApiKey="404a7e90-e6ff-4dbe-9706-9e0b4febab03";
    Boolean FlagPosition=true;
    ProgressDialog pd;
    List<DataObject> ListObjects;//список объектов на карте
    int CountQuery;//счетчик запросов
    int IdObject;

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
                CountQuery=0;IdObject=0;
                pd = new ProgressDialog(MapActivity.this);
                pd.setTitle("Сообщение");
                pd.setMessage("Подождите, идет обработка данных.");
                // включаем анимацию ожидания
                pd.setIndeterminate(true);
                //не даем исчезнуть диалогу с сообщением
                pd.setCancelable(false);
                pd.show();
                SearchObjectsMap("груминг-салон, ветеринар");
            }
        });
        if(Service.IdUser>0)GetFavoritePlaces();
    }
    //загрузка координат избранных мест
    private void GetFavoritePlaces(){
        ProgressDialog pd;RequestParams params = new RequestParams();
        params.put("id_user", Service.IdUser);
        pd = new ProgressDialog(MapActivity.this);
        pd.setTitle("Сообщение");
        pd.setMessage("Подождите, идет подготовка данных.");
        // включаем анимацию ожидания
        pd.setIndeterminate(true);
        //не даем исчезнуть диалогу с сообщением
        pd.setCancelable(false);
        pd.show();
        //отправка данных на сервер
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(Service.UrlServer+"/GetFavoritePlacesController", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray array=response.getJSONArray("array");
                    Service.ListFavoritePlaces=new ArrayList<>();
                    FavoritePlace data;
                    for(int i=0;i<array.length();i++){
                        JSONObject obj=array.getJSONObject(i);
                        data=new FavoritePlace();
                        data.Lat=obj.getDouble("lat");
                        data.Lng=obj.getDouble("lng");
                        Service.ListFavoritePlaces.add(data);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                pd.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                pd.dismiss();
                Toast toast = Toast.makeText(getApplicationContext(), "Произошла ошибка.", Toast.LENGTH_SHORT);
                toast.show();
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
    //подготовка
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
                CountQuery=CountQuery+1;
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
                        data.Id=IdObject;IdObject++;
                        data.Description=description;
                        JSONObject geometry=obj.getJSONObject("geometry");
                        if(geometry.getString("type").equals("Point")){
                            JSONArray coordinates=geometry.getJSONArray("coordinates");
                            data.Lng=coordinates.getDouble(0);
                            data.Lat=coordinates.getDouble(1);
                        }
                        data.Category=CountQuery;
                        data.Favorite=CheckFavoritePlace(data);
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
    DataObject DataObj;
    //вывод на карту объектов
    private void ShowObjectMap(){
        ListMarkers=new ArrayList<>();
        for(int i=0;i<ListObjects.size();i++){
            DataObject data=ListObjects.get(i);
            ListMarkers.add(AddMarker(data));
        }
        pd.dismiss();
    }
    private MapObjectTapListener MarkerMapObjectTapListener = new MapObjectTapListener(){
        @Override
        public boolean onMapObjectTap(@NonNull MapObject mapObject, @NonNull Point point) {
            // Обработка нажатия на маркер
            int id_object=(int)mapObject.getUserData();
            for(int j=0;j<ListObjects.size();j++){
                DataObj = ListObjects.get(j);
                if(id_object==DataObj.Id){
                    AlertDialog modal_data=null;
                    //получаем вид с файла
                    LayoutInflater li = LayoutInflater.from(MapActivity.this);
                    View modalView = li.inflate(R.layout.modal_data_object_map, null);
                    TableRow tableRow1;
                    TextView textData;
                    Button buttonFav;
                    tableRow1=modalView.findViewById(R.id.tableRow1ObjectMap);
                    textData=modalView.findViewById(R.id.textViewDataObjectMap);
                    buttonFav=modalView.findViewById(R.id.buttonFavObjectMap);
                    textData.setText(DataObj.Description);
                    if(Service.IdUser>0) {
                        if (DataObj.Favorite) buttonFav.setText("Удалить из избранного");
                        else buttonFav.setText("В избранное");
                        tableRow1.setVisibility(View.VISIBLE);
                        buttonFav.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                DataObj.Favorite=!DataObj.Favorite;
                                for(int k=0;k<ListObjects.size();k++)
                                    if(ListObjects.get(k).Id==DataObj.Id){
                                        ListObjects.set(k, DataObj);
                                        break;
                                    }
                                //если убираем объект из избранного, то удаляем его из списка
                                if(DataObj.Favorite==false) {
                                    for (int k = 0; k < Service.ListFavoritePlaces.size(); k++)
                                        if (DataObj.Lat == Service.ListFavoritePlaces.get(k).Lat && DataObj.Lng == Service.ListFavoritePlaces.get(k).Lng) {
                                            Service.ListFavoritePlaces.remove(k);
                                            break;
                                        }
                                    buttonFav.setText("В избранное");
                                }
                                //если добавление избранного
                                if(DataObj.Favorite){
                                    FavoritePlace data_coord=new FavoritePlace();
                                    data_coord.Lat=DataObj.Lat;
                                    data_coord.Lng=DataObj.Lng;
                                    Service.ListFavoritePlaces.add(data_coord);
                                    buttonFav.setText("Удалить из избранного");
                                }
                                //обновляем данные по избранным в бд
                                ProgressDialog pd;
                                RequestParams params = new RequestParams();
                                params.put("favorite", DataObj.Favorite);
                                params.put("id_user", Service.IdUser);
                                params.put("lat", DataObj.Lat);
                                params.put("lng", DataObj.Lng);
                                pd = new ProgressDialog(MapActivity.this);
                                pd.setTitle("Сообщение");
                                pd.setMessage("Подождите, идет подготовка данных.");
                                // включаем анимацию ожидания
                                pd.setIndeterminate(true);
                                //не даем исчезнуть диалогу с сообщением
                                pd.setCancelable(false);
                                pd.show();
                                //отправка данных на сервер
                                AsyncHttpClient client = new AsyncHttpClient();
                                client.get(Service.UrlServer+"/DoFavoritePlaceController", params, new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onStart() {

                                    }
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        //Map.getMap().getMapObjects().clear();//удаляем все маркеры
                                        ShowIconsMap();
                                        pd.dismiss();
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                        pd.dismiss();
                                        System.out.println("ERROR="+error.toString());
                                        Toast toast = Toast.makeText(getApplicationContext(), "Произошла ошибка.", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                });
                            }
                        });
                    }else tableRow1.setVisibility(View.GONE);
                    //Создаем AlertDialog
                    AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(MapActivity.this);
                    //Настраиваем modal для нашего AlertDialog:
                    aDialogBuilder.setView(modalView);
                    aDialogBuilder.setCancelable(false);
                    aDialogBuilder.setPositiveButton("Закрыть",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                    //создаем AlertDialog
                    modal_data = aDialogBuilder.create();
                    //и отображаем его
                    modal_data.show();
                    modal_data.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));//убираем черный фон
                    break;
                }
            }
            return true;
        }
    };
    private void ShowIconsMap(){
        for(int i=0;i<ListObjects.size();i++) {
            DataObject data = ListObjects.get(i);
            PlacemarkMapObject placemark=ListMarkers.get(i);
            if(data.Category==1){
                if(data.Favorite)placemark.setIcon(ImageProvider.fromResource(getApplicationContext(), R.drawable.icon_saloon_favorite));
                else placemark.setIcon(ImageProvider.fromResource(getApplicationContext(), R.drawable.icon_saloon));
            }
            if(data.Category==2){
                if(data.Favorite)placemark.setIcon(ImageProvider.fromResource(getApplicationContext(), R.drawable.icon_medical_favorite));
                else placemark.setIcon(ImageProvider.fromResource(getApplicationContext(), R.drawable.icon_medical));
            }
            if(data.Category==3){
                if(data.Favorite)placemark.setIcon(ImageProvider.fromResource(getApplicationContext(), R.drawable.icon_dog_favorite));
                else placemark.setIcon(ImageProvider.fromResource(getApplicationContext(), R.drawable.icon_dog));
            }
        }
    }
    //формирование маркера для карты
    private PlacemarkMapObject AddMarker(DataObject data){
        PlacemarkMapObject placemark=Map.getMap().getMapObjects().addPlacemark(new Point(data.Lat, data.Lng));
        if(data.Category==1){
            if(data.Favorite)placemark.setIcon(ImageProvider.fromResource(getApplicationContext(), R.drawable.icon_saloon_favorite));
            else placemark.setIcon(ImageProvider.fromResource(getApplicationContext(), R.drawable.icon_saloon));
        }
        if(data.Category==2){
            if(data.Favorite)placemark.setIcon(ImageProvider.fromResource(getApplicationContext(), R.drawable.icon_medical_favorite));
            else placemark.setIcon(ImageProvider.fromResource(getApplicationContext(), R.drawable.icon_medical));
        }
        if(data.Category==3){
            if(data.Favorite)placemark.setIcon(ImageProvider.fromResource(getApplicationContext(), R.drawable.icon_dog_favorite));
            else placemark.setIcon(ImageProvider.fromResource(getApplicationContext(), R.drawable.icon_dog));
        }
        placemark.setUserData(data.Id);
        placemark.addTapListener(MarkerMapObjectTapListener);
        return placemark;
    }
    //определяем является ли место избранным
    private boolean CheckFavoritePlace(DataObject object){
        boolean result=false;
        FavoritePlace data;
        for(int i=0;i<Service.ListFavoritePlaces.size();i++){
            data=Service.ListFavoritePlaces.get(i);
            if(data.Lat==object.Lat && data.Lng==object.Lng){
                result=true;
                break;
            }
        }
        return result;
    }
}

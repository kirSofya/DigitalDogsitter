package com.dog.dogsitter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.dog.dogsitter.structs.DataUser;
import com.dog.dogsitter.structs.FavoritePlace;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.yandex.mapkit.MapKitFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    TableRow TableRowSignIn, TableRowReg, TableRowFindDogsitters, TableRowData, TableRowMyDogs, TableRowMyArticles, TableRowArticles;
    Button ButtonSignIn, ButtonReg, ButtonDogsitters, ButtonData, ButtonMyDogs, ButtonMyArticles, ButtonArticles, ButtonMap, ButtonOut;
    TextView TextViewUserStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageLogo=(ImageView) findViewById(R.id.imageLogo);
        TableRowSignIn=(TableRow) findViewById(R.id.tableRowSignIn);
        TableRowReg=(TableRow) findViewById(R.id.tableRowReg);
        TableRowFindDogsitters=(TableRow) findViewById(R.id.tableRowFindDogsitters);
        TableRowData=(TableRow) findViewById(R.id.tableRowData);
        TableRowMyDogs=(TableRow) findViewById(R.id.tableRowMyDogs);
        TableRowMyArticles=(TableRow) findViewById(R.id.tableRowMyRowArticles);
        TableRowArticles=(TableRow) findViewById(R.id.tableRowArticles);
        ButtonSignIn=(Button) findViewById(R.id.buttonSignIn);
        ButtonReg=(Button) findViewById(R.id.buttonReg);
        ButtonDogsitters=(Button) findViewById(R.id.buttonFindDogsitters);
        ButtonData=(Button) findViewById(R.id.buttonData);
        ButtonMyDogs=(Button) findViewById(R.id.buttonMyDogs);
        ButtonMyArticles=(Button) findViewById(R.id.buttonMyArticles);
        ButtonArticles=(Button) findViewById(R.id.buttonArticles);
        ButtonMap=(Button) findViewById(R.id.buttonMap);
        ButtonOut=(Button) findViewById(R.id.buttonOut);
        TextViewUserStatus=(TextView) findViewById(R.id.textViewUserStatus);
        TextViewUserStatus.setText("");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        // убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //узнаем размер экрана
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metricsB = new DisplayMetrics();
        display.getMetrics(metricsB);
        Service.ScreenWidth=metricsB.widthPixels;
        Service.ScreenHeight=metricsB.heightPixels;
        //масштабируем верхнее изображение
        Bitmap bm= BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.logo);
        double proc, new_width=0, new_height=0;
        double raz;
        if(bm.getWidth()>Service.ScreenWidth){
            raz=bm.getWidth()-Service.ScreenWidth;
            proc=(int)(raz/bm.getWidth()*100.0);
            new_width=bm.getWidth()-(bm.getWidth()/100.0*proc);
            new_height=bm.getHeight()-(bm.getHeight()/100.0*proc);
        }else{
            raz=Service.ScreenWidth-bm.getWidth();
            proc=(int)(raz/Service.ScreenWidth*100.0);
            new_width=bm.getWidth()+(Service.ScreenWidth/100.0*proc);
            new_height=bm.getHeight()+(Service.ScreenWidth/100.0*proc);
        }
        bm=Service.GetResizedBitmap(bm, (int)new_width, (int)new_height);
        //изменяем размеры ImageView под новый размер картинки
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) imageLogo.getLayoutParams();//получаем параметры
        params.width=(int)new_width;
        params.height = (int)new_height;
        imageLogo.setLayoutParams(params);
        imageLogo.setImageBitmap(bm);
        //нажата кнопка 'Авторизация'
        ButtonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegSignInActivity.class);
                intent.putExtra("Oper", 2);
                startActivity(intent);
            }
        });
        //нажата кнопка 'Регистрация'
        ButtonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegSignInActivity.class);
                intent.putExtra("Oper", 1);
                startActivity(intent);
            }
        });
        //нажата кнопка 'Личные данные'
        ButtonData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DataUserActivity.class);
                startActivity(intent);
            }
        });
        //нажата кнопка 'Мои собаки'
        ButtonMyDogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Service.FlagUpdate=true;
                Intent intent = new Intent(MainActivity.this, DogsUserActivity.class);
                intent.putExtra("IdUser", Service.IdUser);
                startActivity(intent);
            }
        });
        //нажата кнопка 'Мои статьи'
        ButtonMyArticles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Service.FlagUpdate=true;
                Intent intent = new Intent(MainActivity.this, ArticlesUserActivity.class);
                startActivity(intent);
            }
        });
        //нажата кнопка 'Статьи'
        ButtonArticles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ArticlesActivity.class);
                startActivity(intent);
            }
        });
        //нажата кнопка 'Найти догситтеров'
        ButtonDogsitters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Service.FlagUpdate=false;
                Intent intent = new Intent(MainActivity.this, DogsittersActivity.class);
                startActivity(intent);
            }
        });
        //нажата кнопка 'Карта'
        ButtonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
        //нажата кнопка 'Выход'
        ButtonOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Сообщение")
                        .setMessage("Закрыть приложение?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        });
        MapKitFactory.setApiKey("8171fba7-1917-4438-92d9-0dd32ad0a9ab");
    }
    //событие при активизации активности
    @Override
    protected void onStart(){
        super.onStart();
        SetVisibleMainMenu();
    }
    //установка видимости ячеек таблицы в зависимости от вида пользователя
    public void SetVisibleMainMenu(){
        if(Service.IdUser==-1){
            TableRowSignIn.setVisibility(View.VISIBLE);
            TableRowReg.setVisibility(View.VISIBLE);
            TableRowData.setVisibility(View.GONE);
            TableRowMyDogs.setVisibility(View.GONE);
            TableRowMyArticles.setVisibility(View.GONE);
            TextViewUserStatus.setText("неавторизованный пользователь");
        }
        if(Service.IdUser>0){
            TableRowSignIn.setVisibility(View.GONE);
            TableRowReg.setVisibility(View.GONE);
            TableRowData.setVisibility(View.VISIBLE);
            TableRowMyDogs.setVisibility(View.VISIBLE);
            TableRowMyArticles.setVisibility(View.VISIBLE);
            TextViewUserStatus.setText(Service.EmailUser);
        }
    }
}

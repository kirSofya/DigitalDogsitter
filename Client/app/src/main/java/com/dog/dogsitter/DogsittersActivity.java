package com.dog.dogsitter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.dog.dogsitter.structs.DataDog;
import com.dog.dogsitter.structs.DataUser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class DogsittersActivity extends AppCompatActivity {
    ListView ListViewDogsitters;
    List<DataUser> ListDogsitters;//список догситтеров
    AdapterDogsitters Adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dogsitters);
        ListViewDogsitters=findViewById(R.id.listViewDogsitters);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        // убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        GetListDogsitters();
    }
    //событие при активизации активности
    @Override
    protected void onStart(){
        super.onStart();
        if(Service.FlagUpdate)GetListDogsitters();
    }
    //получение списка догситтеров
    private void GetListDogsitters(){
        ProgressDialog pd;
        pd = new ProgressDialog(DogsittersActivity.this);
        pd.setTitle("Сообщение");
        pd.setMessage("Подождите, идет обработка данных.");
        // включаем анимацию ожидания
        pd.setIndeterminate(true);
        //не даем исчезнуть диалогу с сообщением
        pd.setCancelable(false);
        pd.show();
        //отправка данных на сервер
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(Service.UrlServer+"/GetDogsittersController", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                final Handler h = new Handler() {//слушатель ответа
                    public void handleMessage(android.os.Message msg) {
                        pd.dismiss();
                        Adapter = new AdapterDogsitters(DogsittersActivity.this);
                        ListViewDogsitters.setAdapter(Adapter);
                    };
                };
                Thread t = new Thread(new Runnable() {
                    @SuppressLint("Range")
                    public void run() {
                        try {
                            JSONArray array=response.getJSONArray("array");
                            ListDogsitters=new ArrayList<>();
                            DataUser data;
                            for(int i=0;i<array.length();i++){
                                JSONObject obj=array.getJSONObject(i);
                                data=new DataUser();
                                data.Id=obj.getInt("id");
                                data.Name=obj.getString("name");
                                data.DateBirth=obj.getString("date_birth");
                                data.Age=obj.getInt("age");
                                data.Email=obj.getString("email");
                                data.Phone=obj.getString("phone");
                                data.Rating=obj.getDouble("rating");
                                ListDogsitters.add(data);
                            }
                            Message msg=h.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("result", "");
                            msg.setData(bundle);
                            h.sendMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.start();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                pd.dismiss();
                Toast toast = Toast.makeText(getApplicationContext(), "Произошла ошибка.", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
    public class AdapterDogsitters extends BaseAdapter {
        private LayoutInflater mLayoutInflater;

        public AdapterDogsitters(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return ListDogsitters.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public void getString(int position) {
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)convertView = mLayoutInflater.inflate(R.layout.list_dogsitters, null);
            TextView textViewName=convertView.findViewById(R.id.textViewNameListDogsitters);
            TextView textViewDateBirth=convertView.findViewById(R.id.textViewDateBirthListDogsitters);
            TextView textViewAge=convertView.findViewById(R.id.textViewAgeListDogsitters);
            TextView textViewEmail=convertView.findViewById(R.id.textViewEmailListDogsitters);
            TextView textViewPhone=convertView.findViewById(R.id.textViewPhoneListDogsitters);
            TextView textViewRating=convertView.findViewById(R.id.textViewRatingDogsitters);
            FloatingActionButton fabAboutUser=convertView.findViewById(R.id.fabAboutUserListDogsitters);
            DataUser data=ListDogsitters.get(position);
            textViewName.setText("ФИО: "+data.Name);
            textViewDateBirth.setText("Дата рождения: "+data.DateBirth);
            textViewAge.setText("Возраст: "+data.Age);
            textViewEmail.setText("Email: "+data.Email);
            textViewPhone.setText("Телефон: "+data.Phone);
            textViewRating.setText("Рейтинг: "+String.format("%.2f", data.Rating));
            fabAboutUser.setTag(data.Id);
            //подробная информация о догситтере
            fabAboutUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Service.FlagUpdate=false;
                    Intent intent = new Intent(DogsittersActivity.this, AboutUserActivity.class);
                    intent.putExtra("IdUser", (int)view.getTag());
                    startActivity(intent);
                }
            });
            return convertView;
        }
    }
}

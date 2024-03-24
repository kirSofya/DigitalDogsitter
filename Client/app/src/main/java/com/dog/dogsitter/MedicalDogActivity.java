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
import com.dog.dogsitter.structs.DataMedicalDog;
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

public class MedicalDogActivity extends AppCompatActivity {
    ListView ListViewMedical;
    FloatingActionButton FabAdd;
    TextView TextViewTitle;
    int IdUser=0, IdDog=0;
    List<DataMedicalDog> ListMedicalDog;//список медицинских данных по собаке
    AdapterMedicalDog Adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_dog);
        ListViewMedical=findViewById(R.id.listViewMedicalDog);
        FabAdd=findViewById(R.id.fabAddMedicalDog);
        TextViewTitle=findViewById(R.id.textViewNameDogMedicalDog);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        // убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //получаем значение операции
        Bundle arguments = getIntent().getExtras();
        IdUser = Integer.parseInt(arguments.get("IdUser").toString());
        IdDog = Integer.parseInt(arguments.get("IdDog").toString());
        if(Service.IdUser==IdUser) {
            FabAdd.setVisibility(View.VISIBLE);
            //добавление данных
            FabAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Service.FlagUpdate = false;
                    Intent intent = new Intent(MedicalDogActivity.this, AddEditMedicalDog.class);
                    intent.putExtra("Oper", 1);
                    intent.putExtra("IdDog", IdDog);
                    startActivity(intent);
                }
            });
        }else FabAdd.setVisibility(View.GONE);
        GetDataDog(IdDog);
        GetListMedicalDog();
    }
    //событие при активизации активности
    @Override
    protected void onStart(){
        super.onStart();
        if(Service.FlagUpdate)GetListMedicalDog();
    }
    //получение данных о собаке
    private void GetDataDog(int id_dog){
        ProgressDialog pd;
        RequestParams params = new RequestParams();
        params.put("id_dog", id_dog);
        pd = new ProgressDialog(MedicalDogActivity.this);
        pd.setTitle("Сообщение");
        pd.setMessage("Подождите, идет обработка данных.");
        // включаем анимацию ожидания
        pd.setIndeterminate(true);
        //не даем исчезнуть диалогу с сообщением
        pd.setCancelable(false);
        pd.show();
        //отправка данных на сервер
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(Service.UrlServer + "/GetDataDogController", params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                pd.dismiss();
                String response=new String(responseBody);
                response=response.trim();
                try {
                    JSONObject obj=new JSONObject(response);
                    String title="Медицинские данные: ";
                    title+=obj.getString("name")+" ";
                    title+=" ("+obj.getString("breed")+")";
                    TextViewTitle.setText(title);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();
                Toast toast = Toast.makeText(getApplicationContext(), "Произошла ошибка.", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
    //получение медицинских данных о собаке
    private void GetListMedicalDog(){
        ProgressDialog pd;
        RequestParams params = new RequestParams();
        params.put("id_dog", IdDog);
        pd = new ProgressDialog(MedicalDogActivity.this);
        pd.setTitle("Сообщение");
        pd.setMessage("Подождите, идет обработка данных.");
        // включаем анимацию ожидания
        pd.setIndeterminate(true);
        //не даем исчезнуть диалогу с сообщением
        pd.setCancelable(false);
        pd.show();
        //отправка данных на сервер
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(Service.UrlServer+"/GetListMedicalDogController", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                final Handler h = new Handler() {//слушатель ответа
                    public void handleMessage(android.os.Message msg) {
                        pd.dismiss();
                        Adapter = new AdapterMedicalDog(MedicalDogActivity.this);
                        ListViewMedical.setAdapter(Adapter);
                    };
                };
                Thread t = new Thread(new Runnable() {
                    @SuppressLint("Range")
                    public void run() {
                        try {
                            JSONArray array=response.getJSONArray("array");
                            ListMedicalDog=new ArrayList<>();
                            DataMedicalDog data;
                            for(int i=0;i<array.length();i++){
                                JSONObject obj=array.getJSONObject(i);
                                data=new DataMedicalDog();
                                data.Id=obj.getInt("id");
                                data.Date=obj.getString("date");
                                data.Description=obj.getString("description");
                                ListMedicalDog.add(data);
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
    public class AdapterMedicalDog extends BaseAdapter {
        private LayoutInflater mLayoutInflater;

        public AdapterMedicalDog(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return ListMedicalDog.size();
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
            if (convertView == null)convertView = mLayoutInflater.inflate(R.layout.list_medical_dog, null);
            TextView textViewDate=convertView.findViewById(R.id.textViewDateListMedicalDog);
            TextView textViewDescription=convertView.findViewById(R.id.textViewDescriptionListMedicalDog);
            FloatingActionButton fabEdit=convertView.findViewById(R.id.fabEditListMedicalDog);
            FloatingActionButton fabDelete=convertView.findViewById(R.id.fabDeleteListMedicalDog);
            DataMedicalDog data=ListMedicalDog.get(position);
            textViewDate.setText(data.Date);
            textViewDescription.setText(data.Description);
            if(Service.IdUser==IdUser) {
                fabEdit.setTag(data.Id);
                //редактирование данных о собаке
                fabEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Service.FlagUpdate=false;
                        Intent intent = new Intent(MedicalDogActivity.this, AddEditMedicalDog.class);
                        intent.putExtra("Oper", 2);
                        intent.putExtra("IdMedical", (int)view.getTag());
                        startActivity(intent);
                    }
                });
                fabDelete.setTag(data.Id);
                //удаление данных о собаке
                fabDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog alertDialog = new AlertDialog.Builder(MedicalDogActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Сообщение")
                                .setMessage("Удалить данные о собаке?")
                                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ProgressDialog pd;
                                        RequestParams params = new RequestParams();
                                        params.put("id_medical", (int) view.getTag());
                                        pd = new ProgressDialog(MedicalDogActivity.this);
                                        pd.setTitle("Сообщение");
                                        pd.setMessage("Подождите, идет обработка данных.");
                                        // включаем анимацию ожидания
                                        pd.setIndeterminate(true);
                                        //не даем исчезнуть диалогу с сообщением
                                        pd.setCancelable(false);
                                        pd.show();
                                        //отправка данных на сервер
                                        AsyncHttpClient client = new AsyncHttpClient();
                                        client.post(Service.UrlServer + "/DeleteMedicalDogController", params, new AsyncHttpResponseHandler() {
                                            @Override
                                            public void onStart() {

                                            }

                                            @Override
                                            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                                                pd.dismiss();
                                                Toast.makeText(getApplicationContext(), "Удаление прошло успешно.", Toast.LENGTH_SHORT).show();
                                                GetListMedicalDog();
                                            }

                                            @Override
                                            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                                                pd.dismiss();
                                                Toast.makeText(getApplicationContext(), "Произошла ошибка.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
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
            }else{
                fabEdit.setVisibility(View.GONE);
                fabDelete.setVisibility(View.GONE);
            }
            return convertView;
        }
    }
}

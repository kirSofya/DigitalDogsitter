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

import com.dog.dogsitter.structs.DataArticle;
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

public class ArticlesUserActivity extends AppCompatActivity {
    ListView ListViewArticles;//список статей пользователя
    List<DataArticle> ListArticles;
    FloatingActionButton FabAdd;
    AdapterArticles Adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles_user);
        ListViewArticles=findViewById(R.id.listViewArticlesUser);
        FabAdd=findViewById(R.id.fabAddArticlesUser);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        // убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //добавление данных
        FabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Service.FlagUpdate = false;
                Intent intent = new Intent(ArticlesUserActivity.this, AddEditArticleActivity.class);
                intent.putExtra("Oper", 1);
                startActivity(intent);
            }
        });
    }
    //событие при активизации активности
    @Override
    protected void onStart(){
        super.onStart();
        if(Service.FlagUpdate)GetListArticles();
    }
    //получение списка статей пользователя
    private void GetListArticles(){
        ProgressDialog pd;
        RequestParams params = new RequestParams();
        params.put("id_user", Service.IdUser);
        pd = new ProgressDialog(ArticlesUserActivity.this);
        pd.setTitle("Сообщение");
        pd.setMessage("Подождите, идет обработка данных.");
        // включаем анимацию ожидания
        pd.setIndeterminate(true);
        //не даем исчезнуть диалогу с сообщением
        pd.setCancelable(false);
        pd.show();
        //отправка данных на сервер
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(Service.UrlServer+"/GetArticlesUserController", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                final Handler h = new Handler() {//слушатель ответа
                    public void handleMessage(android.os.Message msg) {
                        pd.dismiss();
                        Adapter = new AdapterArticles(ArticlesUserActivity.this);
                        ListViewArticles.setAdapter(Adapter);
                    };
                };
                Thread t = new Thread(new Runnable() {
                    @SuppressLint("Range")
                    public void run() {
                        try {
                            JSONArray array=response.getJSONArray("array");
                            ListArticles=new ArrayList<>();
                            DataArticle data;
                            for(int i=0;i<array.length();i++){
                                JSONObject obj=array.getJSONObject(i);
                                data=new DataArticle();
                                data.Id=obj.getInt("id");
                                data.DateText=obj.getString("date_text");
                                data.Title=obj.getString("title");
                                data.Description=obj.getString("description");
                                ListArticles.add(data);
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
    public class AdapterArticles extends BaseAdapter {
        private LayoutInflater mLayoutInflater;

        public AdapterArticles(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return ListArticles.size();
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
            if (convertView == null)convertView = mLayoutInflater.inflate(R.layout.list_articles_user, null);
            TextView textViewName=convertView.findViewById(R.id.textViewNameListArticlesUser);
            TextView textViewDate=convertView.findViewById(R.id.textViewDateListArticlesUser);
            TextView textViewDescription=convertView.findViewById(R.id.textViewDescriptionListArticlesUser);
            FloatingActionButton fabDescription=convertView.findViewById(R.id.fabDescriptionListArticlesUser);
            FloatingActionButton fabEdit=convertView.findViewById(R.id.fabEditListArticlesUser);
            FloatingActionButton fabDelete=convertView.findViewById(R.id.fabDeleteListArticlesUser);
            DataArticle data=ListArticles.get(position);
            textViewName.setText(data.Title);
            textViewDate.setText(data.DateText);
            textViewDescription.setText(data.Description);
            fabDescription.setTag(data.Id);
            //подробная информация о статье
            fabDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Service.FlagUpdate=false;
                    Intent intent = new Intent(ArticlesUserActivity.this, DescriptionArticleActivity.class);
                    intent.putExtra("IdArticle", (int)view.getTag());
                    startActivity(intent);
                }
            });
            fabEdit.setTag(data.Id);
            //редактирование данных по статье
            fabEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Service.FlagUpdate=false;
                    Intent intent = new Intent(ArticlesUserActivity.this, AddEditArticleActivity.class);
                    intent.putExtra("Oper", 2);
                    intent.putExtra("IdArticle", (int)view.getTag());
                    startActivity(intent);
                }
            });
            fabDelete.setTag(data.Id);
            //удаление данных о статье
            fabDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog alertDialog = new AlertDialog.Builder(ArticlesUserActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Сообщение")
                            .setMessage("Удалить данные о собаке?")
                            .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ProgressDialog pd;
                                    RequestParams params = new RequestParams();
                                    params.put("id_article", (int) view.getTag());
                                    pd = new ProgressDialog(ArticlesUserActivity.this);
                                    pd.setTitle("Сообщение");
                                    pd.setMessage("Подождите, идет обработка данных.");
                                    // включаем анимацию ожидания
                                    pd.setIndeterminate(true);
                                    //не даем исчезнуть диалогу с сообщением
                                    pd.setCancelable(false);
                                    pd.show();
                                    //отправка данных на сервер
                                    AsyncHttpClient client = new AsyncHttpClient();
                                    client.post(Service.UrlServer + "/DeleteArticleController", params, new AsyncHttpResponseHandler() {
                                        @Override
                                        public void onStart() {

                                        }

                                        @Override
                                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                                            pd.dismiss();
                                            Toast.makeText(getApplicationContext(), "Удаление прошло успешно.", Toast.LENGTH_SHORT).show();
                                            GetListArticles();
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
            return convertView;
        }
    }
}

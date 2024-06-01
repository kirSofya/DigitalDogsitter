package com.dog.dogsitter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.dog.dogsitter.structs.DataArticle;
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

public class ArticlesActivity extends AppCompatActivity {
    EditText EditTextValue;
    Button ButtonSearch;
    ListView ListViewArticles;
    List<DataArticle> ListArticles;//список статей
    AdapterArticles Adapter;

    View.OnClickListener ButtonSearchOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SearchArticles();
        }
    };

    JsonHttpResponseHandler SearchArticlesControllerPost = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            final Handler h = new Handler() {//слушатель ответа
                public void handleMessage(android.os.Message msg) {
//                    pd.dismiss();
                    Adapter = new AdapterArticles(ArticlesActivity.this);
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
                            data.IdUser= obj.getInt("id_user");
                            data.DateText=obj.getString("date");
                            data.Title=obj.getString("title");
                            data.InfoUser=obj.getString("user");
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
//            pd.dismiss();
            Toast toast = Toast.makeText(getApplicationContext(), "Произошла ошибка.", Toast.LENGTH_SHORT);
            toast.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles);
        EditTextValue=findViewById(R.id.editTextValueArticles);
        ButtonSearch=findViewById(R.id.buttonSearchArticles);
        ListViewArticles=findViewById(R.id.listViewArticles);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        // убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //нажата кнопка 'Найти'
        ButtonSearch.setOnClickListener(ButtonSearchOnClickListener);
        SearchArticles();
    }
    //поиск статей
    private void SearchArticles(){
        String search=EditTextValue.getText().toString();
        ProgressDialog pd;
        RequestParams params = new RequestParams();
        params.put("search", search);
        pd = new ProgressDialog(ArticlesActivity.this);
        pd.setTitle("Сообщение");
        pd.setMessage("Подождите, идет обработка данных.");
        // включаем анимацию ожидания
        pd.setIndeterminate(true);
        //не даем исчезнуть диалогу с сообщением
        pd.setCancelable(false);
        pd.show();
        //отправка данных на сервер
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(Service.UrlServer+"/SearchArticlesController", params, SearchArticlesControllerPost);
        pd.dismiss();
    }
    public class AdapterArticles extends BaseAdapter {
        private LayoutInflater mLayoutInflater;

        View.OnClickListener fabDescriptionOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Service.FlagUpdate=false;
                Intent intent = new Intent(ArticlesActivity.this, DescriptionArticleActivity.class);
                intent.putExtra("IdArticle", (int)view.getTag());
                startActivity(intent);
            }
        };

        View.OnClickListener fabUserOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ArticlesActivity.this, AboutUserActivity.class);
                intent.putExtra("IdUser", (int)view.getTag());
                startActivity(intent);
            }
        };

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
            if (convertView == null)convertView = mLayoutInflater.inflate(R.layout.list_articles, null);
            TextView textViewTitle=convertView.findViewById(R.id.textViewTitleListArticles);
            TextView textViewUser=convertView.findViewById(R.id.textViewUserListArticles);
            TextView textViewDate=convertView.findViewById(R.id.textViewDateListArticles);
            TextView textViewDescription=convertView.findViewById(R.id.textViewDescriptionListArticles);
            FloatingActionButton fabDescription=convertView.findViewById(R.id.fabDescriptionListArticles);
            FloatingActionButton fabUser=convertView.findViewById(R.id.fabAboutUserListArticles);
            DataArticle data=ListArticles.get(position);
            textViewTitle.setText(data.Title);
            textViewUser.setText(data.InfoUser);
            textViewDate.setText(data.DateText);
            textViewDescription.setText(data.Description);
            fabDescription.setTag(data.Id);
            //подробная информация о статье
            fabDescription.setOnClickListener(fabDescriptionOnClickListener);
            fabUser.setTag(data.IdUser);
            //редактирование данных по статье
            fabUser.setOnClickListener(fabUserOnClickListener);
            return convertView;
        }
    }
}

package com.dog.dogsitter;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

public class DescriptionArticleActivity extends AppCompatActivity {
    TextView TextViewTitle, TextViewDate, TextViewDescription;

    AsyncHttpResponseHandler GetArticleUserControllerPost = new AsyncHttpResponseHandler() {
        @Override
        public void onStart() {

        }

        @Override
        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
//            pd.dismiss();
            String response=new String(responseBody);
            response=response.trim();
            try {
                JSONObject obj=new JSONObject(response);
                TextViewTitle.setText(obj.getString("title"));
                TextViewDate.setText(obj.getString("date_text"));
                TextViewDescription.setText(obj.getString("description"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
//            pd.dismiss();
            Toast toast = Toast.makeText(getApplicationContext(), "Произошла ошибка.", Toast.LENGTH_SHORT);
            toast.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description_article);
        TextViewTitle=findViewById(R.id.textViewTitleDescriptionArticle);
        TextViewDate=findViewById(R.id.textViewDateDescriptionArticle);
        TextViewDescription=findViewById(R.id.textViewDescriptionArticle);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        // убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //получаем код статьи
        Bundle arguments = getIntent().getExtras();
        int id_article= Integer.parseInt(arguments.get("IdArticle").toString());
        GetArticle(id_article);
    }
    //получение статьи
    private void GetArticle(int id_article){
        ProgressDialog pd;
        RequestParams params = new RequestParams();
        params.put("id_article", id_article);
        pd = new ProgressDialog(DescriptionArticleActivity.this);
        pd.setTitle("Сообщение");
        pd.setMessage("Подождите, идет обработка данных.");
        // включаем анимацию ожидания
        pd.setIndeterminate(true);
        //не даем исчезнуть диалогу с сообщением
        pd.setCancelable(false);
        pd.show();
        //отправка данных на сервер
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(Service.UrlServer + "/GetArticleUserController", params, GetArticleUserControllerPost);
        pd.dismiss();
    }
}

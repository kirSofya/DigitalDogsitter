package com.dog.dogsitter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;

//класс сервисных операций и общих данных
public class Service {
    public static int IdUser = -1;//код пользователя, если -1, то неавторизованный пользователь, если больше 0 то авторизованный пользователь
    public static String EmailUser="";//электронная почта пользователя
    public static int ScreenWidth, ScreenHeight;//ширина и высота экрана
    public static String UrlServer="http://82.146.55.27";//адрес сервера
    public static Boolean FlagUpdate;//флаг для обновления списков
    //масштабирование изображения
    public static Bitmap GetResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }
    //масштабирование изображения под размер активности, scale - процент от ширины разрешения экрана
    public static Bitmap ScaleImage(int scale, Bitmap image){
        Bitmap result=null;
        int w, h, new_w;
        w=image.getWidth();
        h=image.getHeight();
        int start_w=w;int start_h=h;
        new_w=(int)(Service.ScreenWidth/100.0*scale);
        if(w>new_w) {
            double sootn_w=(double)start_w/start_h;
            double rw=w;
            do {
                rw-=sootn_w;h--;
            } while (rw > new_w);
            if(w<=0 || h<=0){
                w=start_w;h=start_h;
            }else w=(int)rw;
            result=GetResizedBitmap(image, w, h);
        }else result=image;
        return result;
    }
    //определяем размер изображения находящегося в bitmap в байтах
    public static int SizeOfBitmap(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        } else {
            return data.getByteCount();
        }
    }
    //проверка на корректность ввода даты в формате MySQL
    public static boolean CheckDate(String date){
        try {
            Date d=java.sql.Date.valueOf(date);
        }catch (Exception ex){
            return false;
        }
        return true;
    }
    //преобразование bitmap в string_base64
    public static String EncodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }
    //преобразование string_base64 в bitmap
    public static Bitmap DecodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}

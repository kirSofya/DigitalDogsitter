package com.dog.dogsitter.structs;

import com.yandex.mapkit.map.PlacemarkMapObject;

//класс описывающий данные по объекту на крате
public class DataObject {
    public int Id, Category;
    public double Lat, Lng;
    public String Description;
    public boolean Favorite;//является место избранным или нет
}

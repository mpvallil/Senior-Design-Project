package ncsu.project15.ece484_project15_client;

import android.app.Activity;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.text.Layout;
import android.util.JsonReader;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

// Printer objects that will be received by server and updated on the map
public class Printer {
    private String name;
    private LatLng location;

    Printer() {}

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {

        return name;
    }

    void setLocation(LatLng location) {
        this.location = location;
    }

    LatLng getLocation() {
        return location;
    }

    public JsonObject getJsonObject() {
        Gson gson = new Gson();
        return new JsonParser().parse(gson.toJson(this)).getAsJsonObject();
    }
}

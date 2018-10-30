package ncsu.project15.ece484_project15_client;

import android.util.JsonReader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

// Printer objects that will be received by server and updated on the map
public class Printer {
    private String name;

    public Printer(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {

        return name;
    }

    public String getJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}

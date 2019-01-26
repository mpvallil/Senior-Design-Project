package ink.plink.plinkApp;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// Printer objects that will be received by server and updated on the map
public class Printer {
    private String printer_id;
    private String user_id;
    private double lat;
    private double lng;
    private int status;
    private int color;
    private String printer_name;
    private double price;
    private String address;
    private double distance;
    private String printer_type;

    Printer() {}

    Printer setPrinterId(String printer_id) {
        this.printer_id = printer_id;
        return this;
    }

    public String getPrinterId() {
        return this.printer_id;
    }

    Printer setUserId(String user_id) {
        this.user_id = user_id;
        return this;
    }

    public String getUserId() {
        return this.user_id;
    }

    Printer setLocation(LatLng location) {
        this.lat = location.latitude;
        this.lng = location.longitude;
        return this;
    }

    LatLng getLocation() {
        return new LatLng(this.lat, this.lng);
    }

    JsonObject getJsonObject() {
        Gson gson = new Gson();
        return new JsonParser().parse(gson.toJson(this)).getAsJsonObject();
    }

    String getPrinterType() {
        return this.printer_type;
    }

    Printer setStatus(boolean status) {
        if (status) {
            this.status = 1;
        } else {
            this.status = 0;
        }
        return this;
    }

    Boolean getStatus() {
        if (this.status != 0) {
            return true;
        } else {
            return false;
        }
    }

    String getStatusAsString() {
        if (this.status != 0) {
            return "Active";
        } else {
            return "Offline";
        }
    }

    Printer setColor(boolean color) {
        if (color) {
            this.color = 1;
        } else {
            this.color = 0;
        }
        return this;
    }

    Boolean getColor() {
        if (this.color != 0) {
            return true;
        } else {
            return false;
        }
    }

    Printer setName(String name) {
        this.printer_name = name;
        return this;
    }

    String getName() {
        return this.printer_name;
    }

    Printer setPrice(double price) {
        this.price = price;
        return this;
    }

    Double getPrice() {
        return this.price;
    }

    Printer setAddress(String address) {
        this.address = address;
        return this;
    }

    String getAddress() {
        return this.address;
    }

    Printer setDistance(double distance) {
        this.distance = distance;
        return this;
    }

    Double getDistance() {
        return this.distance;
    }

    Printer setPrinterType(String type) {
        this.printer_type = type;
        return this;
    }

    String getJsonAsString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    static Printer[] getPrinterList(String printerJSON) {
        Printer[] printers;
        Gson gson = new Gson();
        //JsonElement json = new JsonParser().parse(printerJSON);
        printers = gson.fromJson(printerJSON, Printer[].class);
        return printers;
    }
}

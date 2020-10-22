package com.ilazlow.fridgeinventory.adapter;

public class InventoryInfo {
    public String id;
    public String name;
    public String ean;
    public String value;
    public String rec_value;
    public String timestamp;
    public String fridge;
    public String image_thumb_url;
    public String last_update;

    public InventoryInfo(String id, String name, String ean, String value, String rec_value, String timestamp, String fridge, String image_thumb_url, String last_update) {
        this.id = id;
        this.name = name;
        this.ean = ean;
        this.value = value;
        this.rec_value = rec_value;
        this.timestamp = timestamp;
        this.fridge = fridge;
        this.image_thumb_url = image_thumb_url;
        this.last_update = last_update;
    }
}

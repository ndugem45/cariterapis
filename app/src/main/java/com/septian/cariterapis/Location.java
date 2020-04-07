package com.septian.cariterapis;

public class Location {

    private String name,loc,distance,id,wa,jam,hari,phone;
    private Double lat,lng;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getJam() {
        return jam;
    }

    public void setJam(String jam) {
        this.jam = jam;
    }

    public String getHari() {
        return hari;
    }

    public void setHari(String hari) {
        this.hari = hari;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWa() {
        return wa;
    }

    public void setWa(String wa) {
        this.wa = wa;
    }

    public String getName(){
            return name;
    }

    public void setName(String name){
            this.name = name;
        }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}

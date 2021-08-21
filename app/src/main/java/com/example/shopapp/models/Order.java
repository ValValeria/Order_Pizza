package com.example.shopapp.models;

public class Order {
    private String username;
    private int count;
    private String dishKey;
    private String time;
    private String status;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getDishKey() {
        return dishKey;
    }

    public void setDishKey(String dishKey) {
        this.dishKey = dishKey;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

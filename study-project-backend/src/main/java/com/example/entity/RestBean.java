package com.example.entity;


import lombok.Data;

@Data
public class RestBean<T> {
    private T message;
    private int status;
    private boolean success;

    public RestBean(int status,boolean success,T message) {
        this.message = message;
        this.status = status;
        this.success = success;
    }

    public static <T> RestBean<T> success(){
        return new RestBean<>(200,true,null);
    }

    public static <T> RestBean<T> success(T data){
        return new RestBean<>(200, true, data);
    }

    public static <T> RestBean<T> failure(int status){
        return new RestBean<>(status,false,null);
    }

    public static <T> RestBean<T> failure(int status,T data){
        return new RestBean<>(status, false, data);
    }
}

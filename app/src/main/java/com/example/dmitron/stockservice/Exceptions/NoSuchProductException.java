package com.example.dmitron.stockservice.Exceptions;

public class NoSuchProductException extends Exception {
    private String description;

    public NoSuchProductException(String str){
        description = str;
    }

    public NoSuchProductException(){

    }

    public String getDescription() {
        return description;
    }
}

package com.example.dmitron.stockservice.exceptions;

public class InvalidRequestException extends Exception {
    private String description;

    public InvalidRequestException(String str){
        description = str;
    }

    public InvalidRequestException(){

    }

    public String getDescription() {
        return description;
    }
}

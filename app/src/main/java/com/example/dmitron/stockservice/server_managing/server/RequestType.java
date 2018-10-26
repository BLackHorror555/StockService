package com.example.dmitron.stockservice.server_managing.server;

/**
 * Type of message that client and server send
 */
public enum RequestType {
    PRODUCT_INFO, BUYING, OPERATION_ACCEPTED, OPERATION_PROHIBITED, SELLING, CLOSE_CONNECTION
}

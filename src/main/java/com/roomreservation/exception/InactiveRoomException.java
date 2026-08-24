package com.roomreservation.exception;

public class InactiveRoomException extends RuntimeException {

    public InactiveRoomException(String message) {
        super(message);
    }
}

package com.gestioncomercial.api.identity.application;

public class IdentityException extends RuntimeException {
    private final int status;
    private final String code;
    private final String title;

    public IdentityException(int status, String code, String title, String message) {
        super(message);
        this.status = status;
        this.code = code;
        this.title = title;
    }

    public int status() { return status; }
    public String code() { return code; }
    public String title() { return title; }
}

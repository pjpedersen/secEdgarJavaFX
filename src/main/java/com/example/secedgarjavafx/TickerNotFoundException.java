package com.example.secedgarjavafx;

public class TickerNotFoundException extends Exception {
    private String ticker;

    public TickerNotFoundException(String ticker) {
        this.ticker = ticker;
    }

    public String getTicker() {
        return ticker;
    }
}

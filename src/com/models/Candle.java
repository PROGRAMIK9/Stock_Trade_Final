package com.models;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Candle {
    private long timestamp;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;

    public Candle(long timestamp, double open, double high, double low, double close, long volume) {
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public double getClose() { return close; }
    public long getTimestamp() { return timestamp; }
    
    // Helper to print date nicely
    public String getDate() {
        return Instant.ofEpochSecond(timestamp)
                      .atZone(ZoneId.systemDefault())
                      .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public long getVolume() {
		return volume;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setClose(double close) {
		this.close = close;
	}
}
package com.errorcab.model;

import java.time.LocalDateTime;

/**
 * Represents a payment transaction for an ERRORCab booking.
 */
public class Payment {
    private int id;
    private int bookingId;
    private double amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionRef;
    private LocalDateTime paidAt;

    public Payment(int id, int bookingId, double amount, PaymentMethod method, PaymentStatus status,
                   String transactionRef, LocalDateTime paidAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.transactionRef = transactionRef;
        this.paidAt = paidAt != null ? paidAt : LocalDateTime.now();
    }

    public Payment(int bookingId, double amount, PaymentMethod method) {
        this(0, bookingId, amount, method, PaymentStatus.PENDING, "TXN-" + System.currentTimeMillis() % 1000000, LocalDateTime.now());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}

package com.errorcab.copilot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Visual budget intelligence summary estimating cab, food, activity, and buffer amounts.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BudgetBreakdown {
    private double totalBudget;
    private double estimatedCabFare;
    private double estimatedFoodCost;
    private double estimatedActivityCost;
    private double estimatedRemainingBuffer;
    private String note;

    public BudgetBreakdown() {}

    public BudgetBreakdown(double totalBudget, double estimatedCabFare, double estimatedFoodCost,
                           double estimatedActivityCost, double estimatedRemainingBuffer, String note) {
        this.totalBudget = totalBudget;
        this.estimatedCabFare = estimatedCabFare;
        this.estimatedFoodCost = estimatedFoodCost;
        this.estimatedActivityCost = estimatedActivityCost;
        this.estimatedRemainingBuffer = estimatedRemainingBuffer;
        this.note = note != null ? note : "Approximate guidance based on selected budget tier; actual expenses vary.";
    }

    public double getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(double totalBudget) {
        this.totalBudget = totalBudget;
    }

    public double getEstimatedCabFare() {
        return estimatedCabFare;
    }

    public void setEstimatedCabFare(double estimatedCabFare) {
        this.estimatedCabFare = estimatedCabFare;
    }

    public double getEstimatedFoodCost() {
        return estimatedFoodCost;
    }

    public void setEstimatedFoodCost(double estimatedFoodCost) {
        this.estimatedFoodCost = estimatedFoodCost;
    }

    public double getEstimatedActivityCost() {
        return estimatedActivityCost;
    }

    public void setEstimatedActivityCost(double estimatedActivityCost) {
        this.estimatedActivityCost = estimatedActivityCost;
    }

    public double getEstimatedRemainingBuffer() {
        return estimatedRemainingBuffer;
    }

    public void setEstimatedRemainingBuffer(double estimatedRemainingBuffer) {
        this.estimatedRemainingBuffer = estimatedRemainingBuffer;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

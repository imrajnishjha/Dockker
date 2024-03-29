package com.example.blockchain;

public class AdminNocModel {
    int Status,Reject;
    public AdminNocModel() {
    }

    public AdminNocModel(int status, int reject) {
        Status = status;
        Reject = reject;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    public int getReject() {
        return Reject;
    }

    public void setReject(int reject) {
        Reject = reject;
    }
}

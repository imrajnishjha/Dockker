package com.example.blockchain;

public class AdminNocModel {
    int Status;
    public AdminNocModel() {
    }
    public AdminNocModel(int status) {
        Status = status;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }
}

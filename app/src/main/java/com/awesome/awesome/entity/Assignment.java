package com.awesome.awesome.entity;


import com.awesome.awesome.Status;
import com.awesome.awesome.Priority;
import java.io.Serializable;
import java.time.LocalDateTime;

public class Assignment implements Serializable {

    int ID;
    String name;
    LocalDateTime endDateTime;
    String subject;
    Status status;
    Priority priority;

    public Assignment(String name, LocalDateTime endDateTime, String subject) {
        this.name = name;
        this.endDateTime = endDateTime;
        this.subject = subject;
        status = Status.WAITING;
    }

    public Assignment(int ID, String name, LocalDateTime endDateTime, Status status, Priority priority, String subject) {
        this.ID = ID;
        this.name = name;
        this.endDateTime = endDateTime;
        this.status = status;
        this.priority = priority;
        this.subject = subject;
    }

    public int getID() {
        return ID;
    }

    public void setID(int newID) {
        this.ID = newID;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime newEndDateTime) {
        this.endDateTime = newEndDateTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status newStatus) {
        this.status = newStatus;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getSubject(){return subject;}

    public void setSubject(String subject){this.subject = subject;}

}

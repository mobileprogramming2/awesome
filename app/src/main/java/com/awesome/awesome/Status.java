package com.awesome.awesome;

public enum Status {
    WAITING,
    ONGOING,
    COMPLETE;

    public static Status intToStatus(int integer) {
        switch (integer) {
            case 0:
                return WAITING;
            case 1:
                return ONGOING;
            case 2:
                return COMPLETE;
        }
        return null;
    }

    public static int StatusToInt(Status status) {
        switch (status) {
            case WAITING:
                return 0;
            case ONGOING:
                return 1;
            case COMPLETE:
                return 2;
            default:
                return -1;
        }
    }
}

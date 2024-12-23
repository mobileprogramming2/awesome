package com.awesome.awesome;

public enum Priority {
    LOW, MEDIUM, HIGH;

    public static Priority intToPriority(int integer) {
        switch (integer) {
            case 0:
                return HIGH;
            case 1:
                return MEDIUM;
            case 2:
                return LOW;
        }
        return null;
    }

    public static int PriorityToInt(Priority priority) {
        switch (priority) {
            case HIGH:
                return 0;
            case MEDIUM:
                return 1;
            case LOW:
                return 2;
            default:
                return -1;
        }
    }
}

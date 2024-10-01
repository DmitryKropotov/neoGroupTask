package org.example.service;

import org.example.model.TimeStampModel;

import java.util.List;

public class GeneralMethods {
    public static boolean isListInAscendingOrder(List<TimeStampModel> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i).compareTo(list.get(i + 1)) > 0) {
                return false;
            }
        }
        return true;
    }
}

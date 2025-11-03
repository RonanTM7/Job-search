package com.example.job.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class FormatUtils {

    public static String formatSalary(String salary) {
        if (salary == null || salary.isEmpty() || salary.length() % 2 != 0) {
            return salary;
        }

        try {
            int middle = salary.length() / 2;
            String minSalaryStr = salary.substring(0, middle);
            String maxSalaryStr = salary.substring(middle);

            long minSalary = Long.parseLong(minSalaryStr);
            long maxSalary = Long.parseLong(maxSalaryStr);

            NumberFormat format = NumberFormat.getNumberInstance(new Locale("ru", "RU"));
            String formattedMin = format.format(minSalary);
            String formattedMax = format.format(maxSalary);

            return formattedMin + " - " + formattedMax + " ₽";
        } catch (NumberFormatException e) {
            return salary;
        }
    }
}

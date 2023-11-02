package org.aggregateframework.utils;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;

/**
 * @Author huabao.fang
 * @Date 2022/6/15 15:45
 **/
public class AggDateFormatUtils {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private AggDateFormatUtils(){
    }

    public static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return DateFormatUtils.format(date, DATE_FORMAT);
    }


}

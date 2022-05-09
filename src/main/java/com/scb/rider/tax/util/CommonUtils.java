package com.scb.rider.tax.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class CommonUtils {

    private static final Integer ROUND_PLACES = 2;
    
    private CommonUtils() {}

    @SuppressWarnings("unchecked")
    public static String toHash(String content) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = messageDigest.digest(content.getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        } catch(NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String toHex(byte[] orig) {
        StringBuilder result = new StringBuilder();

        for(int i = 0; i < orig.length; ++i) {
            String hx = Integer.toHexString(255 & orig[i]);
            if (hx.length() == 2) {
                result.append(hx);
            } else if (hx.length() != 1) {
                throw new RuntimeException("convert to Hex error");
            } else {
                result.append("0" + hx);
            }
        }
        return result.toString();
    }

    public static String getFormattedCurrentDate(String format) {
        return getFormattedDate(LocalDate.now(), format);
    }

    public static String getFormattedDate(LocalDate localDate, String format) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        return dtf.format(localDate);
    }

    public static String getFormattedCurrentDateTime(String format) {
        return getFormattedDateTime(LocalDateTime.now(), format);
    }

    public static String getFormattedDateTime(LocalDateTime localDateTime, String format) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        return dtf.format(localDateTime);
    }

    public static String getFormattedTime(LocalTime localTime, String format) {
        if(Objects.isNull(localTime)) {
            return StringUtils.EMPTY;
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        return dtf.format(localTime);
    }

    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)"+regex+"(?!.*?"+regex+")", replacement);
    }

    public static void downloadFile(HttpServletResponse response, byte[] bytes) throws IOException {
        response.setHeader("charset", StandardCharsets.UTF_8.name());
        response.setContentType("application/octet-stream");
        response.setContentLength(bytes.length);

        try(OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(bytes, 0, bytes.length);
            outputStream.flush();
            outputStream.close();
            response.flushBuffer();
        }
    }

    public static String sanitize(byte[] strBytes) {
        return new String(strBytes)
                .replace("\r", "")
                .replace("\n", "");
    }
    
    public static Double round(String value) {
        if (value == null)
            return null;
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(ROUND_PLACES, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static Double round(Double value) { 
    	return CommonUtils.round(String.valueOf(value));
    }
    
}

package com.scb.rider.tax.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class PropertyUtils {

    @Autowired
    MessageSource messageSource;

    public String getProperty(String key){
        if(StringUtils.isEmpty(key)) {
            return StringUtils.EMPTY;
        }
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, null, locale);
    }

    public String getLocalizedReason(String reason) {
        try {
           return getProperty(reason);
        } catch(Exception e) {
            return reason;
        }
    }
}

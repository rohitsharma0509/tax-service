package com.scb.rider.tax.util;

import com.scb.rider.tax.constants.ErrorConstants;
import org.apache.maven.surefire.shade.org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyUtilsTest {

    @InjectMocks
    private PropertyUtils propertyUtils;

    @Mock
    private MessageSource messageSource;

    @Test
    void getLocalizedReasonWhenReasonIsEmpty() {
        String result = propertyUtils.getLocalizedReason(StringUtils.EMPTY);
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void getLocalizedReasonWhenExceptionOccurred() {
        when(messageSource.getMessage(eq(ErrorConstants.SERVER_ERROR_EX_MSG), any(), any(Locale.class))).thenThrow(new NullPointerException());
        String result = propertyUtils.getLocalizedReason(ErrorConstants.SERVER_ERROR_EX_MSG);
        assertEquals(ErrorConstants.SERVER_ERROR_EX_MSG, result);
    }

    @Test
    void getLocalizedReasonWhenUnableToLocalized() {
        when(messageSource.getMessage(eq(ErrorConstants.SERVER_ERROR_EX_MSG), any(), any(Locale.class))).thenReturn(ErrorConstants.SERVER_ERROR_EX_MSG);
        String result = propertyUtils.getLocalizedReason(ErrorConstants.SERVER_ERROR_EX_MSG);
        assertEquals(ErrorConstants.SERVER_ERROR_EX_MSG, result);
    }
}

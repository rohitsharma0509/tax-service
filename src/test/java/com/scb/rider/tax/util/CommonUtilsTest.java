package com.scb.rider.tax.util;

import com.scb.rider.tax.constants.Constants;
import com.scb.rider.tax.model.enums.FileType;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class CommonUtilsTest {

    private static final String INVOICE_FILE_NAME = "rbh-rider-60261f0ae5d94525e6470625-210212143906.csv";
    private static final String CHECK_FILE_NAME = "rbh-rider-60261f0ae5d94525e6470625-210212143906.chk";

    @Test
    void shouldToHash() {
        String result = CommonUtils.toHash("test");
        assertNotNull(result);
    }

    @Test
    void shouldGetFormattedCurrentDate() {
        String result = CommonUtils.getFormattedCurrentDate(Constants.DATE_FORMAT_YYYYMMDD);
        assertNotNull(result);
    }

    @Test
    void shouldGetFormattedTimeForNull() {
        String result = CommonUtils.getFormattedTime(null, Constants.TIME_FORMAT);
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void shouldGetFormattedTime() {
        String result = CommonUtils.getFormattedTime(LocalTime.now(), Constants.TIME_FORMAT);
        assertNotNull(result);
    }

    @Test
    void getFormattedCurrentDateTime() {
        String result = CommonUtils.getFormattedCurrentDateTime(Constants.DATETIME_FORMAT_YYMMDDHHMMSS);
        assertNotNull(result);
    }

    @Test
    void shouldReplaceLast() {
        String result = CommonUtils.replaceLast(INVOICE_FILE_NAME, FileType.CSV.getExtension(), FileType.CHK.getExtension());
        assertEquals(CHECK_FILE_NAME, result);
    }

    @Test
    void shouldDownloadFile() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        CommonUtils.downloadFile(response, new byte[1]);
    }

    @Test
    void shouldSanitize() {
        String result = CommonUtils.sanitize("test".getBytes(StandardCharsets.UTF_8));
        assertNotNull(result);
    }

    @Test
    void shouldRoundString() {
        Double result = CommonUtils.round("10.0");
        assertNotNull(result);
    }

    @Test
    void shouldRoundDouble() {
        Double result = CommonUtils.round(10.0);
        assertNotNull(result);
    }
}

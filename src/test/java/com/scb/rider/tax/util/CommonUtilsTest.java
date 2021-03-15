package com.scb.rider.tax.util;

import com.scb.rider.tax.constants.Constants;
import com.scb.rider.tax.model.enums.FileType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

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
}

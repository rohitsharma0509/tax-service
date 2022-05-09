package com.scb.rider.tax.service;

import com.scb.rider.tax.exception.RecipientListEmptyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static com.scb.rider.tax.constants.ExcelHeaderValue.FILE_EXTENSION;
import static com.scb.rider.tax.constants.ExcelHeaderValue.FILE_PREFIX;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private static final String FROM_EMAIL_FIELD = "formEmail";
    private static final String TO_EMAIL_FIELD = "toEmail";
    private static final String FROM_EMAIL = "from@scb.com";
    private static final String TO_EMAIL = "to@scb.com";
    private static final String FILE_NAME = "temp.xls";
    private static final String SUBJECT = "test subject";
    private static final String TEXT = "test text";
    private static final int INVOKED_ONCE = 1;

    @InjectMocks
    private EmailService emailService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @Test
    void throwExceptionSendMailWithAttachmentWhenToEmailIsNull() {
        ReflectionTestUtils.setField(emailService, TO_EMAIL_FIELD, null);
        assertThrows(RecipientListEmptyException.class, () -> emailService.sendMailWithAttachment(FILE_NAME, new byte[1]));
        verify(mailSender, times(INVOKED_ONCE)).createMimeMessage();
    }

    @Test
    void shouldSendMailWithAttachment() throws MessagingException {
        ReflectionTestUtils.setField(emailService, TO_EMAIL_FIELD, TO_EMAIL);
        ReflectionTestUtils.setField(emailService, FROM_EMAIL_FIELD, FROM_EMAIL);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        emailService.sendMailWithAttachment(FILE_NAME, new byte[1]);
        verify(mailSender, times(INVOKED_ONCE)).send(any(MimeMessage.class));
    }

    @Test
    void shouldSendMail() {
        ReflectionTestUtils.setField(emailService, TO_EMAIL_FIELD, TO_EMAIL);
        ReflectionTestUtils.setField(emailService, FROM_EMAIL_FIELD, FROM_EMAIL);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        emailService.sendMail(SUBJECT, TEXT);
        verify(mailSender, times(INVOKED_ONCE)).send(any(MimeMessage.class));
    }

    @Test
    void shouldGetFileName() {
        String result = emailService.getFileName();
        assertTrue(result.startsWith(FILE_PREFIX));
        assertTrue(result.endsWith(FILE_EXTENSION));
    }

}

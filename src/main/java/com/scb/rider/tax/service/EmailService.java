package com.scb.rider.tax.service;

import com.scb.rider.tax.exception.RecipientListEmptyException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.scb.rider.tax.constants.ExcelHeaderValue.FILE_EXTENSION;
import static com.scb.rider.tax.constants.ExcelHeaderValue.FILE_PREFIX;

@Service("emailService")
@AllArgsConstructor
@NoArgsConstructor
@Log4j2
public class EmailService {
    @Value("${spring.mail.fromemail}")
    private String formEmail;

    @Value("${spring.mail.toemail}")
    private String toEmail;

    private static final String subject = "GL Report";
    private static final String template = "Please find attached GL";


    @Autowired
    private JavaMailSender mailSender;

    public void sendMailWithAttachment(String fileName, byte[] fileToAttach) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        log.info("Trying to send mail with attachment with File name {}",fileName);
        if(toEmail==null || toEmail.isEmpty()){
            log.info("Exception while Sending mail as no recipient is set");
            throw new RecipientListEmptyException("Exception while Sending mail as no recipient is set");
        }
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(formEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(template);
            helper.addAttachment(fileName, new ByteArrayResource(fileToAttach));
            mailSender.send(message);
        } catch (MessagingException e) {
            log.info("Exception while Sending mail {} ",e.getMessage());
            throw new MessagingException("Exception While Sending mail ");
        }
    }

    public String getFileName() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String formatDateTime = now.format(formatter);
        return FILE_PREFIX + formatDateTime + FILE_EXTENSION;
    }

}


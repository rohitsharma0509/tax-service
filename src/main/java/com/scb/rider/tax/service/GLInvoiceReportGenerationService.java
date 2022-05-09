package com.scb.rider.tax.service;

import com.scb.rider.tax.client.impl.ReconciliationServiceClient;
import com.scb.rider.tax.constants.ErrorConstants;
import com.scb.rider.tax.exception.ExternalServiceInvocationException;
import com.scb.rider.tax.exception.MatchedDataNotFoundException;
import com.scb.rider.tax.exception.RecipientListEmptyException;
import com.scb.rider.tax.model.document.GLInvoice;
import com.scb.rider.tax.model.dto.ExcelGLJobDetails;
import com.scb.rider.tax.model.dto.FinalPaymentReconciliationDetails;
import com.scb.rider.tax.model.enums.GLInvoiceStatus;
import com.scb.rider.tax.repository.GLInvoiceRepository;
import com.scb.rider.tax.service.impl.AmazonS3Service;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.List;

@Service
@Log4j2
public class GLInvoiceReportGenerationService {
    private static final String GL_INVOICE = "GL-Invoice";

    @Autowired
    private EmailService emailService;
    @Autowired
    private ReconciliationServiceClient reconciliationServiceClient;
    @Autowired
    private GLInvoiceRepository glInvoiceRepository;
    @Autowired
    private AmazonS3Service amazonS3Service;

    @Async("threadPoolTaskExecutor")
    public void generateInvoiceReport(GLInvoice glInvoice) {
        boolean isSuccess = Boolean.FALSE;
        String reason = StringUtils.EMPTY;

        try {
            glInvoice.setStartTime(LocalTime.now());
            glInvoice.setStatus(GLInvoiceStatus.IN_PROGRESS);
            glInvoice.setStartTime(LocalTime.now());
            glInvoiceRepository.save(glInvoice);


            List<FinalPaymentReconciliationDetails> finalDetails = reconciliationServiceClient.getFinalReconciliationDetailsByReconBatchId(glInvoice.getReconBatchId());
            byte[] bytes = ExcelGLJobDetails.generateGlReport(finalDetails);
            
            log.info("File Generated for GL and sending mail");
            String fileName= emailService.getFileName();
            try(InputStream inputStream = new ByteArrayInputStream(bytes)) {
                String s3FileName = amazonS3Service.uploadInputStream(inputStream, GL_INVOICE, glInvoice.getS1BatchId(), fileName);
                glInvoice.setFileName(s3FileName);
            } catch (Exception e){
                reason = ErrorConstants.AWS_S3_EX_MSG;
            }
            emailService.sendMailWithAttachment(fileName, bytes);
            isSuccess = true;
        } catch (ExternalServiceInvocationException | MatchedDataNotFoundException e) {
            log.error("exception while getting GL invoice data. Reason {}", e.getMessage());
            reason = e.getMessage();
        } catch (MessagingException e) {
            log.error("exception while sending mail", e);
            reason = ErrorConstants.MESSAGING_ERROR_MSG;
        } catch (RecipientListEmptyException e) {
            log.error("Recipient list is empty", e);
            reason = ErrorConstants.RECIPIENT_LIST_EMPTY_ERROR_MSG;
        } catch (Exception e) {
            log.error("Exception occurred while generating GL invoice", e);
            reason = ErrorConstants.SERVER_ERROR_EX_MSG;
        } finally {
            glInvoice.setReason(reason);
            glInvoice.setStatus(isSuccess ? GLInvoiceStatus.SUCCESS : GLInvoiceStatus.FAILED);
            glInvoice.setEndTime(LocalTime.now());
            glInvoiceRepository.save(glInvoice);
        }
    }
}

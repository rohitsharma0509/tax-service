package com.scb.rider.tax.repository;



import com.scb.rider.tax.model.document.TaxInvoice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface TaxInvoiceRepository extends MongoRepository<TaxInvoice, String> {
    Optional<TaxInvoice> findByInvoiceNumber(String invoiceNumber);
    Optional<TaxInvoice> findByReconBatchId(String reconBatchId);
}

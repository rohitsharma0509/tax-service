package com.scb.rider.tax.repository;

import com.scb.rider.tax.model.document.GLInvoice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GLInvoiceRepository extends MongoRepository<GLInvoice, String> {

    List<GLInvoice> findByReconBatchIdOrS1BatchId(String reconBatchId, String s1batchId);

    Optional<GLInvoice> findByS1BatchId(String s1BatchId);
}

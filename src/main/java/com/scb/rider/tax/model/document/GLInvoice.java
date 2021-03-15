package com.scb.rider.tax.model.document;

import com.fasterxml.jackson.annotation.JsonView;
import com.scb.rider.tax.model.enums.GLInvoiceStatus;
import com.scb.rider.tax.view.View;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "gl_invoice")
public class GLInvoice extends BaseEntity implements Serializable {
    @Id
    @JsonView(value = {View.GLInvoiceView.GET.class})
    private String id;
    @JsonView(value = {View.GLInvoiceView.GET.class, View.GLInvoiceView.POST.class})
    private String reconBatchId;
    @JsonView(value = {View.GLInvoiceView.GET.class, View.GLInvoiceView.POST.class})
    @NotBlank
    @Indexed(name = "glInvoiceS1BatchId",unique = true, sparse = true)
    private String s1BatchId;
    private String fileName;
    @JsonView(value = {View.GLInvoiceView.GET.class})
    private GLInvoiceStatus status;
    @JsonView(value = {View.GLInvoiceView.GET.class})
    private LocalTime startTime;
    @JsonView(value = {View.GLInvoiceView.GET.class})
    private LocalTime endTime;
    @JsonView(value = {View.GLInvoiceView.GET.class})
    private String reason;
}

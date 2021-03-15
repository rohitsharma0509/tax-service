package com.scb.rider.tax.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponseDto {
    private Integer totalPages;
    private Long totalCount;
    private Integer currentPage;
    private List<TaxInvoiceDto> content;

    public static SearchResponseDto of(List<TaxInvoiceDto> content, int totalPages, long totalCount, int currentPageNumber) {
        return SearchResponseDto.builder()
                .content(content).totalPages(totalPages)
                .totalCount(totalCount).currentPage(currentPageNumber)
                .build();

    }
}


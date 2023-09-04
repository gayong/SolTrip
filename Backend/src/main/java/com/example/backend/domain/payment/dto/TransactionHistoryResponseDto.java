package com.example.backend.domain.payment.dto;

import com.example.backend.domain.payment.PaymentType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder(toBuilder = true)
public class TransactionHistoryResponseDto {
    private String content;
    private Long amount;
    private String storeName;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime transactionTime;
    private PaymentType paymentType;
}

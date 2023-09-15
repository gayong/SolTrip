package com.example.backend.domain.analyze.service;

import com.example.backend.domain.account.Account;
import com.example.backend.domain.account.exception.UserNotFoundException;
import com.example.backend.domain.account.repository.AccountRepository;
import com.example.backend.domain.analyze.dto.AnalyzeRequestDto;
import com.example.backend.domain.analyze.dto.AnalyzeResponseDto;
import com.example.backend.domain.budget.entity.Budget;
import com.example.backend.domain.budget.repository.BudgetRepository;
import com.example.backend.domain.common.exception.ResourceNotFoundException;
import com.example.backend.domain.payment.Payment;
import com.example.backend.domain.payment.repository.PaymentRepository;
import com.example.backend.domain.plan.entity.Plan;
import com.example.backend.domain.plan.repository.PlanRepository;
import com.example.backend.domain.portfolio.dto.KakaoPlaceSearchResponseDto;
import com.example.backend.domain.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyzeService {
    private final PortfolioService portfolioService;
    private final PlanRepository planRepository;
    private final BudgetRepository budgetRepository;
    private final AccountRepository accountRepository;
    private final PaymentRepository paymentRepository;


    /**
     * 1. 거래 내역 조회를 받아온다.
     * 2. 여행 1일차부터 오늘 일차까지 거래 내역조회를 반복한다.
     * 3. 카카오 API를 통해 상호명으로 해당 내역의 카테고리를 찾아내고 퍼센트 비율을 계산해준다
     * 음식점
     * 교통,수송
     * 스포츠,레저
     * 여행
     * 의료,건강
     * 기타
     * 4. 마지막 오늘 일차일 경우 오늘 총 사용 금액과 예산 비율을 계산해준다.
     */
    public AnalyzeResponseDto analyzeGet(AnalyzeRequestDto analyzeRequestDto, String userNumber, Long planId) {
        Map<String, Integer> category = new HashMap<>();

        category.put("음식점", 0);
        category.put("교통,수송", 0);
        category.put("스포츠,레저", 0);
        category.put("여행", 0);
        category.put("의료,건강", 0);
        category.put("기타", 0);

        int amountUsed = 0; // 오늘 일차 사용 금액
        int budget_cost = 0; // 오늘 예산 값
        int total_cost = 0; // 전체 예산 값

        // 2. plan id를 통해서 해당 여행 시작 일자를 구한다.
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new ResourceNotFoundException("Plan", planId));

        //시작 날짜와 오늘 날짜 차이 구함
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        long day = ChronoUnit.DAYS.between(plan.getStartDate(), LocalDate.parse(analyzeRequestDto.getDataBody().getTodayDate(), formatter));

        //실제 결제 내역 조회
        Account account = accountRepository.findAccountByUserNumber(userNumber)
                .orElseThrow(UserNotFoundException::new);

        List<Payment> paymentList = paymentRepository.findByAccountId(account.getId());

        for (int i = 0; i <= day; i++) {
            amountUsed = 0; //마지막 날만 저장
            // payment 결제 내역으로 상호명 검색
            for (Payment payment : paymentList) {
                // 결제 날짜가 같고 해당 결제가 지출인 경우
                if (payment.getTransactionDate().equals(plan.getStartDate().plusDays(i))) {
                    // 거래내역조회 내용(상호명) 카카오 api로 정보 찾기
                    Mono<KakaoPlaceSearchResponseDto> location = portfolioService.findLocation(payment.getStoreName());
                    KakaoPlaceSearchResponseDto responseDto = location.block(); // Mono의 결과를 동기적으로 가져옴

                    String extractedCategory = "";
                    if (responseDto != null && !responseDto.getDocuments().isEmpty()) {
                        String categoryName = responseDto.getDocuments().get(0).getCategory_name();
                        extractedCategory = categoryName.split(">")[0].trim();
                    }

                    if (category.containsKey(extractedCategory)) {
                        category.put(extractedCategory, (int) (category.get(extractedCategory) + payment.getAmount()));
                    } else { // 기타 상승
                        category.put("기타", (int) (category.get("기타") + payment.getAmount()));
                    }

                    //오늘 일차일 경우 총 사용 금액 계산.
                    amountUsed += payment.getAmount();
                }

            }

            List<Budget> budgets = budgetRepository.findAllByPlanAndTravelDate(plan, plan.getStartDate().plusDays(i));
            // 오늘 일차 예상 예산 총 값 구히기(비율 구할 때 사용)

            //마지막 값만 저장
            budget_cost = 0;

            for (Budget budget : budgets) {
                budget_cost += budget.getAmount();
                total_cost += budget.getAmount();
            }
        }

        // 4. 오늘 일차 사용 예산 퍼센트
        long totalBudget = 100L;

        if (budget_cost != 0) {
            totalBudget = (amountUsed * 100L) / budget_cost;
        }


        LocalDate startDate = plan.getStartDate();
        LocalDate endDate = LocalDate.parse(analyzeRequestDto.getDataBody().getTodayDate(), formatter);

        // 카테고리 퍼센트 비율 구하기
        AnalyzeResponseDto.DataBody dataBody = AnalyzeResponseDto.DataBody.builder()
                .day(ChronoUnit.DAYS.between(startDate, endDate))
                .totalBudget(totalBudget)
                .amountUsed(amountUsed)
                .meal((long) ((double) category.get("음식점") / total_cost * 100.0))
                .traffic((long) ((double) category.get("교통,수송") / total_cost * 100.0))
                .sports((long) ((double) category.get("스포츠,레저") / total_cost * 100.0))
                .travel((long) ((double) category.get("여행") / total_cost * 100.0))
                .medical((long) ((double) category.get("의료,건강") / total_cost * 100.0))
                .etc((long) ((double) category.get("기타") / total_cost * 100.0))
                .build();

        return AnalyzeResponseDto.builder()
                .dataBody(dataBody)
                .build();
    }
}
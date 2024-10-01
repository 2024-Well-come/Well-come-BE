package com.wellcome.WellcomeBE.global;

import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import com.wellcome.WellcomeBE.global.type.DayOfWeekType;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class OpeningHoursUtils {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");

    // 요일 및 운영 시간 정보 가져오기
    public static OpenStatus getOpenStatus(PlaceReviewResponse.PlaceResult placeResult) {
        DayOfWeekType today = DayOfWeekType.fromIndex(LocalDate.now().getDayOfWeek().getValue() % 7); // 요일 인덱스 조정
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Seoul")).withSecond(0).withNano(0); // 한국 표준시(KST)를 기준으로 현재 시간 가져오기
        String openDetail = "정보 없음";
        Boolean isOpen = false;
//        log.info("한국시간: {}", now);

        if (placeResult.getOpening_hours() != null) {
            openDetail = getOpenDetail(placeResult.getOpening_hours().getWeekday_text(), today);
            isOpen = isCurrentlyOpen(placeResult.getOpening_hours().getPeriods(), today, now,openDetail);
        }

        return new OpenStatus(openDetail, isOpen);
    }

    // 운영 시간 상세 정보 가져오기
    public static String getOpenDetail(List<String> weekdayText, DayOfWeekType today) {
        for (String text : weekdayText) {
            if (text.startsWith(today.getKoreanName())) {
                return text;
            }
        }
        return "정보 없음";
    }

    // 현재 운영 중인지 확인하기
    public static Boolean isCurrentlyOpen(List<PlaceReviewResponse.PlaceResult.OpeningHours.Period> periods, DayOfWeekType today, LocalTime now,String openDetail) {
        int todayIndex = today.getIndex(); // 요일 인덱스

        if (periods == null || periods.isEmpty()) {
            return false; // 운영 시간이 없는 경우 닫혀 있는 것으로 간주
        }


        for (PlaceReviewResponse.PlaceResult.OpeningHours.Period period : periods) {
            int periodDayIndex = period.getOpen().getDay();

            // 24시간 운영 여부 확인
            if (period.getClose() == null && period.getOpen().getTime().equals("0000")) {
                // openDetail에 "24시간 영업"이라는 문구가 포함되어 있는지 확인
                if (openDetail != null && openDetail.contains("24시간 영업")) {
                    // 요일별 24시간 운영 여부 확인
                    String[] weekdayTexts = openDetail.split("\n"); // openDetail에서 줄 바꿈으로 요일 텍스트 분리
                    for (String text : weekdayTexts) {
                        if (text.startsWith(DayOfWeekType.fromIndex(todayIndex).getKoreanName())) {
                            return true; // 오늘이 24시간 영업인 경우
                        }
                    }
                }
            }

            // todayIndex와 period의 day가 같은지 비교
            if (periodDayIndex == todayIndex) {
                LocalTime openTime = LocalTime.parse(period.getOpen().getTime(), TIME_FORMATTER);
                LocalTime closeTime = (period.getClose() != null)
                        ? LocalTime.parse(period.getClose().getTime(), TIME_FORMATTER)
                        : LocalTime.MAX; // 23:59로 설정하여 당일 자정까지로 간주

//                log.info("시작: {}", openTime);
//                log.info("끝: {}", closeTime);

                // 현재 시간이 openTime과 closeTime 사이에 있는지 확인
                if (!now.isBefore(openTime) && !now.isAfter(closeTime)) {
                    return true;
                }
            }
        }
        return false;
    }

    // 운영 시간 상태 정보를 담을 클래스
    public static class OpenStatus {
        private final String openDetail;
        private final Boolean isOpen;

        public OpenStatus(String openDetail, Boolean isOpen) {
            this.openDetail = openDetail;
            this.isOpen = isOpen;
        }

        public String getOpenDetail() {
            return openDetail;
        }

        public Boolean getIsOpen() {
            return isOpen;
        }
    }
}

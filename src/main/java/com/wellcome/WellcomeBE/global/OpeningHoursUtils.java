package com.wellcome.WellcomeBE.global;

import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class OpeningHoursUtils {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");
    private static final Map<String, String> WEEKDAY_MAP = new HashMap<>() {{
        put("MONDAY", "월요일");
        put("TUESDAY", "화요일");
        put("WEDNESDAY", "수요일");
        put("THURSDAY", "목요일");
        put("FRIDAY", "금요일");
        put("SATURDAY", "토요일");
        put("SUNDAY", "일요일");
    }};

    // 요일 및 운영 시간 정보 가져오기
    public static OpenStatus getOpenStatus(PlaceReviewResponse.PlaceResult placeResult) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Seoul")).withSecond(0).withNano(0); // 한국 표준시(KST)를 기준으로 현재 시간 가져오기
        String openDetail = "정보 없음";
        Boolean isOpen = false;

        if (placeResult.getOpening_hours() != null) {
            openDetail = getOpenDetail(placeResult.getOpening_hours().getWeekday_text(), today);
            isOpen = isCurrentlyOpen(placeResult.getOpening_hours().getPeriods(), today, now);
        }

        return new OpenStatus(openDetail, isOpen);
    }

    // 운영 시간 상세 정보 가져오기
    public static String getOpenDetail(List<String> weekdayText, DayOfWeek today) {
        String todayString = today.name();
        for (String text : weekdayText) {
            if (text.startsWith(WEEKDAY_MAP.get(todayString))) {
                return text;
            }
        }
        return "정보 없음";
    }

    // 현재 운영 중인지 확인하기
    public static Boolean isCurrentlyOpen(List<PlaceReviewResponse.PlaceResult.OpeningHours.Period> periods, DayOfWeek today, LocalTime now) {
        int todayIndex = today.getValue(); // DayOfWeek는 월요일이 1, 일요일이 7

        if (periods == null || periods.isEmpty()) {
            return false; // 운영 시간이 없는 경우 닫혀 있는 것으로 간주
        }


        for (PlaceReviewResponse.PlaceResult.OpeningHours.Period period : periods) {
            // 구글의 day는 0(일요일)부터 시작하므로 DayOfWeek의 index와 맞추기 위해 수정
            int periodDayIndex = period.getOpen().getDay() + 1; // open의 day 값은 0부터 시작함

            // todayIndex와 period의 day가 같은지 비교
            if (periodDayIndex == todayIndex) {
                LocalTime openTime = LocalTime.parse(period.getOpen().getTime(), TIME_FORMATTER);

                // period.getClose()가 null인 경우 24시간 영업으로 간주
                LocalTime closeTime = (period.getClose() != null)
                        ? LocalTime.parse(period.getClose().getTime(), TIME_FORMATTER)
                        : LocalTime.MAX; // 23:59로 설정하여 당일 자정까지로 간주

                log.info("시작: {}",openTime);
                log.info("끝: {}",closeTime);

                // 현재 시간이 openTime과 closeTime 사이에 있는지 확인
                if (!now.isBefore(openTime) && !now.isAfter(closeTime)) {
                    return true;
                }
            }
        }
        log.info("한국시간: {}",now);
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

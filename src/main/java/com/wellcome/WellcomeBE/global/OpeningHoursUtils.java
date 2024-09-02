package com.wellcome.WellcomeBE.global;

import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        LocalTime now = LocalTime.now();
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
        int todayIndex = today.getValue(); // 월요일이 1, 일요일이 7

        if (periods == null || periods.isEmpty()) {
            return false; // 운영 시간이 없는 경우 닫혀 있는 것으로 간주
        }

        for (PlaceReviewResponse.PlaceResult.OpeningHours.Period period : periods) {
            if (period.getOpen().getDay() == todayIndex - 1) { // JSON의 day는 0부터 시작하므로 1을 빼야 함
                LocalTime openTime = LocalTime.parse(period.getOpen().getTime(), TIME_FORMATTER);
                LocalTime closeTime = LocalTime.parse(period.getClose().getTime(), TIME_FORMATTER);

                if (now.isAfter(openTime) && now.isBefore(closeTime)) {
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

package com.wellcome.WellcomeBE.global;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeatherUtils {

    // 발표 날짜
    public static String getReleaseDate(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        if(currentDateTime.getHour() == 0 && currentDateTime.getMinute() < 45){ //00:45 이전
            return LocalDate.now().minusDays(1).format(formatter);
        }else{
            return LocalDate.now().format(formatter);
        }
    }

    // 발표 시각
    public static String getReleaseTime(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        int currentHour = currentDateTime.getHour();
        int currentMinute = currentDateTime.getMinute();

        if(currentMinute < 45){
            currentHour -= 1;
        }

        return String.format("%02d30", currentHour); //HH30 형태로 반환
    }
}

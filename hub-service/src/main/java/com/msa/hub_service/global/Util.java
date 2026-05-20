package com.msa.hub_service.global;

import java.math.BigDecimal;

public class Util {
    public static class DistanceCalculator {

        // 지구의 평균 반지름 (단위: km)
        private static final double EARTH_RADIUS = 6371.0;

        // 위치에 따른 거리 계산
        public static double getDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {

            if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
                throw new IllegalArgumentException("위도와 경도 값은 null일 수 없습니다.");
            }

            // Math 삼각함수 사용을 위해 double 타입으로 변환
            double lat1Double = lat1.doubleValue();
            double lon1Double = lon1.doubleValue();
            double lat2Double = lat2.doubleValue();
            double lon2Double = lon2.doubleValue();

            // 위도와 경도를 라디안(Radian) 단위로 변환
            double dLat = Math.toRadians(lat2Double - lat1Double);
            double dLon = Math.toRadians(lon2Double - lon1Double);

            double radLat1 = Math.toRadians(lat1Double);
            double radLat2 = Math.toRadians(lat2Double);


            double a = Math.pow(Math.sin(dLat / 2), 2) +
                    Math.pow(Math.sin(dLon / 2), 2) *
                            Math.cos(radLat1) * Math.cos(radLat2);

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            // 결과값 리턴 (km 단위)
            return EARTH_RADIUS * c;
        }
    }
}

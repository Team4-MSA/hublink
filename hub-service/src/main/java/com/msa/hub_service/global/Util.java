package com.msa.hub_service.global;

import com.msa.core_common.error.exception.CustomException;
import com.msa.hub_service.entity.RouteInfo;
import com.msa.hub_service.entity.RouteType;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Util {

    private Util() {
        throw new IllegalStateException("Utility class는 객체로 생성할 수 없습니다.");
    }

    public static class DistanceCalculator {

        // 지구의 평균 반지름 (단위: km)
        private static final double EARTH_RADIUS = 6371.0;

        // 위치에 따른 거리 계산
        public static double getDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {

            if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
                throw new CustomException(HubErrorCode.NULL_COORDINATES);
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

    public static class RouteCalculator {
        private static final double AVERAGE_TRUCK_SPEED_KMH = 60.0;
        private static final double ROAD_CURVATURE_WEIGHT = 1.3;
        private static final double H2H_DISTANCE_THRESHOLD_KM = 200.0;

        public static RouteInfo calculate(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
            // 위경도 상 거리
            double straightDistance = Util.DistanceCalculator.getDistance(lat1, lon1, lat2, lon2);

            // 실제 거리
            double actualDistance = actualDistance(straightDistance);
            BigDecimal distanceKm = BigDecimal.valueOf(actualDistance).setScale(2, RoundingMode.HALF_UP);

            // 주행 시간
            int durationMin = calculateDuration(actualDistance);

            // 거리에 따른 루트 타입
            RouteType type = determineRouteType(actualDistance);

            return new RouteInfo(distanceKm, durationMin, type);
        }

        public static RouteType determineRouteType(double km) {
            return (km > H2H_DISTANCE_THRESHOLD_KM) ? RouteType.H2H : RouteType.P2P;
        }

        public static Integer calculateDuration(double km) {
            return (int) Math.round((km / AVERAGE_TRUCK_SPEED_KMH) * 60.0);
        }

        public static double actualDistance(double straightDistance) {
            return straightDistance * ROAD_CURVATURE_WEIGHT;
        }
    }
}

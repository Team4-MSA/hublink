package com.msa.hub_service.client;

import com.msa.hub_service.dto.CoordinateDto;

public interface AddressGeocodingPort {
    // 도로명 주소 받아 위도/경도 반환
    CoordinateDto getCoordinate(String address);
}

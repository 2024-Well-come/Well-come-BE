package com.wellcome.WellcomeBE.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Area {

    SSEOUL ("서울", 1),
    INCHEON("인천", 2),
    DAEJEON ("대전", 3),
    DAEGU ("대구", 4),
    GWANGJU ("광주", 5),
    BUSAN ("부산", 6),
    ULSAN ("울산", 7),
    SEJONG ("세종특별자치시", 8),
    GYEONGGI("경기도", 31),
    GANGWON ("강원특별자치도", 32),
    CHUNGCHEONGBUKDO ("충청북도", 33),
    CHUNGCHEONGNAMDO ("충청남도", 34),
    GYEONGSANGBUKDO ("경상북도", 35),
    GYEONGSANGNAMDO ("경상남도", 36),
    JEOLLABUKDO ("전북특별자치도", 37),
    JEOLLANAMDO ("전라남도", 38),
    JEJU ("제주도", 39);

    private final String name;
    private final int code;

}

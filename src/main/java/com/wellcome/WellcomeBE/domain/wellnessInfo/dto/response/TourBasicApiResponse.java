package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.global.type.Area;
import com.wellcome.WellcomeBE.global.type.Category;
import com.wellcome.WellcomeBE.global.type.Sigungu;
import com.wellcome.WellcomeBE.global.type.Thema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.util.List;

import static com.wellcome.WellcomeBE.global.type.Area.getAreaType;
import static com.wellcome.WellcomeBE.global.type.Category.getCategoryType;
import static com.wellcome.WellcomeBE.global.type.Sigungu.getSigunguType;
import static com.wellcome.WellcomeBE.global.type.Thema.getThemaType;

@Data
@Slf4j
public class TourBasicApiResponse {

    private Response response;

    @Data
    public static class Response {
        private Header header;
        private Body body;

        @Data
        public static class Header {
            private String resultCode;
            private String resultMsg;
        }

        @Data
        public static class Body {
            @JsonDeserialize(using = ItemsDeserializer.class)
            private Items items;

            private int numOfRows;
            private int pageNo;
            private int totalCount;

            @Data
            public static class Items {
                private List<Item> item;

                @Data
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class Item {
                    private String addr1;
                    private String addr2;
                    private String areacode;
                    private String booktour;
                    private String cat1;
                    private String cat2;
                    private String cat3;
                    private String contentid;
                    private String contenttypeid;
                    private String createdtime;
                    private String firstimage;
                    private String firstimage2;
                    private String cpyrhtDivCd;
                    private String mapx;
                    private String mapy;
                    private String mlevel;
                    private String modifiedtime;
                    private String sigungucode;
                    private String tel;
                    private String title;

                    public WellnessInfo toEntity(String s3ThumbnailUrl) throws ParseException {

                        // WKTReader를 통해 WKT -> 실제 타입으로 변환
                        String pointWKT = String.format("POINT(%s %s)", mapx, mapy);
                        Point point = (Point) new WKTReader().read(pointWKT);

                        // ENUM
                        Area area = areacode != null ? getAreaType(Integer.parseInt(areacode)) : getAreaType(100);
                        Sigungu sigungu = sigungucode != null ? getSigunguType(Integer.parseInt(sigungucode)): getSigunguType(100);
                        Category category = getCategoryType(Integer.parseInt(contenttypeid));

                        // 테마
                        Thema thema = getThemaType(cat1, cat2, cat3);

                        WellnessInfo wellnessSpot = WellnessInfo.builder()
                                .title(title)
                                .contentId(contentid)
                                .originalThumbnailUrl(firstimage2)
                                .thumbnailUrl(s3ThumbnailUrl)
                                .tel(tel)
                                .area(area)
                                .sigungu(sigungu)
                                .thema(thema)
                                .category(category)
                                .address(addr1)
                                .mapX(Double.parseDouble(mapx))
                                .mapY(Double.parseDouble(mapy))
                                .location(point)
                                .build();

                        return wellnessSpot;
                    }
                }
            }
        }
    }
}

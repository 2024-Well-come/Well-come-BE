package com.wellcome.WellcomeBE.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.*;

@Slf4j
public class TourApiErrorHandler {

    // XML 에러 처리
    public String handleXmlErrorResponse(String xmlResponse) {
        try {
            // XML 파서를 위한 DocumentBuilderFactory 생성
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // XML 문서를 Document 객체로 파싱
            Document document = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes()));

            // XPath 객체 생성
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            // XPath 표현식 정의
            XPathExpression expr = xpath.compile("/OpenAPI_ServiceResponse/cmmMsgHeader/returnReasonCode");

            // XPath 표현식을 사용하여 태그 값 추출
            Node returnReasonCodeNode = (Node) expr.evaluate(document, XPathConstants.NODE);

            // 추출된 노드의 값 반환, Custom Exception 처리
            String reasonCode = returnReasonCodeNode != null ? returnReasonCodeNode.getTextContent() : null;
            if(reasonCode != null){
                return getErrorMessage(reasonCode);
            }else{
                log.error("XML 파싱 중 오류 발생: returnReasonCode 태그를 찾을 수 없습니다.");
                throw new CustomException(TOUR_API_XML_PARSING_ERROR);
            }
        } catch (Exception e){
            log.error("XML 파싱 중 오류 발생: ", e.getMessage());
            throw new CustomException(TOUR_API_XML_PARSING_ERROR);
        }
    }

    // 에러 코드 <-> 에러 메세지 매핑
    public String getErrorMessage(String reasonCode){
        switch (reasonCode){
            case "01":
                return "애플리케이션 에러가 발생했습니다.";
            case "02":
                return "데이터베이스 에러가 발생했습니다.";
            case "03":
                return "데이터가 없습니다.";
            case "04":
                return "HTTP 에러가 발생했습니다.";
            case "05":
                return "서비스 연결 실패 에러가 발생했습니다.";
            case "10":
                return "요청 파라미터가 잘못되었습니다.";
            case "11":
                return "필수 요청 파라미터가 누락되었습니다.";
            case "12":
                return "해당 오픈 API 서비스가 없거나 폐기되었습니다.";
            case "20":
                return "서비스 접근이 거부되었습니다.";
            case "21":
                return "일시적으로 사용할 수 없는 서비스 키입니다.";
            case "22":
                return "서비스 요청 제한 횟수를 초과했습니다.";
            case "30":
                return "등록되지 않은 서비스 키입니다.";
            case "31":
                return "활용 기간이 만료되었습니다.";
            case "32":
                return "등록되지 않은 IP 입니다.";
            case "33":
                return "서명되지 않은 호출입니다.";
            case "99":
                return "기타 에러입니다.";
        }
        return "";
        //throw new CustomException(CustomErrorCode.TOUR_API_ERROR, errorMessage);
    }
}

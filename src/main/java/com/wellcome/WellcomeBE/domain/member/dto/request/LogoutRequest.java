package com.wellcome.WellcomeBE.domain.member.dto.request;

import lombok.Getter;

@Getter
public class LogoutRequest {
    private String refreshToken;
}

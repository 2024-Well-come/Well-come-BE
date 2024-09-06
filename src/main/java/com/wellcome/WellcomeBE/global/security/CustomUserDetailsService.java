package com.wellcome.WellcomeBE.global.security;

import com.wellcome.WellcomeBE.domain.member.repository.MemberRepository;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.MEMBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberRepository.findByKakaoId(Long.valueOf(username))
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
    }

}

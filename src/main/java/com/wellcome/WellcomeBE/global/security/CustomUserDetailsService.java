package com.wellcome.WellcomeBE.global.security;

import com.wellcome.WellcomeBE.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberRepository.findByKakaoId(Long.valueOf(username))
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
                // TODO Custom Exception 처리
    }

}

package com.ureka.techpost.domain.auth.jwt;

import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.user.entity.User;
import com.ureka.techpost.domain.user.enums.Role;
import com.ureka.techpost.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // Authorization 헤더가 아예 없는 경우: 필터는 토큰 검증을 건너뛰고 그대로 다음 체인으로 진행해야 한다.
    @Test
    void noAuthorizationHeader_continuesChain() throws ServletException, IOException {
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtUtil, userRepository);
    }

    // Authorization 헤더는 있으나 Bearer 타입이 아닌 경우: 토큰으로 간주하지 않고 필터를 통과시켜야 한다.
    @Test
    void nonBearerHeader_continuesChain() throws ServletException, IOException {
        request.addHeader("Authorization", "Token abc");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtUtil, userRepository);
    }

    // 만료된 토큰: 만료 검증에서 걸러지고 401 JSON 응답을 내려야 하며, 이후 체인으로 넘어가면 안 된다.
    @Test
    void expiredToken_returns401AndStopsChain() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer expired");
        when(jwtUtil.isExpired("expired")).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        // 디버그용: 응답 상태/본문 출력
        System.out.println("expiredToken status=" + response.getStatus() + " body=" + response.getContentAsString());

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("만료"));
    }

    // 카테고리가 access가 아닌 토큰: 잘못된 토큰 유형으로 판단하여 401을 내려야 하고 체인 진행을 중단해야 한다.
    @Test
    void invalidCategory_returns401AndStopsChain() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer badcat");
        when(jwtUtil.isExpired("badcat")).thenReturn(false);
        when(jwtUtil.getCategory("badcat")).thenReturn("refresh");

        filter.doFilterInternal(request, response, filterChain);

        // 디버그용: 응답 상태/본문 출력
        System.out.println("invalidCategory status=" + response.getStatus() + " body=" + response.getContentAsString());

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("카테고리"));
    }

    // 토큰의 username으로 조회했지만 DB에 사용자가 없을 때: 401 응답을 주고 SecurityContext에 인증이 설정되지 않아야 한다.
    @Test
    void userNotFound_returns401AndStopsChain() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer good");
        when(jwtUtil.isExpired("good")).thenReturn(false);
        when(jwtUtil.getCategory("good")).thenReturn("access");
        when(jwtUtil.getUsernameFromToken("good")).thenReturn("nouser");
        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        // 디버그용: 응답 상태/본문 출력
        System.out.println("userNotFound status=" + response.getStatus() + " body=" + response.getContentAsString());

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // 토큰 파싱 단계에서 예외가 발생할 때: 예외 메시지를 콘솔에 찍어보고, 예외가 그대로 전파되는지 확인한다.
    @Test
    void malformedToken_returns401AndPrintsResponse() throws Exception {
        request.addHeader("Authorization", "Bearer bad");
        when(jwtUtil.isExpired("bad")).thenReturn(false);
        when(jwtUtil.getCategory("bad")).thenReturn("access");
        when(jwtUtil.getUsernameFromToken("bad"))
                .thenThrow(new IllegalArgumentException("토큰 파싱 실패"));

        filter.doFilterInternal(request, response, filterChain);

        // 🔍 여기서 response 확인
        System.out.println("status = " + response.getStatus());
        System.out.println("body = " + response.getContentAsString());

        // ✅ 실제 검증은 assert로
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("유효하지 않은 토큰"));
        verify(filterChain, never()).doFilter(any(), any());
    }

    // 정상 토큰 흐름: 만료/카테고리/사용자 조회를 통과하면 인증 객체가 생성되어 컨텍스트에 저장되고 체인도 계속 진행된다.
    @Test
    void validToken_setsAuthenticationAndContinuesChain() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer good");
        when(jwtUtil.isExpired("good")).thenReturn(false);
        when(jwtUtil.getCategory("good")).thenReturn("access");
        when(jwtUtil.getUsernameFromToken("good")).thenReturn("user1");

        User user = User.builder()
                .userId(1L)
                .username("user1")
                .password("pass")
                .name("User One")
                .role(Role.ROLE_USER)
                .provider("NONE")
                .providerId(null)
                .build();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("user1", authentication.getName());
        assertTrue(authentication.getPrincipal() instanceof CustomUserDetails);
    }
}

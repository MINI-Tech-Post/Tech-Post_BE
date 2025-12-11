/**
 * @file StompChannelInterceptor.java
 @author 이재, 강승우
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 WebSocket 사용시 사용자 인증하는 클래스입니다.
 */

package com.ureka.techpost.global.config.websocket;

import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.auth.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// STOMP jwt 인증 처리
// STOMP에서 클라이언트가 연결 요청시 JWT의 유효성을 검증하는 역할
// 웹소켓으로 채팅 서버에 접속하려는 클라이언트가 보내는 연결메시지 (CONNECT)를 가로채서 메시지에 포함된 JWT 토큰이 유효한지 확인하는 보안설정 코드
@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

    // application.yaml에 정의된 jwt.secretKey 값을 가져와 필드에 주입
    @Value("${jwt.secret}")
    private String secretKey;

    private SecretKey getSigningKey() {
      return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    private final CustomUserDetailsService userDetailsService;

    // connect, subscribe, disconnet 하기 전에 preSend()를 무조건 거친다
    // 클라이언트로부터 메시지가 채널로 전송되기 직전에 이 메서드 호출됨
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if(StompCommand.CONNECT == accessor.getCommand()) {
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            String token = bearerToken.substring(7);

            // 토큰 검증
            Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String username = claims.getSubject();

            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            accessor.setUser(authentication);
            accessor.getSessionAttributes().put("user", userDetails);

        }

        // 사용자가 채팅방의 참여자인지 검증
        if(StompCommand.SUBSCRIBE == accessor.getCommand()) {
            System.out.println("subscribe 검증");
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            String token = bearerToken.substring(7);

            Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String email = claims.getSubject();
            String roomId = accessor.getDestination().split("/")[2];
        }
        return message;
    }



}

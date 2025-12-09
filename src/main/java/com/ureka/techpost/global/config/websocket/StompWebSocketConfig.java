/**
 * @file StompWebSocketConfig.java
 @author 이재, 강승우
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 WebSocket 사용 설정 클래스입니다.
 */

package com.ureka.techpost.global.config.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// STOMP 사용해 메시지 브로커 설정
@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker // STOMP 전용
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final StompChannelInterceptor stompChannelInterceptor;

    // WebSocket 엔드포인트 등록 : 클라이언트가 연결할 수 있는 WebSocket 엔드포인트 정의
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 WebSocket에 연결하기 위한 엔드포인트를 "/connect"로 설정
        registry.addEndpoint("/connect")
                // 클라이언트의 origin을 명시적으로 지정
                 .setAllowedOrigins("http://localhost:3000")
                // WebSocket을 지원하지 않는 브라우저에서도 SockJS를 통해 WebSocket 기능 사용 가능하도록
                .withSockJS();
    }

    // 메시지 브로커 구성 : 클라 - 서버 간의 메시지 라우팅 관리
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 발행 (publish) : /publish/1 형태로 메시지 발행해야 함을 설정
        // /publish로 시작하는 url 패턴으로 메시지 발행되면 @Controller 객체의 @MessageMapping 메서드로 라우팅
        registry.setApplicationDestinationPrefixes("/publish");

        // 수신 (subscribe) : /topic/1 형태로 메시지 수신해야 함을 설정
        registry.enableSimpleBroker("/topic");
    }

    // 웹소켓 요청 (Connect, subscribe, disconnect) 등의 요청 시에는 http reader 등 http 메시지를 넣어올 수 있고,
    // 이를 interceptor를 통해 가로채 토큰 등을 검증할 수 있음
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompChannelInterceptor);
    }
}

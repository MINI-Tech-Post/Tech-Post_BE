/**
 * @file StompEventLister.java
 @author 이재, 강승우
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 WebSocket 세션 관리 클래스입니다.
 */

package com.ureka.techpost.global.config.websocket;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

// connection 객체 관리
// 실시간 서버에서 문제 => 연결 객체 많아져서 서버 과부화되는 것 => 적절한 제거 필요함

// 스프링과 STOMP는 세션 관리를 자동 (내부적)으로 처리
// 연결 / 해제 이벤트 기록, 연결된 세션 수를 실시간으로 확인할 목적으로 EventListener 생성
// 로그 & 디버깅 목적
@Component
public class StompEventListener {
    private final Set<String> sessions = ConcurrentHashMap.newKeySet();

    @EventListener
    public void connectHandle(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.add(accessor.getSessionId()); // 세션 생성

        System.out.println("connect session ID " + accessor.getSessionId());
        System.out.println("total session : " + sessions.size());
    }

    @EventListener
    public void disconnectHandle(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.remove(accessor.getSessionId()); // 세션 삭제

        System.out.println("disconnect session ID " + accessor.getSessionId());
        System.out.println("total session : " + sessions.size());
    }
}

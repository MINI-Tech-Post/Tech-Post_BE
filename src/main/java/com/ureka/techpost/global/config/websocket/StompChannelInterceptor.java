//package com.ureka.techpost.global.config.websocket;
//
//import com.goojakgyo.goojakgyo.chat.service.ChatService;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.security.authentication.AuthenticationServiceException;
//import org.springframework.stereotype.Component;
//
//// STOMP jwt 인증 처리
//// STOMP에서 클라이언트가 연결 요청시 JWT의 유효성을 검증하는 역할
//// 웹소켓으로 채팅 서버에 접속하려는 클라이언트가 보내는 연결메시지 (CONNECT)를 가로채서 메시지에 포함된 JWT 토큰이 유효한지 확인하는 보안설정 코드
//@Component
//public class StompChannelInterceptor implements ChannelInterceptor {
//
//    // application.yaml에 정의된 jwt.secretKey 값을 가져와 필드에 주입
//    @Value("${jwt.secretKey}")
//    private String secretKey;
//
//    private final ChatService chatService;
//
//    public StompChannelInterceptor(ChatService chatService) {
//        this.chatService = chatService;
//    }
//
//    // connect, subscribe, disconnet 하기 전에 preSend()를 무조건 거친다
//    // 클라이언트로부터 메시지가 채널로 전송되기 직전에 이 메서드 호출됨
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//
//        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//
//        if(StompCommand.CONNECT == accessor.getCommand()) {
//            System.out.println("connect 요청 시 토큰 유효성 검증");
//
//            String bearerToken = accessor.getFirstNativeHeader("Authorization");
//
//            // Bearer [토큰 값] 형태이므로 substring을 이용해 순수 토큰 값만 남기기
//            String token = bearerToken.substring(7);
//
//            // 토큰 검증
//            Claims claims = Jwts.parserBuilder()
//                    .setSigningKey(secretKey)
//                    .build()
//                    .parseClaimsJws(token)
//                    .getBody();
//
//            System.out.println("토큰 검증 완료");
//        }
//
//        // 사용자가 채팅방의 참여자인지 검증
//        if(StompCommand.SUBSCRIBE == accessor.getCommand()) {
//            System.out.println("subscribe 검증");
//            String bearerToken = accessor.getFirstNativeHeader("Authorization");
//            String token = bearerToken.substring(7);
//
//            Claims claims = Jwts.parserBuilder()
//                    .setSigningKey(secretKey)
//                    .build()
//                    .parseClaimsJws(token)
//                    .getBody();
//
//            String email = claims.getSubject();
//            String roomId = accessor.getDestination().split("/")[2];
//            if(!chatService.isRoomParticipant(email, Long.parseLong(roomId))) {
//                throw new AuthenticationServiceException("해당 room에 권한이 없습니다.");
//            }
//        }
//        return message;
//    }
//
//
//
//}

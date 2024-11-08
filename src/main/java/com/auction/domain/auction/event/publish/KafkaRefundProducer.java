//package com.auction.domain.auction.event.publish;
//
//import com.auction.common.apipayload.status.ErrorStatus;
//import com.auction.common.exception.ApiException;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class KafkaRefundProducer {
//
//    private final KafkaTemplate<String, Object> kafkaTemplate;
//    private final ObjectMapper objectMapper;
//
//    public void produceRefundTopic(Object object) {
//        try {
//            kafkaTemplate.send("refund-point-topic", objectMapper.writeValueAsString(object));
//            log.info("Publish :::::  {} ::::::::::", "refund");
//        } catch (JsonProcessingException e) {
//            throw new ApiException(ErrorStatus._INVALID_REQUEST);
//        }
//    }
//}

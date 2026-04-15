package com.example.cancellation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class CancellationRefundRuleTest {

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("환불 금액이 원결제 금액을 초과하면 400을 반환한다")
    void 환불금액이_원결제초과시_400() throws Exception {
        // sale-1: amount = 50,000 → 60,000 환불 요청 → 거절
        mockMvc.perform(post("/cancellation-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleRecordId": "sale-1",
                                  "refundAmount": 60000,
                                  "canceledAt": "2025-03-10T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("누적 환불 금액이 원결제 금액을 초과합니다")));
    }

    @Test
    @DisplayName("부분 취소를 여러 번 등록할 수 있다")
    void 부분취소_여러번_등록_가능() throws Exception {
        // sale-1: amount = 50,000
        // 1차 부분 환불 20,000 → 성공
        mockMvc.perform(post("/cancellation-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleRecordId": "sale-1",
                                  "refundAmount": 20000,
                                  "canceledAt": "2025-03-10T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated());

        // 2차 부분 환불 30,000 → 누적 50,000 = 원결제 → 성공
        mockMvc.perform(post("/cancellation-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleRecordId": "sale-1",
                                  "refundAmount": 30000,
                                  "canceledAt": "2025-03-11T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("누적 환불액이 원결제 금액을 초과하면 400을 반환한다")
    void 누적환불액_원결제초과시_400() throws Exception {
        // sale-1: amount = 50,000
        // 1차 부분 환불 30,000 → 성공
        mockMvc.perform(post("/cancellation-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleRecordId": "sale-1",
                                  "refundAmount": 30000,
                                  "canceledAt": "2025-03-10T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated());

        // 2차 환불 30,000 → 누적 60,000 > 50,000 → 거절
        mockMvc.perform(post("/cancellation-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleRecordId": "sale-1",
                                  "refundAmount": 30000,
                                  "canceledAt": "2025-03-11T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("누적 환불 금액이 원결제 금액을 초과합니다")));
    }

    @Test
    @DisplayName("이미 전액 환불된 판매 건에 추가 취소를 시도하면 400을 반환한다")
    void 전액환불_후_추가취소시_400() throws Exception {
        // sale-3: amount = 80,000, cancel-1(80,000 전액환불) 이미 존재
        // 추가로 1원이라도 환불 시도 → 거절
        mockMvc.perform(post("/cancellation-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleRecordId": "sale-3",
                                  "refundAmount": 1,
                                  "canceledAt": "2025-03-26T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("누적 환불 금액이 원결제 금액을 초과합니다")));
    }
}

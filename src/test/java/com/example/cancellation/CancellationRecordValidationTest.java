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
class CancellationRecordValidationTest {

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("refundAmount가 음수이면 400을 반환한다")
    void refundAmount_음수이면_400() throws Exception {
        mockMvc.perform(post("/cancellation-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleRecordId": "sale-1",
                                  "refundAmount": -1000,
                                  "canceledAt": "2025-03-10T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("canceledAt이 null이면 400을 반환한다")
    void canceledAt_null이면_400() throws Exception {
        mockMvc.perform(post("/cancellation-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleRecordId": "sale-1",
                                  "refundAmount": 50000,
                                  "canceledAt": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("saleRecordId가 빈 문자열이면 400을 반환한다")
    void saleRecordId_빈문자열이면_400() throws Exception {
        mockMvc.perform(post("/cancellation-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleRecordId": "",
                                  "refundAmount": 50000,
                                  "canceledAt": "2025-03-10T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}

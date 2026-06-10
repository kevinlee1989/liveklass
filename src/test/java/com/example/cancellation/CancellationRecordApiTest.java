package com.example.cancellation;

import com.example.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CancellationRecordApiTest extends BaseIntegrationTest {

    @Test
    @DisplayName("취소 내역을 정상적으로 등록하면 201과 id를 반환한다")
    void 취소_내역_정상_등록() throws Exception {
        String request = """
                {
                  "saleRecordId": "sale-1",
                  "refundAmount": 50000,
                  "canceledAt": "2025-03-10T10:00:00+09:00"
                }
                """;

        mockMvc.perform(post("/cancellation-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("부분 환불도 정상 등록된다")
    void 부분_환불_정상_등록() throws Exception {
        String request = """
                {
                  "saleRecordId": "sale-4",
                  "refundAmount": 30000,
                  "canceledAt": "2025-03-28T10:00:00+09:00"
                }
                """;

        mockMvc.perform(post("/cancellation-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("존재하지 않는 saleRecordId로 등록하면 400을 반환한다")
    void 존재하지_않는_saleRecordId_등록시_400() throws Exception {
        String request = """
                {
                  "saleRecordId": "sale-999",
                  "refundAmount": 50000,
                  "canceledAt": "2025-03-10T10:00:00+09:00"
                }
                """;

        mockMvc.perform(post("/cancellation-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("존재하지 않는 판매 내역입니다: sale-999"));
    }
}

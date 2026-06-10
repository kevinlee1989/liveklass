package com.example.creator;

import com.example.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;

class CreatorApiTest extends BaseIntegrationTest {

    @Test
    @DisplayName("전체 크리에이터 목록 조회 시 등록된 크리에이터를 모두 반환한다")
    void 전체_크리에이터_목록_조회() throws Exception {
        // DataInitializer: creator-1(김강사), creator-2(이강사), creator-3(박강사)
        mockMvc.perform(get("/creators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[*].id", hasItems("creator-1", "creator-2", "creator-3")))
                .andExpect(jsonPath("$[*].name", hasItems("김강사", "이강사", "박강사")));
    }

    @Test
    @DisplayName("크리에이터 추가 후 목록 조회 시 추가된 크리에이터가 포함된다")
    void 크리에이터_추가_후_목록_조회() throws Exception {
        mockMvc.perform(post("/creators")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "id": "creator-new", "name": "최강사" }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/creators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[*].id", hasItems("creator-new")))
                .andExpect(jsonPath("$[*].name", hasItems("최강사")));
    }

    @Test
    @DisplayName("목록 조회 응답은 id와 name 필드를 포함한다")
    void 목록_조회_응답_필드_확인() throws Exception {
        mockMvc.perform(get("/creators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists());
    }
}

package it.ute.QAUTE.controller;

import it.ute.QAUTE.api.FastAPIClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Controller
@RequestMapping("/consultant/events")
public class SuggestEvent {

    @Autowired
    private FastAPIClient fastApiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @PostMapping("/suggest-from-text")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> suggestEventDetails(@RequestBody String eventText) {
        if (eventText == null || eventText.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nội dung không được để trống"));
        }

        String aiJsonResponse = null;
        String jsonToParse = null;

        try {

            String prompt = "Trích xuất các thông tin sau từ mô tả sự kiện: " +
                    "Tiêu đề (title), " +
                    "Mô tả (description), " +
                    // Thêm 'type' với các giá trị enum
                    "Loại sự kiện (type - một trong: Workshop, Consultation, Seminar, GroupSession), " +
                    "Thời gian bắt đầu (startTime - định dạng YYYY-MM-DDTHH:MM), " +
                    "Thời gian kết thúc (endTime - định dạng YYYY-MM-DDTHH:MM), " +
                    "Địa điểm (location), " +
                    // Thêm 'meetingLink'
                    "Link họp online (meetingLink), " +
                    // Thêm 'maxParticipants'
                    "Số lượng tham gia tối đa (maxParticipants - là một con số), " +
                    "Hình thức (mode - một trong: Online, Offline, Hybrid), " +
                    "Tên Khoa (departmentName - ví dụ: 'Công nghệ thông tin'), " +
                    "Tên Lĩnh vực (fieldName - ví dụ: 'Học thuật') " +
                    "từ mô tả sự kiện sau. " +
                    "Chỉ trả về một đối tượng JSON duy nhất chứa các trường này. " +
                    "Nếu không tìm thấy thông tin nào, để giá trị là null.\n\nMô tả sự kiện:\n\"" +
                    eventText + "\"\n\nJSON Output:";


            aiJsonResponse = fastApiClient.chatBlocking(prompt, java.time.Duration.ofSeconds(60)); // Tăng timeout nếu cần
            log.info("AI Response (Raw): {}", aiJsonResponse);

            if (aiJsonResponse.contains("Xin lỗi, hệ thống đang bận")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", aiJsonResponse));
            }

            Pattern jsonPattern = Pattern.compile("```json\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL);
            Matcher matcher = jsonPattern.matcher(aiJsonResponse);

            if (matcher.find()) {
                jsonToParse = matcher.group(1).trim();
                log.info("Extracted JSON from ```json block: {}", jsonToParse);
            } else {
                int firstBrace = aiJsonResponse.indexOf('{');
                int lastBrace = aiJsonResponse.lastIndexOf('}');
                if (firstBrace != -1 && lastBrace > firstBrace) {
                    jsonToParse = aiJsonResponse.substring(firstBrace, lastBrace + 1).trim();
                    log.warn("Could not find ```json block. Fallback to first {{...}} block: {}", jsonToParse);
                } else {
                    throw new JsonProcessingException("AI response does not contain a valid JSON block."){};
                }
            }
            Map<String, Object> suggestions = objectMapper.readValue(jsonToParse, Map.class);
            return ResponseEntity.ok(suggestions);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse extracted JSON. Error: {}. Raw AI Response: [{}]. Extracted Part: [{}]",
                    e.getMessage(), aiJsonResponse, jsonToParse);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Lỗi xử lý phản hồi từ AI. Phản hồi không phải JSON hợp lệ.",
                    "rawResponse", aiJsonResponse
            ));
        } catch (Exception e) {
            log.error("Cannot suggest event details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Không thể gợi ý thông tin từ AI: " + e.getMessage()));
        }
    }
}
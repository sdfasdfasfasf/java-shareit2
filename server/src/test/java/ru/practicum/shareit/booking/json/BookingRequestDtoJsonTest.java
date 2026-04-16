package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingRequestDtoJsonTest {

    @Autowired
    private JacksonTester<BookingRequestDto> json;

    @Test
    void testSerialize() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingRequestDto dto = new BookingRequestDto(1L, start, end);
        JsonContent<BookingRequestDto> result = json.write(dto);
        assertThat(result).hasJsonPathNumberValue("@.itemId");
        assertThat(result).hasJsonPathStringValue("@.start");
        assertThat(result).hasJsonPathStringValue("@.end");
        assertThat(result).extractingJsonPathNumberValue("@.itemId").isEqualTo(1);
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"itemId\":1,\"start\":\"2025-01-01T12:00:00\",\"end\":\"2025-01-02T12:00:00\"}";
        BookingRequestDto dto = json.parse(content).getObject();
        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2025, 1, 2, 12, 0));
    }
}
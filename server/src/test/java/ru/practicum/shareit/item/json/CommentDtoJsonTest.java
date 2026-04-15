package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void testSerialize() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        CommentDto dto = CommentDto.builder().id(1L).text("Nice!").authorName("John").created(now).build();
        var result = json.write(dto);
        assertThat(result).hasJsonPathNumberValue("@.id");
        assertThat(result).hasJsonPathStringValue("@.text");
        assertThat(result).hasJsonPathStringValue("@.authorName");
        assertThat(result).hasJsonPathStringValue("@.created");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":1,\"text\":\"Nice!\",\"authorName\":\"John\",\"created\":\"2025-01-01T12:00:00\"}";
        CommentDto dto = json.parse(content).getObject();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("Nice!");
        assertThat(dto.getAuthorName()).isEqualTo("John");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0));
    }
}
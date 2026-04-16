package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void testSerialize() throws Exception {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        JsonContent<UserDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("@.id");
        assertThat(result).hasJsonPathStringValue("@.name");
        assertThat(result).hasJsonPathStringValue("@.email");
        assertThat(result).extractingJsonPathNumberValue("@.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("@.name").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("@.email").isEqualTo("john@example.com");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":1,\"name\":\"John Doe\",\"email\":\"john@example.com\"}";
        UserDto dto = json.parse(content).getObject();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("John Doe");
        assertThat(dto.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testDeserializeWithMissingFields() throws Exception {
        String content = "{\"name\":\"Only Name\"}";
        UserDto dto = json.parse(content).getObject();

        assertThat(dto.getId()).isNull();
        assertThat(dto.getName()).isEqualTo("Only Name");
        assertThat(dto.getEmail()).isNull();
    }
}
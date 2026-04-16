package ru.practicum.shareit.user.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void create_shouldSaveUser() {
        UserDto dto = UserDto.builder().name("John").email("john@test.com").build();
        UserDto saved = userService.create(dto);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("John");
        assertThat(saved.getEmail()).isEqualTo("john@test.com");
    }

    @Test
    void create_shouldThrowConflict_whenEmailNotUnique() {
        UserDto dto1 = UserDto.builder().name("John").email("john@test.com").build();
        userService.create(dto1);
        UserDto dto2 = UserDto.builder().name("John2").email("john@test.com").build();
        assertThatThrownBy(() -> userService.create(dto2)).isInstanceOf(ConflictException.class);
    }

    @Test
    void update_shouldUpdateUser() {
        UserDto created = userService.create(UserDto.builder().name("John").email("john@test.com").build());
        UserDto updated = userService.update(created.getId(), UserDto.builder().name("John Updated").build());
        assertThat(updated.getName()).isEqualTo("John Updated");
        assertThat(updated.getEmail()).isEqualTo("john@test.com");
    }

    @Test
    void getById_shouldThrowNotFound_whenUserNotExists() {
        assertThatThrownBy(() -> userService.getById(999L)).isInstanceOf(NotFoundException.class);
    }
}
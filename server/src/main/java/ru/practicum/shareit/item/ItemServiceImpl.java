package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Booking.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        User owner = getUserOrThrow(ownerId);
        Item item = ItemMapper.toEntity(itemDto);
        item.setOwner(owner);
        if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Request not found with id=" + itemDto.getRequestId()));
            item.setRequest(request);
            log.info("Linked item to request {}", request.getId());
        }
        Item saved = itemRepository.save(item);
        log.info("Created item id={} for owner id={}", saved.getId(), ownerId);
        return ItemMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        Item item = getItemOrThrow(itemId);
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("User is not the owner of item");
        }
        if (itemDto.getName() != null) item.setName(itemDto.getName());
        if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());
        Item updated = itemRepository.save(item);
        return ItemMapper.toDto(updated);
    }

    @Override
    public ItemResponseDto getById(Long userId, Long itemId) {
        Item item = getItemOrThrow(itemId);
        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());

        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;
        if (item.getOwner().getId().equals(userId)) {
            lastBooking = getLastBooking(itemId);
            nextBooking = getNextBooking(itemId);
        }

        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(comments)
                .build();
    }

    @Override
    public List<ItemResponseDto> getAllByOwner(Long ownerId) {
        getUserOrThrow(ownerId);
        return itemRepository.findByOwnerId(ownerId).stream()
                .map(item -> ItemResponseDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .description(item.getDescription())
                        .available(item.getAvailable())
                        .lastBooking(getLastBooking(item.getId()))
                        .nextBooking(getNextBooking(item.getId()))
                        .comments(commentRepository.findByItemId(item.getId()).stream()
                                .map(CommentMapper::toDto)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) return Collections.emptyList();
        return itemRepository.search(text).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);

        boolean hasCompleted = bookingRepository.findCompletedBooking(
                userId, itemId, BookingStatus.APPROVED, LocalDateTime.now().plusSeconds(1)
        ).isPresent();

        if (!hasCompleted) {
            log.warn("No completed booking for user {} item {}", userId, itemId);
            throw new BadRequestException("User has not completed booking of this item");
        }

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id=" + userId));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with id=" + itemId));
    }

    private BookingShortDto getLastBooking(Long itemId) {
        List<Booking> bookings = bookingRepository.findByItemIdAndStatusAndStartBefore(
                itemId, BookingStatus.APPROVED, LocalDateTime.now(), Sort.by(Sort.Direction.DESC, "start"));
        if (bookings.isEmpty()) return null;
        Booking b = bookings.get(0);
        return BookingShortDto.builder().id(b.getId()).bookerId(b.getBooker().getId()).start(b.getStart()).end(b.getEnd()).build();
    }

    private BookingShortDto getNextBooking(Long itemId) {
        List<Booking> bookings = bookingRepository.findByItemIdAndStatusAndStartAfter(
                itemId, BookingStatus.APPROVED, LocalDateTime.now(), Sort.by(Sort.Direction.ASC, "start"));
        if (bookings.isEmpty()) return null;
        Booking b = bookings.get(0);
        return BookingShortDto.builder().id(b.getId()).bookerId(b.getBooker().getId()).start(b.getStart()).end(b.getEnd()).build();
    }
}
package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.error.exeptions.DuplicatedDataException;
import ru.practicum.ewm.error.exeptions.NotFoundException;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(NewUserRequest newUserRequest) {
        log.info("Процесс создание нового пользователя: " + newUserRequest);
        log.trace("Проверка на дубликат email: " + newUserRequest.getEmail());
        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            log.warn("Пользователь не был создан: Пользователь с таким email уже существует");
            throw new DuplicatedDataException("Пользователь с данным email уже сущесвует");
        }
        log.trace("Проверка пройдена!");
        log.info("Пользоваетль успешно создан!");
        User user = userRepository.save(UserMapper.toUser(newUserRequest));
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> get(List<Long> ids, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from, size, Sort.by("id").ascending());
        if (ids == null || ids.isEmpty()) {
            log.info("Получаем список всех пользователей");
            return userRepository.findAll(pageRequest).getContent().stream()
                    .map(UserMapper::toUserDto)
                    .toList();
        }
        log.info("Получаем список пользователей c id: " + ids);
        return userRepository.findByIdIn(ids, pageRequest).getContent().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Процесс удаления пользоваетля с id = " + id);
        log.trace("Проверка существование пользователя");
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            log.warn("Пользователь не был удален: пользователь с указанным id не существует");
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        log.info("Пользователь успешно удален!");
        userRepository.deleteById(id);
    }
}

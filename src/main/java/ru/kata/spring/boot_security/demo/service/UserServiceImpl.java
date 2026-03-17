package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.repository.UserRepository;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        Iterable<User> users = userRepository.findAll();
        List<User> userList = new ArrayList<>();
        users.forEach(userList::add);
        return userList;
    }

    @Override
    public User getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    @Override
    public void saveOrUpdateUser(Long id, String firstName, String lastName, String email,
                                 int age, String password, List<String> roles) {
        User user;

        if (id != null) {
            // РЕДАКТИРОВАНИЕ существующего пользователя
            user = getUserById(id);
            if (user != null) {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setEmail(email);
                user.setAge(age);

                // Обновляем пароль только если ввели новый
                if (password != null && !password.isEmpty()) {
                    user.setPassword(passwordEncoder.encode(password));
                }

                // ВАЖНО: обновляем роли ТОЛЬКО если они были выбраны в форме
                // (roles не равен null и не пустой)
                if (roles != null && !roles.isEmpty()) {
                    Set<Role> userRoles = new LinkedHashSet<>();
                    for (String roleName : roles) {
                        Role role = roleRepository.findByName(roleName);
                        if (role != null) {
                            userRoles.add(role);
                        }
                    }
                    user.setRoles(userRoles);
                }
                // Если roles == null или пустой - роли НЕ МЕНЯЕМ, оставляем старые

            } else {
                // Странная ситуация: id есть, но пользователь не найден
                user = new User(firstName, lastName, email, age, passwordEncoder.encode(password));
                setUserRoles(user, roles); // для нового пользователя всегда устанавливаем роли
            }
        } else {
            // СОЗДАНИЕ нового пользователя
            user = new User(firstName, lastName, email, age, passwordEncoder.encode(password));
            setUserRoles(user, roles); // для нового пользователя всегда устанавливаем роли
        }

        userRepository.save(user);
    }

    // Вспомогательный метод для установки ролей (используется только для НОВЫХ пользователей)
    private void setUserRoles(User user, List<String> roles) {
        Set<Role> userRoles = new LinkedHashSet<>();
        if (roles != null) {
            for (String roleName : roles) {
                Role role = roleRepository.findByName(roleName);
                if (role != null) {
                    userRoles.add(role);
                }
            }
        }
        user.setRoles(userRoles);
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
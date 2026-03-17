package ru.kata.spring.boot_security.demo.controller;

import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin";
    }

    @GetMapping("/admin/users")
    public String showUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin";
    }

    @GetMapping("/admin/addUser")
    public String showAddForm(Model model) {
        model.addAttribute("user", new User());
        List<String> allRoles = List.of("ROLE_USER", "ROLE_ADMIN");
        model.addAttribute("allRoles", allRoles);
        return "user-form";
    }

    @PostMapping("/admin/saveUser")
    public String saveUser(@RequestParam(value = "id", required = false) Long id,
                           @RequestParam("firstName") String firstName,
                           @RequestParam("lastName") String lastName,
                           @RequestParam("email") String email,
                           @RequestParam("age") int age,
                           @RequestParam(value = "password", required = false) String password,
                           @RequestParam(value = "roleIds", required = false) List<String> roles) {

        userService.saveOrUpdateUser(id, firstName, lastName, email, age, password, roles);
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/editUser")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        List<String> allRoles = List.of("ROLE_USER", "ROLE_ADMIN");
        model.addAttribute("allRoles", allRoles);
        return "user-form";
    }

    @GetMapping("/admin/deleteUser")
    public String deleteUser(@RequestParam("id") Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/user")
    public String userPage(Model model, Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        model.addAttribute("user", user);
        return "user";
    }
}
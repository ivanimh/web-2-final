package com.example.productcrud.controller;

import com.example.productcrud.dto.ChangePasswordRequest;
import com.example.productcrud.dto.UpdateProfileRequest;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Helper ambil user dari session
    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    // ── VIEW PROFILE ──
    @GetMapping
    public String viewProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        model.addAttribute("user", user);
        return "profile/view";
    }

    // ── FORM EDIT PROFILE ──
    @GetMapping("/edit")
    public String showEditForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFullName(user.getFullName());
        req.setEmail(user.getEmail());
        req.setPhoneNumber(user.getPhoneNumber());
        req.setAddress(user.getAddress());
        req.setBio(user.getBio());
        req.setProfileImageUrl(user.getProfileImageUrl());

        model.addAttribute("profileRequest", req);
        return "profile/edit";
    }

    // ── SIMPAN EDIT PROFILE ──
    @PostMapping("/edit")
    public String saveProfile(@ModelAttribute UpdateProfileRequest profileRequest,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);

        user.setFullName(profileRequest.getFullName() != null
                ? profileRequest.getFullName().trim() : null);
        user.setEmail(profileRequest.getEmail() != null
                ? profileRequest.getEmail().trim() : null);
        user.setPhoneNumber(profileRequest.getPhoneNumber() != null
                ? profileRequest.getPhoneNumber().trim() : null);
        user.setAddress(profileRequest.getAddress() != null
                ? profileRequest.getAddress().trim() : null);
        user.setBio(profileRequest.getBio() != null
                ? profileRequest.getBio().trim() : null);
        user.setProfileImageUrl(profileRequest.getProfileImageUrl() != null
                ? profileRequest.getProfileImageUrl().trim() : null);

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Profil berhasil diperbarui!");
        return "redirect:/profile";
    }

    // ── FORM CHANGE PASSWORD ──
    @GetMapping("/change-password.html")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        return "profile/change-password.html";
    }

    // ── PROSES CHANGE PASSWORD ──
    @PostMapping("/change-password.html")
    public String processChangePassword(@ModelAttribute ChangePasswordRequest req,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);

        // Validasi: password lama harus cocok
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password lama tidak sesuai!");
            return "redirect:/profile/change-password.html";
        }

        // Validasi: password baru tidak boleh kosong
        if (req.getNewPassword() == null || req.getNewPassword().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password baru tidak boleh kosong!");
            return "redirect:/profile/change-password.html";
        }

        // Validasi: konfirmasi harus sama
        if (!req.getNewPassword().equals(req.getConfirmNewPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Password baru dan konfirmasi tidak cocok!");
            return "redirect:/profile/change-password.html";
        }

        // Encode dan simpan
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage",
                "Password berhasil diubah! Silakan login ulang.");
        return "redirect:/profile";
    }
}
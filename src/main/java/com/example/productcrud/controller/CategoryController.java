package com.example.productcrud.controller;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import com.example.productcrud.service.CategoryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public CategoryController(CategoryService categoryService, UserRepository userRepository) {
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    // LIST
    @GetMapping
    public String listCategories(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = getCurrentUser(userDetails);
        model.addAttribute("categories", categoryService.findAllByOwner(currentUser));
        return "category/list";
    }

    // FORM TAMBAH
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("isEdit", false);
        return "category/form";
    }

    // FORM EDIT
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(userDetails);
        return categoryService.findByIdAndOwner(id, currentUser)
                .map(category -> {
                    model.addAttribute("category", category);
                    model.addAttribute("isEdit", true);
                    return "category/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Kategori tidak ditemukan.");
                    return "redirect:/categories";
                });
    }

    // SIMPAN (Tambah & Edit)
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute Category category,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(userDetails);

        // Validasi nama tidak boleh kosong
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nama kategori tidak boleh kosong.");
            return "redirect:/categories/new";
        }

        // Jika edit, pastikan milik user ini
        if (category.getId() != null) {
            boolean isOwner = categoryService.findByIdAndOwner(category.getId(), currentUser).isPresent();
            if (!isOwner) {
                redirectAttributes.addFlashAttribute("errorMessage", "Kategori tidak ditemukan.");
                return "redirect:/categories";
            }
        }

        // Cek duplikat nama (untuk kategori baru saja)
        if (category.getId() == null && categoryService.existsByNameAndOwner(category.getName().trim(), currentUser)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Nama kategori '" + category.getName() + "' sudah ada.");
            return "redirect:/categories/new";
        }

        category.setName(category.getName().trim());
        category.setOwner(currentUser);
        categoryService.save(category);

        redirectAttributes.addFlashAttribute("successMessage", "Kategori berhasil disimpan!");
        return "redirect:/categories";
    }

    // HAPUS
    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(userDetails);
        boolean isOwner = categoryService.findByIdAndOwner(id, currentUser).isPresent();

        if (isOwner) {
            categoryService.deleteByIdAndOwner(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Kategori berhasil dihapus!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Kategori tidak ditemukan.");
        }

        return "redirect:/categories";
    }
}
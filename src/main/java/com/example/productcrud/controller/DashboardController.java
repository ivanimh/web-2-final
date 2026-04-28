package com.example.productcrud.controller;

import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.ProductRepository;
import com.example.productcrud.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.text.NumberFormat;
import java.util.*;
import java.util.Locale;


@Controller
public class DashboardController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public DashboardController(ProductRepository productRepository,
                               UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = getCurrentUser(userDetails);


        long totalProduk = productRepository.countByOwner(currentUser);
        long totalNilaiInventory = productRepository.sumInventoryValue(currentUser);


        NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
        String totalNilaiFormatted = rupiahFormat.format(totalNilaiInventory);


        long produkAktif = productRepository.countByOwnerAndActiveTrue(currentUser);
        long produkTidakAktif = productRepository.countByOwnerAndActiveFalse(currentUser);


        List<Object[]> rawKategori = productRepository.countByCategory(currentUser);
        Map<String, Long> produkPerKategori = new LinkedHashMap<>();
        for (Object[] row : rawKategori) {
            produkPerKategori.put((String) row[0], (Long) row[1]);
        }


        List<Product> lowStockProducts = productRepository.findByOwnerAndStockLessThan(currentUser, 5);


        model.addAttribute("username", currentUser.getUsername());
        model.addAttribute("totalProduk", totalProduk);
        model.addAttribute("totalNilaiInventory", totalNilaiFormatted);
        model.addAttribute("produkAktif", produkAktif);
        model.addAttribute("produkTidakAktif", produkTidakAktif);
        model.addAttribute("produkPerKategori", produkPerKategori);
        model.addAttribute("lowStockProducts", lowStockProducts);

        return "dashboard";
    }
}

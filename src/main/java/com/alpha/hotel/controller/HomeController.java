package com.alpha.hotel.controller;

import com.alpha.hotel.service.ChambreService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ChambreService chambreService;

    public HomeController(ChambreService chambreService) {
        this.chambreService = chambreService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("chambres", chambreService.getChambresDisponibles());
        return "home";
    }
}

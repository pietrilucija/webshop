package hr.algebra.webshop.controller.mvc;

import hr.algebra.webshop.service.CategoryService;
import hr.algebra.webshop.service.LoginHistoryService;
import hr.algebra.webshop.service.OrderService;
import hr.algebra.webshop.service.ProductService;
import hr.algebra.webshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final LoginHistoryService loginHistoryService;
    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalProducts", productService.count());
        model.addAttribute("totalCategories", categoryService.count());
        model.addAttribute("totalOrders", orderService.count());
        model.addAttribute("totalUsers", userService.count());
        return "admin/dashboard";
    }

    @GetMapping("/login-history")
    public String loginHistory(
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("loginHistory",
                loginHistoryService.findAll(PageRequest.of(page, 20)));
        return "admin/login-history";
    }

    @GetMapping("/orders")
    public String allOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,
            Model model) {

        model.addAttribute("orders",
                orderService.findFiltered(userId, from, to,
                        PageRequest.of(page, 20)));
        model.addAttribute("users", userService.findAll());
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "admin/orders";
    }
}
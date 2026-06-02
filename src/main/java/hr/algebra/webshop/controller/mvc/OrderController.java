package hr.algebra.webshop.controller.mvc;

import hr.algebra.webshop.enums.PaymentMethod;
import hr.algebra.webshop.service.CartService;
import hr.algebra.webshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    @GetMapping("/checkout")
    public String checkoutPage(Model model) {
        model.addAttribute("items", cartService.getItems());
        model.addAttribute("total", cartService.getTotal());
        return "order/checkout";
    }

    @PostMapping("/checkout")
    public String checkout(
            @RequestParam String paymentMethod,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            PaymentMethod method = PaymentMethod.valueOf(paymentMethod);
            if (method == PaymentMethod.PAYPAL) {
                String approvalUrl = orderService.checkoutWithPayPal(
                        userDetails.getUsername());
                return "redirect:" + approvalUrl;
            }
            orderService.checkout(userDetails.getUsername(), method);
            redirectAttributes.addFlashAttribute("success",
                    "Narudžba uspješno kreirana!");
            return "redirect:/orders/history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders/checkout";
        }
    }

    @GetMapping("/history")
    public String orderHistory(
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        model.addAttribute("orders",
                orderService.findByUser(userDetails.getUsername(),
                        PageRequest.of(page, 10)));
        return "order/history";
    }
}
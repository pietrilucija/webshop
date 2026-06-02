package hr.algebra.webshop.controller.mvc;

import hr.algebra.webshop.service.CartService;
import hr.algebra.webshop.service.OrderService;
import hr.algebra.webshop.service.PayPalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/paypal")
@RequiredArgsConstructor
public class PayPalController {

    private final PayPalService payPalService;
    private final OrderService orderService;
    private final CartService cartService;

    @GetMapping("/success")
    public String success(
            @RequestParam String token,
            RedirectAttributes redirectAttributes) {
        try {
            boolean captured = payPalService.captureOrder(token);
            if (captured) {
                orderService.markAsPaid(token);
                cartService.clear();
                redirectAttributes.addFlashAttribute("success",
                        "PayPal plaćanje uspješno obrađeno!");
                return "redirect:/orders/history";
            }
            redirectAttributes.addFlashAttribute("error",
                    "PayPal nije potvrdio plaćanje. Pokušajte ponovo.");
            return "redirect:/orders/checkout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Greška pri obradi PayPal plaćanja.");
            return "redirect:/orders/checkout";
        }
    }

    @GetMapping("/cancel")
    public String cancel(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error",
                "PayPal plaćanje otkazano.");
        return "redirect:/orders/checkout";
    }
}
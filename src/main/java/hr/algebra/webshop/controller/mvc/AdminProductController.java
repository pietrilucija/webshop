package hr.algebra.webshop.controller.mvc;

import hr.algebra.webshop.dto.request.ProductRequest;
import hr.algebra.webshop.service.CategoryService;
import hr.algebra.webshop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       Model model) {
        model.addAttribute("products",
                productService.findAll(PageRequest.of(page, 10)));
        return "admin/products";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("productRequest", new ProductRequest());
        model.addAttribute("categories", categoryService.findAll());
        return "admin/product-form";
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute ProductRequest productRequest,
                      BindingResult result,
                      Model model,
                      RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "admin/product-form";
        }
        productService.create(productRequest);
        redirectAttributes.addFlashAttribute("success",
                "Proizvod kreiran!");
        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        var product = productService.findById(id);
        ProductRequest request = new ProductRequest();
        request.setName(product.getName());
        request.setDescription(product.getDescription());
        request.setPrice(product.getPrice());
        request.setStock(product.getStock());
        request.setImageUrl(product.getImageUrl());
        request.setCategoryId(product.getCategoryId());
        model.addAttribute("productRequest", request);
        model.addAttribute("productId", id);
        model.addAttribute("categories", categoryService.findAll());
        return "admin/product-form";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute ProductRequest productRequest,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "admin/product-form";
        }
        productService.update(id, productRequest);
        redirectAttributes.addFlashAttribute("success",
                "Proizvod ažuriran!");
        return "redirect:/admin/products";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("success",
                "Proizvod obrisan!");
        return "redirect:/admin/products";
    }
}
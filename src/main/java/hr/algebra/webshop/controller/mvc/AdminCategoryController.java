package hr.algebra.webshop.controller.mvc;

import hr.algebra.webshop.dto.request.CategoryRequest;
import hr.algebra.webshop.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("categoryRequest", new CategoryRequest());
        return "admin/category-form";
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute CategoryRequest categoryRequest,
                      BindingResult result,
                      RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/category-form";
        }
        categoryService.create(categoryRequest);
        redirectAttributes.addFlashAttribute("success",
                "Kategorija kreirana!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        var category = categoryService.findById(id);
        CategoryRequest request = new CategoryRequest();
        request.setName(category.getName());
        request.setDescription(category.getDescription());
        model.addAttribute("categoryRequest", request);
        model.addAttribute("categoryId", id);
        return "admin/category-form";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute CategoryRequest categoryRequest,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/category-form";
        }
        categoryService.update(id, categoryRequest);
        redirectAttributes.addFlashAttribute("success",
                "Kategorija ažurirana!");
        return "redirect:/admin/categories";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        categoryService.delete(id);
        redirectAttributes.addFlashAttribute("success",
                "Kategorija obrisana!");
        return "redirect:/admin/categories";
    }
}
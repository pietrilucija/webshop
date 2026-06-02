package hr.algebra.webshop.config;

import hr.algebra.webshop.model.Category;
import hr.algebra.webshop.model.Product;
import hr.algebra.webshop.model.User;
import hr.algebra.webshop.enums.Role;
import hr.algebra.webshop.repository.CategoryRepository;
import hr.algebra.webshop.repository.ProductRepository;
import hr.algebra.webshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-password:admin123}")
    private String adminPassword;

    @Value("${app.seed.user-password:ana123}")
    private String userPassword;

    @Override
    public void run(String... args) {
        seedUsersIfEmpty();
        seedProductsIfEmpty();
    }

    private void seedUsersIfEmpty() {
        if (userRepository.count() > 0) return;

        userRepository.save(User.builder()
                .email("admin@blush.hr")
                .password(passwordEncoder.encode(adminPassword))
                .firstName("Admin").lastName("Blush")
                .role(Role.ROLE_ADMIN).enabled(true).build());

        userRepository.save(User.builder()
                .email("ana@email.com")
                .password(passwordEncoder.encode(userPassword))
                .firstName("Ana").lastName("Horvat")
                .role(Role.ROLE_USER).enabled(true).build());
    }

    private void seedProductsIfEmpty() {
        if (productRepository.count() > 0) return;

        Category usne = categoryRepository.save(Category.builder()
                .name("Usne").description("Ruževi, sjajila i olovke za usne").build());
        Category lice = categoryRepository.save(Category.builder()
                .name("Lice").description("Puderi, korektori i bronzeri").build());
        Category oci = categoryRepository.save(Category.builder()
                .name("Oči").description("Maskare, sjenila i tuši").build());
        Category njega = categoryRepository.save(Category.builder()
                .name("Njega").description("Kreme, serumi i hidratacija").build());

        saveProduct("Matte ruž - Berry Kiss",
                "Dugotrajni mat ruž u boji bobica. Lagan na usnama.",
                "12.99", 50, "/images/products/ruz.jpg", usne);
        saveProduct("Lip Gloss - Pink Shimmer",
                "Sjajilo za usne s blagim shimmerom. Savršeno za svaki dan.",
                "9.99", 75, "/images/products/gloss.jpg", usne);
        saveProduct("Olovka za usne - Nude",
                "Precizna olovka za definiranje linije usana.",
                "7.50", 60, "/images/products/olovka.png", usne);

        saveProduct("Foundation - Natural Beige",
                "Lagani tekući puder za prirodan izgled. SPF 15.",
                "24.99", 40, "/images/products/puder.jpg", lice);
        saveProduct("Korektor - Light",
                "Pokriva tamne krugove i nesavršenosti.",
                "14.99", 55, "/images/products/korekotr.jpg", lice);
        saveProduct("Bronzer - Sun Kissed",
                "Daje prirodan, suncem poljubljen izgled.",
                "18.50", 35, "/images/products/bronzer.jpg", lice);

        saveProduct("Maskara - Volume Max",
                "Za dramatičan volumen i duge trepavice. Vodootporna.",
                "15.99", 80, "/images/products/maskara.jpg", oci);
        saveProduct("Paleta sjenila - Sunset Vibes",
                "12 nijansi toplih tonova. Mat i shimmer finish.",
                "29.99", 25, "/images/products/sjenilo.jpg", oci);

        saveProduct("Hidratantna krema - Rose Water",
                "Lagana dnevna krema s ružinom vodicom.",
                "22.00", 45, "/images/products/krema.png", njega);
        saveProduct("Serum - Vitamin C",
                "Posvjetljuje ten i smanjuje hiperpigmentaciju.",
                "34.99", 30, "/images/products/serum.jpg", njega);
    }

    private void saveProduct(String name, String desc, String price,
                             int stock, String imageUrl, Category category) {
        productRepository.save(Product.builder()
                .name(name).description(desc)
                .price(new BigDecimal(price))
                .stock(stock).imageUrl(imageUrl)
                .category(category).active(true).build());
    }
}
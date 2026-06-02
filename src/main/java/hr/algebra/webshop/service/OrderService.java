package hr.algebra.webshop.service;

import hr.algebra.webshop.dto.response.OrderItemResponse;
import hr.algebra.webshop.dto.response.OrderResponse;
import hr.algebra.webshop.dto.response.PayPalOrderResult;
import hr.algebra.webshop.model.*;
import hr.algebra.webshop.enums.OrderStatus;
import hr.algebra.webshop.enums.PaymentMethod;
import hr.algebra.webshop.exception.InsufficientStockException;
import hr.algebra.webshop.exception.ResourceNotFoundException;
import hr.algebra.webshop.repository.OrderRepository;
import hr.algebra.webshop.repository.ProductRepository;
import hr.algebra.webshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final PayPalService payPalService;
    private final OrderNotificationService orderNotificationService;

    @Transactional
    public OrderResponse checkout(String email, PaymentMethod paymentMethod) {
        User user = findUser(email);
        if (cartService.getItems().isEmpty()) {
            throw new IllegalStateException("Košarica je prazna");
        }
        Order order = buildOrderFromCart(user, paymentMethod);
        Order saved = orderRepository.save(order);
        cartService.clear();
        orderNotificationService.sendOrderConfirmation(
                saved.getUser().getEmail(), saved.getId(), saved.getTotalAmount());
        return toResponse(saved);
    }

    @Transactional
    public String checkoutWithPayPal(String email) {
        User user = findUser(email);
        if (cartService.getItems().isEmpty()) {
            throw new IllegalStateException("Košarica je prazna");
        }
        Order order = buildOrderFromCart(user, PaymentMethod.PAYPAL);
        PayPalOrderResult result = payPalService.createPayPalOrder(order.getTotalAmount());
        order.setPaypalOrderId(result.orderId());
        orderRepository.save(order);
        return result.approvalUrl();
    }

    public Page<OrderResponse> findByUser(String email, Pageable pageable) {
        User user = findUser(email);
        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::toResponse);
    }

    public Page<OrderResponse> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    public long count() {
        return orderRepository.count();
    }

    public Page<OrderResponse> findFiltered(Long userId,
                                            LocalDateTime from,
                                            LocalDateTime to,
                                            Pageable pageable) {
        if (userId != null && from != null && to != null) {
            return orderRepository
                    .findByUserIdAndCreatedAtBetween(userId, from, to, pageable)
                    .map(this::toResponse);
        } else if (from != null && to != null) {
            return orderRepository
                    .findByCreatedAtBetween(from, to, pageable)
                    .map(this::toResponse);
        }
        return findAll(pageable);
    }

    public void markAsPaid(String paypalOrderId) {
        orderRepository.findByPaypalOrderId(paypalOrderId)
                .ifPresent(order -> {
                    order.setStatus(OrderStatus.PAID);
                    orderRepository.save(order);
                    orderNotificationService.sendPaymentConfirmation(
                            order.getUser().getEmail(), order.getId());
                });
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik nije pronađen"));
    }

    private Order buildOrderFromCart(User user, PaymentMethod method) {
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .paymentMethod(method)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (var item : cartService.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proizvod nije pronađen"));
            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Nedovoljna zaliha za: " + product.getName());
            }
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(item.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            order.addItem(orderItem);
            total = total.add(product.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalAmount(total);
        return order;
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus().name())
                .paymentMethod(order.getPaymentMethod().name())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .customerEmail(order.getUser().getEmail())
                .items(order.getItems().stream()
                        .map(this::toItemResponse).toList())
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }
}
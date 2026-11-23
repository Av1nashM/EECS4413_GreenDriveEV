package ca.yorku.greendriveev.controller;

import ca.yorku.greendriveev.model.Cart;
import ca.yorku.greendriveev.model.CartItem;
import ca.yorku.greendriveev.model.Vehicle;
import ca.yorku.greendriveev.repository.CartItemRepository;
import ca.yorku.greendriveev.repository.CartRepository;
import ca.yorku.greendriveev.repository.UserRepository;
import ca.yorku.greendriveev.repository.VehicleRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
/**
 * REST API for the shopping cart.
 *
 * Every endpoint takes the owning user's id as the ?userId= query parameter
 * (there are no tokens yet), and only touches that user's cart.
 *
 *   GET    /api/cart?userId=1              view the cart with totals
 *   POST   /api/cart/items?userId=1        PB-15 add a vehicle
 *   PUT    /api/cart/items/{id}?userId=1   PB-16 change a line's quantity
 *   DELETE /api/cart/items/{id}?userId=1   PB-17 remove a line
 */
@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    public CartController(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            VehicleRepository vehicleRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
    }

    /** Loads the user's cart, or responds 404 if the user or cart does not exist. */
    private Cart findCart(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found");
        }

        return cartRepository.findByUserId(userId)
            .orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Cart not found"));
    }

    /** Flat view of a cart line; avoids serializing lazy JPA entities (and the user's password hash). */
    // One line of the cart as sent to the frontend (vehicle details + quantity + line total).
    public record CartItemResponse(
            Long itemId,
            Long vehicleId,
            String make,
            String model,
            int year,
            String bodyStyle,
            String imageUrl,
            double price,
            double customizationCost,
            String customizationJson,
            int quantity,
            double configuredPrice,
            double lineTotal) {}

    // PB10: Customization selections submitted when adding a vehicle.
    public record AddCartItemRequest(
        Integer vehicleId,
        Integer quantity,
        String exterior,
        String interior,
        String wheels,
        List<String> accessories,
        List<String> packages) {}

    // Whole cart: its lines, total number of vehicles (sum of quantities), and the price subtotal.
    public record CartResponse(
            List<CartItemResponse> items,
            int itemCount,
            double subtotal) {}

    /** Converts a CartItem entity into the flat response record. */
    private CartItemResponse toResponse(CartItem item) {
        Vehicle v = item.getVehicle();
        return new CartItemResponse(
            item.getId(), v.getId(), v.getMake(), v.getModel(), v.getYear(),
            v.getBodyStyle(), v.getImageUrl(), v.getPrice(),
            item.getCustomizationCost(), item.getCustomizationJson(),
            item.getQuantity(), v.getPrice() + item.getCustomizationCost(), (v.getPrice() + item.getCustomizationCost()) * item.getQuantity());
    }

    /** Returns the user's cart. Runs in a transaction so the lazy vehicle relation can be read. */
    @GetMapping
    @Transactional(readOnly = true)
    public CartResponse getCart(@RequestParam Long userId) {
        Cart cart = findCart(userId);
        List<CartItemResponse> items = cartItemRepository
            .findByCartId(cart.getId()).stream()
            .map(this::toResponse)
            // Stable order so lines don't jump around after an edit.
            .sorted(java.util.Comparator.comparing(CartItemResponse::itemId))
            .toList();

        int itemCount = items.stream().mapToInt(CartItemResponse::quantity).sum();
        double subtotal = items.stream().mapToDouble(CartItemResponse::lineTotal).sum();
        return new CartResponse(items, itemCount, subtotal);
    }

    // Upper limit for one vehicle's quantity in a cart.
    private static final int MAX_QUANTITY = 99;

    // PB10: Sample customization options and demonstration prices.
    private static final Map<String, Double> EXTERIOR_OPTIONS = Map.of(
        "Midnight Black", 0.0,
        "Ocean Blue", 300.0,
        "Pearl White", 500.0,
        "Racing Red", 400.0
    );

    private static final Map<String, Double> INTERIOR_OPTIONS = Map.of(
        "Light", 0.0,
        "Dark", 0.0,
        "Sport", 600.0
    );

    private static final Map<String, Double> WHEEL_OPTIONS = Map.of(
        "Standard", 0.0,
        "Sport", 800.0,
        "Premium", 1200.0
    );

    private static final Map<String, Double> ACCESSORY_OPTIONS = Map.of(
        "Floor mats", 100.0,
        "Cargo organizer", 150.0,
        "Roof rack", 350.0
    );

    private static final Map<String, Double> PACKAGE_OPTIONS = Map.of(
        "Comfort", 500.0,
        "Technology", 900.0,
        "Sport", 1000.0
    );

    // PB10: Stores validated selections and their calculated additional cost.
    private record ValidatedCustomization(
            String exterior,
            String interior,
            String wheels,
            List<String> accessories,
            List<String> packages,
            double cost) {}

    // Validates a single selection and applies its default if none was supplied.
    private static String validateOption(
            String selected,
            Map<String, Double> options,
            String defaultOption,
            String category) {

        if (selected == null || selected.isBlank()) {
            selected = defaultOption;
        }

        if (!options.containsKey(selected)) {
            throw new IllegalArgumentException("Invalid " + category + " selection: " + selected);
        }

        return selected;
    }

    // Validates multiple selections and calculates their combined cost.
    private static List<String> validateMultipleOptions(
            List<String> selected,
            Map<String, Double> options,
            String category) {

        if (selected == null) {
            return List.of();
        }

        if (selected.size() != selected.stream().distinct().count()) {
            throw new IllegalArgumentException("Duplicate " + category + " selections are not allowed.");
        }

        for (String option : selected) {
            if (option == null || !options.containsKey(option)) {
                throw new IllegalArgumentException("Invalid " + category + " selection: " + option);
            }
        }

        return selected.stream().sorted().toList();
    }

    // Validates all customization selections and calculates their total cost.
    private static ValidatedCustomization validateCustomization(
        AddCartItemRequest request) {

        String exterior = validateOption(
            request.exterior(), EXTERIOR_OPTIONS, "Midnight Black", "exterior");

        String interior = validateOption(
            request.interior(), INTERIOR_OPTIONS, "Light", "interior");

        String wheels = validateOption(
            request.wheels(), WHEEL_OPTIONS, "Standard", "wheels");

        List<String> accessories = validateMultipleOptions(
            request.accessories(), ACCESSORY_OPTIONS, "accessory");

        List<String> packages = validateMultipleOptions(
            request.packages(), PACKAGE_OPTIONS, "package");

        double cost = EXTERIOR_OPTIONS.get(exterior)
            + INTERIOR_OPTIONS.get(interior)
            + WHEEL_OPTIONS.get(wheels);

        for (String accessory : accessories) {
            cost += ACCESSORY_OPTIONS.get(accessory);
        }

        for (String selectedPackage : packages) {
            cost += PACKAGE_OPTIONS.get(selectedPackage);
        }

        return new ValidatedCustomization(
            exterior, interior, wheels, accessories, packages, cost);
    }

    // PB10: Converts a list of selections into a JSON array.
    private static String toJsonArray(List<String> values) {
        return values.stream()
            .map(value -> "\"" + value + "\"")
            .collect(java.util.stream.Collectors.joining(",", "[", "]"));
    }

    // PB10: Stores all customization selections as JSON.
    private static String customizationToJson(ValidatedCustomization customization) {
        return "{"
            + "\"exterior\":\"" + customization.exterior() + "\","
            + "\"interior\":\"" + customization.interior() + "\","
            + "\"wheels\":\"" + customization.wheels() + "\","
            + "\"accessories\":" + toJsonArray(customization.accessories()) + ","
            + "\"packages\":" + toJsonArray(customization.packages())
            + "}";
    }

    // PB10: Generates a unique, consistent key for each configuration.
    private static String generateCustomizationKey(String customizationJson) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                customizationJson.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not generate customization key.", e);
        }
    }

    /**
     * PB-15: add a vehicle to the cart. If the vehicle is already in the cart,
     * the requested quantity is added to the existing line instead of creating a duplicate
     * (the cart_items table has a unique (cart, vehicle) constraint).
     */
    @PostMapping("/items")
    @Transactional
    public ResponseEntity<?> addItem(
            @RequestParam Long userId,
            @RequestBody AddCartItemRequest body) {

        Integer vehicleId = body.vehicleId();
        Integer quantity = body.quantity();

        // Reject missing or out-of-range input before touching the database.
        if (vehicleId == null || quantity == null
                || quantity < 1 || quantity > MAX_QUANTITY) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "vehicleId and a quantity between 1 and "
                    + MAX_QUANTITY + " are required."));
        }

        // Validate customization selections and calculate their cost.
        ValidatedCustomization customization;

        try {
            customization = validateCustomization(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage()));
        }

        // Generate JSON and a unique configuration key.
        String customizationJson = customizationToJson(customization);

        String customizationKey =
            generateCustomizationKey(customizationJson);

        Cart cart = findCart(userId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId.longValue())
            .orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Vehicle not found"));

        // Find an existing cart line with the exact same configuration.
        CartItem item = cartItemRepository
            .findByCartIdAndVehicleIdAndCustomizationKey(
                cart.getId(),
                vehicle.getId(),
                customizationKey
            )
            .orElseGet(() -> {
                CartItem newItem = new CartItem();

                newItem.setCart(cart);
                newItem.setVehicle(vehicle);
                newItem.setQuantity(0);

                return newItem;
            });

        // Check the maximum quantity.
        int newQuantity = item.getQuantity() + quantity;

        if (newQuantity > MAX_QUANTITY) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "A cart can hold at most "
                    + MAX_QUANTITY + " of the same configuration."));
        }

        // Save customization information.
        item.setCustomizationJson(customizationJson);
        item.setCustomizationCost(customization.cost());
        item.setCustomizationKey(customizationKey);
        item.setQuantity(newQuantity);

        CartItem savedItem = cartItemRepository.save(item);

        return ResponseEntity.status(HttpStatus.CREATED).body(
            Map.of(
                "message", "Customized vehicle added to cart.",
                "itemId", savedItem.getId(),
                "vehicleId", vehicle.getId(),
                "quantity", savedItem.getQuantity(),
                "customizationCost", savedItem.getCustomizationCost(),
                "customizationKey", savedItem.getCustomizationKey()
            )
        );
    }

    /** PB-16: set a cart line to an exact quantity (not a delta). */
    @PutMapping("/items/{itemId}")
    @Transactional
    public ResponseEntity<?> updateItem(
            @RequestParam Long userId,
            @PathVariable Long itemId,
            @RequestBody Map<String, Integer> body) {

        Integer quantity = body.get("quantity");

        if (quantity == null || quantity < 1 || quantity > MAX_QUANTITY) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Quantity must be between 1 and " + MAX_QUANTITY + "."));
        }

        Cart cart = findCart(userId);

        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Cart item not found"));

        // The item must belong to this user's cart. Answer 404 (not 403) so we
        // don't reveal that the id exists in someone else's cart.
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.NOT_FOUND, "Cart item not found");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return ResponseEntity.ok(Map.of(
            "message", "Cart quantity updated.",
            "itemId", item.getId(),
            "quantity", item.getQuantity()
        ));
    }

    /** PB-17: remove a line from the cart entirely. */
    @DeleteMapping("/items/{itemId}")
    @Transactional
    public ResponseEntity<?> removeItem(
            @RequestParam Long userId,
            @PathVariable Long itemId) {

        Cart cart = findCart(userId);

        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Cart item not found"));

        // Same ownership check as updateItem.
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.NOT_FOUND, "Cart item not found");
        }

        cartItemRepository.delete(item);

        return ResponseEntity.ok(Map.of(
            "message", "Vehicle removed from cart."
        ));
    }
}

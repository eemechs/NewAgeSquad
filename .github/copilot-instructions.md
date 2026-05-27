# Copilot Instructions

## Build / run
- Requires JDK 17+ (javac/java available in PATH).
- Compile: `javac App.java HttpServerApp.java`
- Run: `java App`

## High-level architecture
- `App` implements the CLI loop; `HttpServerApp` hosts the HTTP server mode.
- Product catalog is defined as static `ProductOption[]` arrays (cafes, doces, bebidas geladas).
- Order state is kept in a `LinkedHashMap<String, OrderItem>` keyed by product name to preserve insertion order.
- Finalization prints the receipt and writes `pedido_<nomeCliente>.txt` via `FileWriter`.

## Key conventions
- User prompts and output are in Portuguese; prices use `R$` and `String.format("%.2f", ...)`.
- Coupon logic: only one coupon; code `JAVA10` (case-insensitive) applies 10% of the current total. If items are canceled after applying, discount is recalculated to 10% of the new total.
- Input parsing uses `readInt`/`readPositiveInt` with `Scanner.nextLine()` to avoid newline issues.
- Menus use 1-based indices; item removal targets the product name stored as the map key.

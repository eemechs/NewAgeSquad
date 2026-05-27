import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpServerApp {

    public static void main(String[] args) {

        try {

            startHttpServer();

        } catch (IOException e) {

            System.out.println("Erro ao iniciar o servidor HTTP.");
        }
    }

    static boolean shouldStartHttpServer(String[] args) {

        if (args != null && args.length > 0) {

            String mode = args[0].trim().toLowerCase();

            if (mode.equals("http") || mode.equals("server")) {

                return true;
            }
        }

        return System.getenv("PORT") != null;
    }

    static void startHttpServer() throws IOException {

        int port = resolvePort();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", HttpServerApp::handleIndex);
        server.createContext("/finalizar", HttpServerApp::handleFinalize);
        server.setExecutor(null);
        server.start();

        System.out.println("Servidor HTTP iniciado na porta " + port);
    }

    private static int resolvePort() {

        String portEnv = System.getenv("PORT");

        if (portEnv == null || portEnv.trim().isEmpty()) {

            return 8080;
        }

        try {

            return Integer.parseInt(portEnv.trim());

        } catch (NumberFormatException e) {

            return 8080;
        }
    }

    private static void handleIndex(HttpExchange exchange) throws IOException {

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {

            sendTextResponse(exchange, "Método não permitido.", 405);
            return;
        }

        sendHtmlResponse(exchange, buildIndexHtml(), 200);
    }

    private static void handleFinalize(HttpExchange exchange) throws IOException {

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {

            sendTextResponse(exchange, "Método não permitido.", 405);
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseFormData(body);

        String nomeCliente = params.getOrDefault("nomeCliente", "").trim();

        if (nomeCliente.isEmpty()) {

            sendHtmlResponse(exchange, buildErrorHtml("Digite o nome do cliente."), 400);
            return;
        }

        LinkedHashMap<String, App.OrderItem> orderItems = new LinkedHashMap<>();
        addItemsFromForm(orderItems, "cafe", App.CAFES, params);
        addItemsFromForm(orderItems, "doce", App.DOCES, params);
        addItemsFromForm(orderItems, "bebida", App.BEBIDAS_GELADAS, params);

        double total = App.calcularTotal(orderItems);
        boolean cupomValido = App.CUPOM_VALIDO.equalsIgnoreCase(params.getOrDefault("cupom", ""));
        double desconto = cupomValido ? total * 0.10 : 0;
        String pedidosParaImpressao = App.buildOrderLines(orderItems);

        File arquivo = App.saveOrderToFile(nomeCliente, pedidosParaImpressao, total, desconto, total - desconto);

        if (arquivo == null) {

            sendHtmlResponse(exchange, buildErrorHtml("Erro ao salvar arquivo!"), 500);
            return;
        }

        String recibo = App.buildReceiptText(nomeCliente, pedidosParaImpressao, total, desconto, total - desconto);
        sendHtmlResponse(
                exchange,
                buildReceiptHtml(nomeCliente, pedidosParaImpressao, total, desconto, total - desconto, arquivo.getPath(), recibo, cupomValido),
                200
        );
    }

    private static Map<String, String> parseFormData(String body) {

        Map<String, String> params = new LinkedHashMap<>();

        if (body == null || body.isEmpty()) {

            return params;
        }

        String[] pairs = body.split("&");

        for (String pair : pairs) {

            int idx = pair.indexOf('=');
            String key = idx >= 0 ? pair.substring(0, idx) : pair;
            String value = idx >= 0 ? pair.substring(idx + 1) : "";
            String decodedKey = URLDecoder.decode(key, StandardCharsets.UTF_8);
            String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);
            params.put(decodedKey, decodedValue);
        }

        return params;
    }

    private static void addItemsFromForm(
            Map<String, App.OrderItem> orderItems,
            String prefix,
            App.ProductOption[] options,
            Map<String, String> params
    ) {

        for (int i = 0; i < options.length; i++) {

            String key = prefix + "_" + i;
            int quantity = parsePositiveInt(params.get(key));

            if (quantity > 0) {

                App.ProductOption selected = options[i];
                App.addOrUpdateOrderItem(orderItems, selected.name, selected.price, quantity);
            }
        }
    }

    private static int parsePositiveInt(String value) {

        if (value == null || value.trim().isEmpty()) {

            return 0;
        }

        try {

            int parsed = Integer.parseInt(value.trim());
            return Math.max(parsed, 0);

        } catch (NumberFormatException e) {

            return 0;
        }
    }

    private static void sendHtmlResponse(HttpExchange exchange, String html, int statusCode) throws IOException {

        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {

            os.write(bytes);
        }
    }

    private static void sendTextResponse(HttpExchange exchange, String text, int statusCode) throws IOException {

        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {

            os.write(bytes);
        }
    }

    private static String buildIndexHtml() {

        StringBuilder builder = new StringBuilder();
        builder.append("<!doctype html><html lang=\"pt-br\"><head><meta charset=\"utf-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .append("<title>Cafeteria Arcane</title>")
                .append("<style>")
                .append("body{font-family:Arial,sans-serif;margin:24px;max-width:900px}")
                .append("h1{margin-bottom:4px}")
                .append("table{width:100%;border-collapse:collapse;margin-bottom:16px}")
                .append("th,td{border-bottom:1px solid #ddd;padding:8px;text-align:left}")
                .append("input[type=number]{width:80px}")
                .append(".actions{margin-top:16px}")
                .append("</style>")
                .append("</head><body>");

        builder.append("<h1>Cafeteria Arcane</h1>")
                .append("<p>Preencha os itens e finalize o pedido.</p>")
                .append("<form method=\"post\" action=\"/finalizar\">")
                .append("<label>Nome do cliente:<br>")
                .append("<input type=\"text\" name=\"nomeCliente\" required></label><br><br>");

        buildCategoryHtml(builder, "CAFÉS", "cafe", App.CAFES);
        buildCategoryHtml(builder, "DOCES", "doce", App.DOCES);
        buildCategoryHtml(builder, "BEBIDAS GELADAS", "bebida", App.BEBIDAS_GELADAS);

        builder.append("<label>Cupom (opcional):<br>")
                .append("<input type=\"text\" name=\"cupom\" placeholder=\"JAVA10\"></label>")
                .append("<div class=\"actions\">")
                .append("<button type=\"submit\">Finalizar pedido</button>")
                .append("</div></form></body></html>");

        return builder.toString();
    }

    private static void buildCategoryHtml(
            StringBuilder builder,
            String title,
            String prefix,
            App.ProductOption[] options
    ) {

        builder.append("<h2>").append(title).append("</h2>")
                .append("<table><thead><tr><th>Item</th><th>Preço</th><th>Qtd</th></tr></thead><tbody>");

        for (int i = 0; i < options.length; i++) {

            App.ProductOption option = options[i];
            builder.append("<tr>")
                    .append("<td>").append(escapeHtml(option.name)).append("</td>")
                    .append("<td>R$ ").append(String.format("%.2f", option.price)).append("</td>")
                    .append("<td><input type=\"number\" min=\"0\" value=\"0\" name=\"")
                    .append(prefix).append("_").append(i).append("\"></td>")
                    .append("</tr>");
        }

        builder.append("</tbody></table>");
    }

    private static String buildReceiptHtml(
            String nomeCliente,
            String pedidosParaImpressao,
            double subtotal,
            double desconto,
            double totalFinal,
            String caminhoArquivo,
            String conteudoArquivo,
            boolean cupomValido
    ) {

        StringBuilder builder = new StringBuilder();
        builder.append("<!doctype html><html lang=\"pt-br\"><head><meta charset=\"utf-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .append("<title>Nota Fiscal</title>")
                .append("<style>")
                .append("body{font-family:Arial,sans-serif;margin:24px;max-width:900px}")
                .append("pre{background:#f7f7f7;padding:12px;border-radius:6px}")
                .append("</style></head><body>");

        builder.append("<h1>Nota Fiscal</h1>")
                .append("<p><strong>Cliente:</strong> ").append(escapeHtml(nomeCliente)).append("</p>");

        if (!cupomValido && desconto == 0) {

            builder.append("<p><strong>Cupom:</strong> inválido ou não informado.</p>");
        }

        builder.append("<h2>Itens</h2>")
                .append("<pre>").append(escapeHtml(pedidosParaImpressao)).append("</pre>")
                .append("<p><strong>Subtotal:</strong> R$ ").append(String.format("%.2f", subtotal)).append("</p>")
                .append("<p><strong>Desconto:</strong> R$ ").append(String.format("%.2f", desconto)).append("</p>")
                .append("<p><strong>Total Final:</strong> R$ ").append(String.format("%.2f", totalFinal)).append("</p>")
                .append("<p><strong>Arquivo gerado:</strong> ").append(escapeHtml(caminhoArquivo)).append("</p>")
                .append("<h3>Conteúdo do arquivo</h3>")
                .append("<pre>").append(escapeHtml(conteudoArquivo)).append("</pre>")
                .append("<p><a href=\"/\">Fazer novo pedido</a></p>")
                .append("</body></html>");

        return builder.toString();
    }

    private static String buildErrorHtml(String message) {

        return "<!doctype html><html lang=\"pt-br\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>Erro</title></head><body>"
                + "<h1>Erro</h1><p>" + escapeHtml(message) + "</p>"
                + "<p><a href=\"/\">Voltar</a></p></body></html>";
    }

    private static String escapeHtml(String value) {

        if (value == null) {

            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

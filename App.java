import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class App {

        private static final String CUPOM_VALIDO = "JAVA10";

        private static final ProductOption[] CAFES = {
                new ProductOption("Expresso de Zaun", 5.00),
                new ProductOption("Capputeemo", 8.00),
                new ProductOption("Latte Hextech", 10.00)
        };

        private static final ProductOption[] DOCES = {
                new ProductOption("Braum-nie", 7.00),
                new ProductOption("Heimer-cake", 12.00),
                new ProductOption("Cupcake da Caitlyn", 6.00)
        };

        private static final ProductOption[] BEBIDAS_GELADAS = {
                new ProductOption("Frappushimmer", 14.00),
                new ProductOption("Piltover", 9.00),
                new ProductOption("Ishake", 15.00)
        };

        static class ProductOption {

                String name;
                double price;

                ProductOption(String name, double price) {

                        this.name = name;
                        this.price = price;
                }
        }

    // Classe interna para representar um item do pedido
    static class OrderItem {

        String name;
        double price;
        int quantity;

        public OrderItem(String name, double price, int quantity) {

            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public String getName() {

            return name;
        }

        public double getPrice() {

            return price;
        }

        public int getQuantity() {

            return quantity;
        }

        public void setQuantity(int quantity) {

            this.quantity = quantity;
        }

        public double getTotalPrice() {

            return price * quantity;
        }

        @Override
        public String toString() {

            return quantity + "x " + name
                    + " - R$ "
                    + String.format("%.2f", price * quantity);
        }
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        int opcao = 0;
        double desconto = 0;

        LinkedHashMap<String, OrderItem> orderItems = new LinkedHashMap<>();

        System.out.println("====================================");
        System.out.println("        CAFETERIA ARCANE");
        System.out.println("====================================");

        System.out.print("Digite o nome do cliente: ");
        String nomeCliente = sc.nextLine();

        while (opcao != 8) {

            displayMainMenu();

            System.out.print("Escolha uma opção: ");
            opcao = readInt(sc);

            switch (opcao) {
                case 1:
                    addItemFromCategory(sc, orderItems, "CAFÉS", CAFES, "café");
                    break;
                case 2:
                    addItemFromCategory(sc, orderItems, "DOCES", DOCES, "doce");
                    break;
                case 3:
                    addItemFromCategory(sc, orderItems, "BEBIDAS GELADAS", BEBIDAS_GELADAS, "bebida");
                    break;
                case 4:
                    desconto = applyCoupon(sc, orderItems, desconto);
                    break;
                case 5:
                    displayOrder(orderItems, calcularTotal(orderItems), desconto);
                    break;
                case 6:
                    desconto = cancelOrderItem(sc, orderItems, desconto);
                    break;
                case 7:
                    orderItems.clear();
                    desconto = 0;
                    System.out.println("Pedido limpo com sucesso!");
                    break;
                case 8:
                    finalizeOrder(nomeCliente, orderItems, desconto);
                    break;
                default:
                    System.out.println("Opção inválida!");
            }
        }

        sc.close();
    }

        private static void displayMainMenu() {

                System.out.println("\n========== MENU ==========");
                System.out.println("1 - Cafés");
                System.out.println("2 - Doces");
                System.out.println("3 - Bebidas Geladas");
                System.out.println("4 - Aplicar Cupom");
                System.out.println("5 - Ver Pedido");
                System.out.println("6 - Cancelar Item");
                System.out.println("7 - Limpar Pedido");
                System.out.println("8 - Finalizar Pedido");
                System.out.println("==========================");
        }

        private static void addItemFromCategory(
                        Scanner sc,
                        LinkedHashMap<String, OrderItem> orderItems,
                        String categoryTitle,
                        ProductOption[] options,
                        String productLabel
        ) {

                displayCategoryMenu(categoryTitle, options);

                System.out.print("Escolha um " + productLabel + ": ");
                int option = readInt(sc);

                if (option < 1 || option > options.length) {

                        System.out.println("Opção inválida!");
                        return;
                }

                System.out.print("Digite a quantidade: ");
                int quantity = readPositiveInt(sc);

                ProductOption selected = options[option - 1];

                addOrUpdateOrderItem(orderItems, selected.name, selected.price, quantity);

                System.out.println(selected.name + " adicionado!");
        }

        private static void displayCategoryMenu(String title, ProductOption[] options) {

                System.out.println("\n------ " + title + " ------");

                int maxLabelLength = 0;

                for (int i = 0; i < options.length; i++) {

                        String label = (i + 1) + " - " + options[i].name;

                        if (label.length() > maxLabelLength) {

                                maxLabelLength = label.length();
                        }
                }

                int dotsPadding = 6;

                for (int i = 0; i < options.length; i++) {

                        ProductOption option = options[i];
                        String label = (i + 1) + " - " + option.name;
                        int dotsCount = Math.max(2, (maxLabelLength + dotsPadding) - label.length());
                        String dots = ".".repeat(dotsCount);

                        System.out.printf("%s %s R$ %.2f%n", label, dots, option.price);
                }
        }

        private static double applyCoupon(
                        Scanner sc,
                        LinkedHashMap<String, OrderItem> orderItems,
                        double descontoAtual
        ) {

                System.out.print("\nDigite o cupom de desconto: ");
                String cupom = sc.nextLine();

                if (!cupom.equalsIgnoreCase(CUPOM_VALIDO)) {

                        System.out.println("Cupom inválido!");
                        return descontoAtual;
                }

                if (descontoAtual > 0) {

                        System.out.println("Um cupom já foi aplicado.");
                        return descontoAtual;
                }

                double novoDesconto = calcularTotal(orderItems) * 0.10;

                System.out.println("Cupom aplicado com sucesso!");
                System.out.println("Desconto de 10% aplicado.");

                return novoDesconto;
        }

        private static double cancelOrderItem(
                        Scanner sc,
                        LinkedHashMap<String, OrderItem> orderItems,
                        double descontoAtual
        ) {

                if (orderItems.isEmpty()) {

                        System.out.println("Nenhum item no pedido.");
                        return descontoAtual;
                }

                ArrayList<OrderItem> itemsList = new ArrayList<>(orderItems.values());

                System.out.println("\n------ ITENS DO PEDIDO ------");

                for (int i = 0; i < itemsList.size(); i++) {

                        OrderItem item = itemsList.get(i);

                        System.out.println(
                                        (i + 1)
                                                        + " - "
                                                        + item.getQuantity()
                                                        + "x "
                                                        + item.getName()
                                                        + " (R$ "
                                                        + String.format("%.2f", item.getPrice())
                                                        + " cada)"
                        );
                }

                System.out.print("Digite o número do item: ");
                int itemIndex = readInt(sc);

                if (itemIndex < 1 || itemIndex > itemsList.size()) {

                        System.out.println("Número inválido!");
                        return descontoAtual;
                }

                OrderItem itemToCancel = itemsList.get(itemIndex - 1);

                System.out.print("Quantas unidades deseja cancelar? ");
                int quantityToCancel = readInt(sc);

                if (quantityToCancel <= 0) {

                        System.out.println("Quantidade inválida.");
                        return descontoAtual;
                }

                if (quantityToCancel > itemToCancel.getQuantity()) {

                        System.out.println("Quantidade maior que o pedido.");
                        return descontoAtual;
                }

                if (quantityToCancel == itemToCancel.getQuantity()) {

                        orderItems.remove(itemToCancel.getName());
                        System.out.println("Item removido do pedido.");

                } else {

                        itemToCancel.setQuantity(itemToCancel.getQuantity() - quantityToCancel);
                        System.out.println("Quantidade cancelada.");
                }

                if (descontoAtual <= 0) {

                        return descontoAtual;
                }

                System.out.println("Desconto recalculado.");
                return calcularTotal(orderItems) * 0.10;
        }

        private static void finalizeOrder(
                        String nomeCliente,
                        LinkedHashMap<String, OrderItem> orderItems,
                        double desconto
        ) {

                double total = calcularTotal(orderItems);
                double totalFinal = total - desconto;
                String pedidosParaImpressao = buildOrderLines(orderItems);

                System.out.println("\n======= NOTA FISCAL =======");
                System.out.println("Cliente: " + nomeCliente);
                System.out.println("\nItens:");
                System.out.print(pedidosParaImpressao);
                System.out.printf("Subtotal: R$ %.2f%n", total);
                System.out.printf("Desconto: R$ %.2f%n", desconto);
                System.out.printf("Total Final: R$ %.2f%n", totalFinal);

                saveOrderToFile(nomeCliente, pedidosParaImpressao, total, desconto, totalFinal);

                System.out.println("\nObrigado pela preferência!");
        }

        private static String buildOrderLines(LinkedHashMap<String, OrderItem> orderItems) {

                if (orderItems.isEmpty()) {

                        return "Nenhum item no pedido.\n";
                }

                StringBuilder builder = new StringBuilder();

                for (OrderItem item : orderItems.values()) {

                        builder.append(item).append("\n");
                }

                return builder.toString();
        }

        private static void saveOrderToFile(
                        String nomeCliente,
                        String pedidosParaImpressao,
                        double total,
                        double desconto,
                        double totalFinal
        ) {

                String safeNome = nomeCliente == null ? "" : nomeCliente.trim();

                if (safeNome.isEmpty()) {

                        safeNome = "cliente";
                }

                safeNome = safeNome
                                .replaceAll("[\\\\/]+", "_")
                                .replaceAll("\\s+", "_");

                File outputFile = new File("pedido_" + safeNome + ".txt");

                try (FileWriter arquivo = new FileWriter(outputFile)) {

                        arquivo.write(
                                        "======= NOTA FISCAL =======\n"
                                                        + "Cliente: " + nomeCliente + "\n\n"
                                                        + "Itens do Pedido:\n"
                                                        + pedidosParaImpressao
                                                        + "\n"
                                                        + String.format("Subtotal: R$ %.2f\n", total)
                                                        + String.format("Desconto: R$ %.2f\n", desconto)
                                                        + String.format("Total Final: R$ %.2f", totalFinal)
                        );

                        System.out.println("\nArquivo TXT salvo com sucesso!");
                        System.out.println("Local: " + outputFile.getPath());

                } catch (IOException e) {

                        System.out.println("Erro ao salvar arquivo!");
                }
        }

    private static int readInt(Scanner scanner) {

        while (true) {

            try {

                return Integer.parseInt(
                        scanner.nextLine()
                );

            } catch (NumberFormatException e) {

                System.out.println(
                        "Entrada inválida."
                );

                System.out.print(
                        "Tente novamente: "
                );
            }
        }
    }

    private static int readPositiveInt(
            Scanner scanner
    ) {

        while (true) {

            int quantity = readInt(scanner);

            if (quantity > 0) {

                return quantity;

            } else {

                System.out.println(
                        "Digite valor maior que zero."
                );

                System.out.print(
                        "Tente novamente: "
                );
            }
        }
    }

    private static void addOrUpdateOrderItem(
            Map<String, OrderItem> orderItems,
            String name,
            double price,
            int quantity
    ) {

        OrderItem item = orderItems.get(name);

        if (item != null) {

            item.setQuantity(item.getQuantity() + quantity);
            return;
        }

        orderItems.put(name, new OrderItem(name, price, quantity));
    }

    private static double calcularTotal(
            Map<String, OrderItem> orderItems
    ) {

        double currentTotal = 0;

                for (OrderItem item : orderItems.values()) {

            currentTotal += item.getTotalPrice();
        }

        return currentTotal;
    }

    private static void displayOrder(
            Map<String, OrderItem> orderItems,
            double currentTotal,
            double desconto
    ) {

        System.out.println(
                "\n========== SEU PEDIDO =========="
        );

        if (orderItems.isEmpty()) {

            System.out.println(
                    "Nenhum item adicionado."
            );

        } else {

                        int index = 1;

                        for (OrderItem item : orderItems.values()) {

                System.out.println(
                                                index
                                + " - "
                                                                + item
                );

                                index++;
            }
        }

        System.out.println(
                "---------------------------------"
        );

        System.out.printf(
                "Subtotal: R$ %.2f\n",
                currentTotal
        );

        if (desconto > 0) {

            System.out.printf(
                    "Desconto: R$ %.2f\n",
                    desconto
            );
        }

        System.out.printf(
                "Total a Pagar: R$ %.2f\n",
                (currentTotal - desconto)
        );

        System.out.println(
                "================================="
        );
    }
}

# NewAgeSquad

Repositório com projetos de ADS e Ciências da Computação da UnP.

## Descrição do projeto

Este projeto contém uma aplicação de terminal em Java chamada Cafeteria Arcane.
O sistema simula um atendimento de cafeteria, permitindo:

- cadastro do nome do cliente;
- escolha de itens por categoria (cafés, doces e bebidas geladas);
- adição e cancelamento de itens do pedido;
- aplicação de cupom de desconto;
- visualização do subtotal, desconto e total final;
- geração da nota fiscal em arquivo .txt ao finalizar o pedido.

## Tecnologias utilizadas

- Java (JDK 17+ recomendado)
- Java Collections Framework (LinkedHashMap, ArrayList, Map)
- Entrada de dados via Scanner
- Escrita de arquivo com FileWriter

## Como rodar localmente

### 1. Pre-requisitos

- Ter o Java JDK instalado (versao 17 ou superior recomendada)
- Ter o comando `javac` e `java` disponiveis no terminal

Para verificar:

```bash
javac -version
java -version
```

### 2. Clonar o repositorio

```bash
git clone https://github.com/eemechs/NewAgeSquad.git
cd NewAgeSquad
```

### 3. Compilar

```bash
javac App.java HttpServerApp.java
```

### 4. Executar

#### Modo console

```bash
java App
```

#### Modo HTTP (sem framework)

```bash
java HttpServerApp
```

Acesse no navegador:

```
http://localhost:8080
```

Para usar outra porta, defina a variavel `PORT` antes de executar.

## Como rodar no Docker

```bash
docker build -t arcanecoffeshop:latest .
docker run -it --rm -p 8080:8080 -v "$PWD:/app" arcanecoffeshop:latest
```

### 5. Saida gerada

Ao finalizar o pedido, sera criado um arquivo no formato:

```text
pedido_NOME_DO_CLIENTE.txt
```

com o resumo da nota fiscal.

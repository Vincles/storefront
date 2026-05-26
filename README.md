# Arquitetura de Microsserviços - Vitrine (Storefront) & Armazém (Warehouse)

Este projeto foi desenvolvido como o desafio final do Bootcamp da [DIO (Digital Innovation One)](https://www.dio.me/). O objetivo principal é construir uma arquitetura de microsserviços em Java, demonstrando a integração, comunicação e resiliência entre diferentes componentes de um ecossistema de e-commerce.

## 🧱 Estrutura do Projeto

O sistema é dividido em dois componentes principais que interagem entre si:

* **Storefront (Vitrine):** Um microsserviço desenvolvido em **Java com Spring Boot 3**. Ele gerencia os produtos ativos na vitrine, lida com as requisições dos clientes e consulta o ecossistema para obter dados atualizados de preço e estoque.
* **Warehouse (Armazém):** Um utilitário/biblioteca desenvolvido em **Java Puro (gerenciado via Gradle)** que simula as operações de estoque, armazenamento e logística do ecossistema.

---

## 🚀 Melhorias Implementadas

Foi implementada a seguinte melhoria no microsserviço **Storefront**:

### Tratamento de Falhas e Degradação Graciosa (*Graceful Degradation*)
* **O problema:** Originalmente, se o componente `warehouse` ficasse fora do ar, estivesse em manutenção ou sofresse com alta latência, a chamada síncrona do `RestClient` quebrava. Isso gerava um erro `500 Internal Server Error`, derrubando a vitrine inteira e impedindo o usuário de navegar pelos produtos.
* **A solução:** Na classe `ProductServiceImpl`, as requisições HTTP foram envolvidas em blocos de tratamento robustos utilizando a captura da exceção `RestClientException`.
* **O benefício:** Caso o `warehouse` fique inacessível, o sistema captura a falha de forma elegante, gera um log de alerta no console do servidor e aplica uma "degradação graciosa", assumindo um preço padrão (`BigDecimal.ZERO`). Isso impede o efeito cascata, mantém a aplicação `storefront` 100% online e preserva a experiência do usuário.

---

## 🛠️ Tecnologias Utilizadas

* **Java 17 / 21**
* **Spring Boot 3.x** (Web, Data JPA)
* **Gradle & Maven** (Gerenciadores de dependências)
* **RabbitMQ** (Mensageria AMQP)
* **Lombok** (Produtividade no código)

---

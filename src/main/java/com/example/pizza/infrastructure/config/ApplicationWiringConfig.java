package com.example.pizza.infrastructure.config;

import com.example.pizza.adapter.out.external.DeliveryZoneStubAdapter;
import com.example.pizza.adapter.out.persistence.OrderPersistenceAdapter;
import com.example.pizza.adapter.out.persistence.entity.OrderEntity;
import com.example.pizza.adapter.out.persistence.mapper.OrderMapper;
import com.example.pizza.adapter.out.persistence.repository.OrderJpaRepository;
import com.example.pizza.application.port.in.ChangeOrderStatusUseCase;
import com.example.pizza.application.port.in.CreateOrderUseCase;
import com.example.pizza.application.port.in.GetOrderUseCase;
import com.example.pizza.application.port.in.GetOrderBoardUseCase;
import com.example.pizza.application.port.in.ListOrdersUseCase;
import com.example.pizza.application.port.in.AssignCourierUseCase;
import com.example.pizza.application.port.in.CreateOrderWorkflowUseCase;
import com.example.pizza.application.port.in.DeliverOrderUseCase;
import com.example.pizza.application.port.in.ReceivePaymentUseCase;
import com.example.pizza.application.port.in.StartCookingUseCase;
import com.example.pizza.application.port.out.DeliveryZonePort;
import com.example.pizza.application.port.out.OrderRepositoryPort;
import com.example.pizza.application.service.AssignCourierInteractor;
import com.example.pizza.application.service.ChangeOrderStatusInteractor;
import com.example.pizza.application.service.CreateOrderInteractor;
import com.example.pizza.application.service.CreateOrderWorkflowInteractor;
import com.example.pizza.application.service.DeliverOrderInteractor;
import com.example.pizza.application.service.GetOrderBoardInteractor;
import com.example.pizza.application.service.GetOrderInteractor;
import com.example.pizza.application.service.ListOrdersInteractor;
import com.example.pizza.application.service.ReceivePaymentInteractor;
import com.example.pizza.application.service.StartCookingInteractor;
import com.example.pizza.application.service.policy.CourierAssignmentPolicy;
import com.example.pizza.application.service.policy.DeliveryCompletionPolicy;
import com.example.pizza.application.service.policy.KitchenStartPolicy;
import com.example.pizza.application.service.policy.PaymentRequestedPolicy;
import com.example.pizza.domain.policy.OrderPricingPolicy;
import com.example.pizza.domain.policy.OrderStatusTransitionPolicy;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// [SEMINAR-16] Wiring/DI только в infrastructure: здесь мы связываем use cases и adapters.
// Важно: application/domain не имеют Spring-аннотаций и не знают, как их создают.
@Configuration
@EnableJpaRepositories(basePackageClasses = OrderJpaRepository.class)
@EntityScan(basePackageClasses = OrderEntity.class)
public class ApplicationWiringConfig {

  @Bean
  public OrderMapper orderMapper() {
    return new OrderMapper();
  }

  @Bean
  public OrderRepositoryPort orderRepositoryPort(OrderJpaRepository jpaRepository, OrderMapper mapper) {
    return new OrderPersistenceAdapter(jpaRepository, mapper);
  }

  @Bean
  public DeliveryZonePort deliveryZonePort() {
    return new DeliveryZoneStubAdapter();
  }

  @Bean
  public OrderPricingPolicy orderPricingPolicy() {
    return new OrderPricingPolicy();
  }

  @Bean
  public OrderStatusTransitionPolicy orderStatusTransitionPolicy() {
    return new OrderStatusTransitionPolicy();
  }

  @Bean
  public CreateOrderUseCase createOrderUseCase(
      OrderRepositoryPort repo,
      DeliveryZonePort deliveryZonePort,
      OrderPricingPolicy pricingPolicy
  ) {
    return new CreateOrderInteractor(repo, deliveryZonePort, pricingPolicy);
  }

  @Bean
  public ChangeOrderStatusUseCase changeOrderStatusUseCase(
      OrderRepositoryPort repo,
      OrderStatusTransitionPolicy policy
  ) {
    return new ChangeOrderStatusInteractor(repo, policy);
  }


  @Bean
  public ListOrdersUseCase listOrdersUseCase(OrderRepositoryPort repo) {
    return new ListOrdersInteractor(repo);
  }

  @Bean
  public GetOrderUseCase getOrderUseCase(OrderRepositoryPort repo) {
    return new GetOrderInteractor(repo);
  }

  @Bean
  public PaymentRequestedPolicy paymentRequestedPolicy() {
    return new PaymentRequestedPolicy();
  }

  @Bean
  public KitchenStartPolicy kitchenStartPolicy() {
    return new KitchenStartPolicy();
  }

  @Bean
  public CourierAssignmentPolicy courierAssignmentPolicy() {
    return new CourierAssignmentPolicy();
  }

  @Bean
  public DeliveryCompletionPolicy deliveryCompletionPolicy() {
    return new DeliveryCompletionPolicy();
  }

  @Bean
  public CreateOrderWorkflowUseCase createOrderWorkflowUseCase(
      OrderRepositoryPort repo,
      DeliveryZonePort deliveryZonePort,
      OrderPricingPolicy pricingPolicy,
      PaymentRequestedPolicy paymentRequestedPolicy
  ) {
    return new CreateOrderWorkflowInteractor(repo, deliveryZonePort, pricingPolicy, paymentRequestedPolicy);
  }

  @Bean
  public ReceivePaymentUseCase receivePaymentUseCase(
      OrderRepositoryPort repo,
      OrderStatusTransitionPolicy policy,
      KitchenStartPolicy kitchenStartPolicy
  ) {
    return new ReceivePaymentInteractor(repo, policy, kitchenStartPolicy);
  }

  @Bean
  public StartCookingUseCase startCookingUseCase(
      OrderRepositoryPort repo,
      OrderStatusTransitionPolicy policy,
      CourierAssignmentPolicy courierAssignmentPolicy
  ) {
    return new StartCookingInteractor(repo, policy, courierAssignmentPolicy);
  }

  @Bean
  public AssignCourierUseCase assignCourierUseCase(
      OrderRepositoryPort repo,
      OrderStatusTransitionPolicy policy,
      DeliveryCompletionPolicy deliveryCompletionPolicy
  ) {
    return new AssignCourierInteractor(repo, policy, deliveryCompletionPolicy);
  }

  @Bean
  public DeliverOrderUseCase deliverOrderUseCase(
      OrderRepositoryPort repo,
      OrderStatusTransitionPolicy policy
  ) {
    return new DeliverOrderInteractor(repo, policy);
  }

  @Bean
  public GetOrderBoardUseCase getOrderBoardUseCase(OrderRepositoryPort repo) {
    return new GetOrderBoardInteractor(repo);
  }

  // FAQ:
  // Q: Почему use case не помечены @Service?
  // A: Чтобы application слой не зависел от Spring и был тестируемым без контекста. См. [SEMINAR-03].
  // Q: Почему адаптеры не @Component?
  // A: Чтобы wiring был централизован в infrastructure: видно, что и с чем связано. См. [SEMINAR-16].
  // Q: Почему policies как бины?
  // A: Так их можно заменить/переопределить без правок use case (другие цены/правила).
}

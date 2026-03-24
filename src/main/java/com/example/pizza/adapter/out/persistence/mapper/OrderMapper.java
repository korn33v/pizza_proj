package com.example.pizza.adapter.out.persistence.mapper;

import com.example.pizza.adapter.out.persistence.entity.OrderEntity;
import com.example.pizza.adapter.out.persistence.entity.OrderItemEntity;
import com.example.pizza.domain.model.Address;
import com.example.pizza.domain.model.Money;
import com.example.pizza.domain.model.Order;
import com.example.pizza.domain.model.OrderStatus;
import com.example.pizza.domain.model.PizzaItem;
import com.example.pizza.domain.model.PizzaSize;

import java.util.ArrayList;
import java.util.List;

// [SEMINAR-07] Маппинг — отдельная ответственность на стыке миров: domain <-> persistence entity.
// Domain не знает про JPA, поэтому код маппинга живёт во внешнем слое (adapter/out/persistence).
public final class OrderMapper {

  public void applyToEntity(OrderEntity e, Order domain) {
    e.setId(domain.id());

    e.setCity(domain.address().city());
    e.setStreet(domain.address().street());
    e.setHouse(domain.address().house());
    e.setApartment(domain.address().apartment());
    e.setPostcode(domain.address().postcode());

    e.setStatus(domain.status().name());
    e.setDeliveryZone(domain.deliveryZone());

    e.setTotalAmount(domain.totalPrice().amount());
    e.setTotalCurrency(domain.totalPrice().currency());

    e.clearItems();
    for (PizzaItem item : domain.items()) {
      OrderItemEntity it = new OrderItemEntity();
      it.setName(item.name());
      it.setSize(item.size().name());
      it.setQty(item.qty());
      e.addItem(it);
    }
  }

  public Order toDomain(OrderEntity e) {
    Address address = new Address(
        e.getCity(),
        e.getStreet(),
        e.getHouse(),
        e.getApartment(),
        e.getPostcode()
    );

    Money total = new Money(e.getTotalAmount(), e.getTotalCurrency());

    List<PizzaItem> items = new ArrayList<>();
    for (OrderItemEntity it : e.getItems()) {
      PizzaSize size = PizzaSize.valueOf(it.getSize());
      items.add(new PizzaItem(it.getName(), size, it.getQty()));
    }

    return new Order(
        e.getId(),
        items,
        address,
        OrderStatus.valueOf(e.getStatus()),
        total,
        e.getDeliveryZone()
    );
  }

  // [SEMINAR-14] ANTIPATTERN: "Репозиторий возвращает наружу JPA Entity"
  // Плохо: entity потечёт в application/web, появятся LazyInitializationException, и слой domain перестанет быть чистым.

  // FAQ:
  // Q: Почему маппер в adapter, а не в domain?
  // A: Потому что domain не должен знать, что такое JPA Entity. Маппер зависит от entity => он снаружи.
  // Q: Почему не используем @Embeddable для Address/Money?
  // A: Это усилит сцепление домена и JPA. Для семинара важнее показать явный маппинг. См. [SEMINAR-06].
  // Q: Почему clearItems() и заново добавляем?
  // A: Простая стратегия синхронизации коллекции для kata. В проде — аккуратнее (diff), но через адаптер.
}

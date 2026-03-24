package com.example.pizza.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// [SEMINAR-06] JPA Entity НЕ доменная сущность.
// Entity оптимизируется под хранение/ORM, домен — под бизнес-правила и неизменяемость.
@Entity
@Table(name = "orders")
public class OrderEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "city", nullable = false, length = 100)
  private String city;

  @Column(name = "street", nullable = false, length = 200)
  private String street;

  @Column(name = "house", nullable = false, length = 50)
  private String house;

  @Column(name = "apartment", length = 50)
  private String apartment;

  @Column(name = "postcode", nullable = false, length = 20)
  private String postcode;

  @Column(name = "status", nullable = false, length = 30)
  private String status;

  @Column(name = "delivery_zone", nullable = false, length = 50)
  private String deliveryZone;

  @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal totalAmount;

  // В миграции: total_currency CHAR(3). Без явной длины Hibernate ожидает VARCHAR(255) и validate падает.
  // [SEMINAR-17] Согласованность схемы: ddl-auto=validate заставляет нас держать mapping в синхроне с Flyway.
  @Column(name = "total_currency", nullable = false, length = 3)
  private String totalCurrency;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<OrderItemEntity> items = new ArrayList<>();

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getHouse() {
    return house;
  }

  public void setHouse(String house) {
    this.house = house;
  }

  public String getApartment() {
    return apartment;
  }

  public void setApartment(String apartment) {
    this.apartment = apartment;
  }

  public String getPostcode() {
    return postcode;
  }

  public void setPostcode(String postcode) {
    this.postcode = postcode;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getDeliveryZone() {
    return deliveryZone;
  }

  public void setDeliveryZone(String deliveryZone) {
    this.deliveryZone = deliveryZone;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public String getTotalCurrency() {
    return totalCurrency;
  }

  public void setTotalCurrency(String totalCurrency) {
    this.totalCurrency = totalCurrency;
  }

  public List<OrderItemEntity> getItems() {
    return items;
  }

  public void setItems(List<OrderItemEntity> items) {
    this.items = items;
  }

  public void addItem(OrderItemEntity item) {
    items.add(item);
    item.setOrder(this);
  }

  public void clearItems() {
    for (OrderItemEntity it : items) {
      it.setOrder(null);
    }
    items.clear();
  }

  // FAQ:
  // Q: Почему тут сеттеры, а в домене — нет?
  // A: Entity — ORM-структура, Hibernate работает с мутабельными объектами. Домен защищаем от мутабельности.
  // Q: Почему задаём length/precision явно?
  // A: Потому что используем ddl-auto=validate: Hibernate сверяет mapping со схемой Flyway и падает при расхождениях.
}

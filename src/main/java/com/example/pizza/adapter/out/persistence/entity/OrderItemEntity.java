package com.example.pizza.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItemEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  private OrderEntity order;

  @Column(name = "name", nullable = false, length = 120)
  private String name;

  @Column(name = "size", nullable = false, length = 20)
  private String size;

  @Column(name = "qty", nullable = false)
  private int qty;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public OrderEntity getOrder() {
    return order;
  }

  public void setOrder(OrderEntity order) {
    this.order = order;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
  }

  public int getQty() {
    return qty;
  }

  public void setQty(int qty) {
    this.qty = qty;
  }
}

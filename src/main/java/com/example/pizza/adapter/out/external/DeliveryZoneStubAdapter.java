package com.example.pizza.adapter.out.external;

import com.example.pizza.application.port.out.DeliveryZonePort;
import com.example.pizza.domain.model.Address;

public final class DeliveryZoneStubAdapter implements DeliveryZonePort {

  @Override
  public String resolveZone(Address address) {
    String pc = address.postcode().trim();
    if (pc.startsWith("10") || pc.startsWith("11")) {
      return "CENTER";
    }
    return "OUTSKIRTS";
  }

  // FAQ:
  // Q: Почему это в adapter/out, а не в application?
  // A: Потому что это интеграция/политика получения данных извне. Application должен зависеть от порта.
  // Q: Как заменить на реальный HTTP?
  // A: Написать новый адаптер, реализующий DeliveryZonePort, и переподключить wiring. См. [SEMINAR-11], [SEMINAR-16].
}

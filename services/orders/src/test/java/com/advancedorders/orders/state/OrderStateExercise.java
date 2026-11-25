package com.advancedorders.orders.state;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import com.advancedorders.orders.state.OrderStateController.Event;

class OrderStateExercise {
    private Event e(int version,String kind){return new Event(version,kind,"SKU-001",2,true);}
    @Test void creationAndCompensationRebuildState() {
        var s=OrderStateController.fold("order",List.of(e(1,"CREATED"),e(2,"RESERVED"),e(3,"PAYMENT_DECLINED"),e(4,"RELEASED")));
        assertThat(s.status()).isEqualTo("CANCELLED");
        assertThat(s.version()).isEqualTo(4);
        assertThat(s.sku()).isEqualTo("SKU-001");
        assertThat(s.quantity()).isEqualTo(2);
    }
    @Test void successfulHistory() {
        var s=OrderStateController.fold("order",List.of(e(1,"CREATED"),e(2,"RESERVED"),e(3,"PAYMENT_APPROVED"),e(4,"CONFIRMED")));
        assertThat(s.status()).isEqualTo("CONFIRMED");
    }
    @Test void rejectsInvalidTransition() {
        assertThatThrownBy(()->OrderStateController.fold("order",List.of(e(1,"CREATED"),e(2,"CONFIRMED")))).isInstanceOf(IllegalStateException.class);
    }
    @Test void rejectsMissingVersion() {
        assertThatThrownBy(()->OrderStateController.fold("order",List.of(e(1,"CREATED"),e(3,"RESERVED")))).isInstanceOf(IllegalStateException.class);
    }
}

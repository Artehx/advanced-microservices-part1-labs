package com.advancedorders.orders.api;

import com.advancedorders.orders.service.OrderEstimateService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderEstimateController {
    private final OrderEstimateService service;
    public OrderEstimateController(OrderEstimateService service) { this.service = service; }

    // TODO P01: expose GET /estimate, bind four query parameters and delegate to service.
}

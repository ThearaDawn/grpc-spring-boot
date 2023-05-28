package com.rean.controller;

import com.google.protobuf.Descriptors;
import com.rean.service.CustomerService;
import com.rean.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@Slf4j
@RequiredArgsConstructor
public class ApiController {

    private final CustomerService customerService;
    private final ProductService productService;

    @GetMapping("customers/{id}")
    public Map<Descriptors.FieldDescriptor, Object> getCustomer(@PathVariable int id) {
        return customerService.getCustomer(id);
    }

    @GetMapping("customers/location/{location}")
    public List<Map<Descriptors.FieldDescriptor, Object>> getCustomerByLocation(@PathVariable String location) {
        return customerService.getCustomerByLocation(location);
    }

    @GetMapping("products")
    public Map<String, Map<Descriptors.FieldDescriptor, Object>> filterProductHighPrice() {
        return productService.filterProductHighPrice();
    }
    @GetMapping("products/{id}")
    public Map<Descriptors.FieldDescriptor, Object> getProduct(@PathVariable int id) {
        return customerService.getCustomer(id);
    }
    @GetMapping("products/customer/{id}")
    public List<Map<Descriptors.FieldDescriptor, Object>> getProductsByCustomers(@PathVariable int id) {
        return productService.getProductsByCustomers(id);
    }

}

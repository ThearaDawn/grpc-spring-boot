package com.rean.config;

import com.rean.Customer;
import com.rean.Product;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class FakeDataLoader {

    public List<Customer> customers() {
        return Arrays.asList(
                Customer.newBuilder()
                        .setCustomerId(1)
                        .setUsername("dtheara")
                        .setGender("M")
                        .setLocation("Cambodia")
                        .setContact("d.service@gmail.com")
                        .build()
        );
    }

    public List<Product> products() {
        return Arrays.asList(
                Product.newBuilder()
                        .setProductId(1)
                        .setName("Laptop Alhpa")
                        .setPrice(2450)
                        .setCustomer(customers().get(0))
                        .build()
        );
    }
}

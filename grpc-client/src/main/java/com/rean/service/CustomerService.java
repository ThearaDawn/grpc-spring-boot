package com.rean.service;

import com.google.protobuf.Descriptors;
import com.rean.Customer;
import com.rean.CustomerServiceGrpc;
import com.rean.config.FakeDataLoader;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CustomerService {

    @GrpcClient("grpc-rean-server")
    CustomerServiceGrpc.CustomerServiceStub customerServiceStubAsynchronous;

    @GrpcClient("grpc-rean-server")
    CustomerServiceGrpc.CustomerServiceBlockingStub customerServiceBlockingStubSynchronous;

    private final FakeDataLoader fakeDataLoader;

    public CustomerService(FakeDataLoader fakeDataLoader) {
        this.fakeDataLoader = fakeDataLoader;
    }

    public Map<Descriptors.FieldDescriptor, Object> getCustomer(int customerId) {
        Customer customer = Customer.newBuilder().setCustomerId(customerId).build();
        Customer authorResponse = customerServiceBlockingStubSynchronous.getCustomer(customer);
        return authorResponse.getAllFields();
    }

    public List<Map<Descriptors.FieldDescriptor, Object>> getCustomerByLocation(String location) {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final List<Map<Descriptors.FieldDescriptor, Object>> response = new ArrayList<>();
        StreamObserver<Customer> responseObserver = customerServiceStubAsynchronous.getCustomerByLocation(new StreamObserver<>() {
            @Override
            public void onNext(Customer customer) {
                response.add(customer.getAllFields());
            }

            @Override
            public void onError(Throwable throwable) {
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });
        fakeDataLoader.customers()
                .stream()
                .filter(customer -> customer.getLocation().equalsIgnoreCase(location))
                .forEach(customer -> responseObserver.onNext(Customer.newBuilder().setCustomerId(customer.getCustomerId()).build()));
        responseObserver.onCompleted();
        try {
            boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
            return await ? response : Collections.emptyList();
        }catch (InterruptedException ex) {
            log.error(ex.getLocalizedMessage());
            return Collections.emptyList();
        }
    }

}

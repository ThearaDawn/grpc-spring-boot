package com.rean.service;

import com.rean.Customer;
import com.rean.CustomerServiceGrpc;
import com.rean.config.FakeDataLoader;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@GrpcService
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServerService extends CustomerServiceGrpc.CustomerServiceImplBase {

    private final FakeDataLoader fakeDataLoader;


    /**
     *
     * @param customerRequest
     * @param responseObserver
     *
     * unary - synchronous
     * client will send one request and server will respond with one response.
     */
    @Override
    public void getCustomer(Customer customerRequest, StreamObserver<Customer> responseObserver) {
        fakeDataLoader.customers()
                .stream()
                .filter(customer -> customer.getCustomerId() == customerRequest.getCustomerId())
                .findFirst()
                .ifPresent(responseObserver::onNext);
        responseObserver.onCompleted();
    }


    /**
     *
     * @param customerStreamObserver
     * @return
     *
     * bi-directional streaming - Asynchronous
     * client will send stream of request and server will respond with stream of response.
     */
    @Override
    public StreamObserver<Customer> getCustomerByLocation(StreamObserver<Customer> customerStreamObserver) {
        return new StreamObserver<Customer>() {
            final List<Customer> customers = new ArrayList<>();

            @Override
            public void onNext(Customer customer) {
                fakeDataLoader.customers()
                        .stream()
                        .filter(cust -> cust.getCustomerId() == customer.getCustomerId())
                        .forEach(customers::add);
            }

            @Override
            public void onError(Throwable throwable) {
                customerStreamObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                customers.forEach(customerStreamObserver::onNext);
                customerStreamObserver.onCompleted();
            }
        };
    }

}

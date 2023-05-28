package com.rean.service;

import com.google.protobuf.Descriptors;
import com.rean.Customer;
import com.rean.Product;
import com.rean.ProductServiceGrpc;
import com.rean.config.FakeDataLoader;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    @GrpcClient("grpc-rean-server")
    ProductServiceGrpc.ProductServiceStub productServiceStubAsynchronous;
    @GrpcClient("grpc-rean-server")
    ProductServiceGrpc.ProductServiceBlockingStub productServiceStubSynchronous;
    private final FakeDataLoader fakeDataLoader;

    public Map<Descriptors.FieldDescriptor, Object> getProduct(int productId) {
        Product product = Product.newBuilder().setProductId(productId).build();
        Product authorResponse = productServiceStubSynchronous.getProduct(product);
        return authorResponse.getAllFields();
    }

    public List<Map<Descriptors.FieldDescriptor, Object>> getProductsByCustomers(int customerId) {
        final Customer customerRequest = Customer.newBuilder().setCustomerId(customerId).build();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final List<Map<Descriptors.FieldDescriptor, Object>> response = new ArrayList<>();
        productServiceStubAsynchronous.getProductsByCustomers(customerRequest, new StreamObserver<>() {
            @Override
            public void onNext(Product product) {
                response.add(product.getAllFields());
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

        try {
            boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
            return await ? response : Collections.emptyList();
        }catch (InterruptedException ex) {
            log.error(ex.getLocalizedMessage());
            return Collections.emptyList();
        }
    }

    public Map<String, Map<Descriptors.FieldDescriptor, Object>> filterProductHighPrice() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final Map<String, Map<Descriptors.FieldDescriptor, Object>> response = new HashMap<>();
        StreamObserver<Product> responseObserver = productServiceStubAsynchronous.filterProductHighPrice(new StreamObserver<>() {
            @Override
            public void onNext(Product product) {
                response.put("High Price", product.getAllFields());
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
        fakeDataLoader.products().forEach(responseObserver::onNext);
        responseObserver.onCompleted();
        try {
            boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
            return await ? response : Collections.emptyMap();
        }catch (InterruptedException ex) {
            log.error(ex.getLocalizedMessage());
            return Collections.emptyMap();
        }
    }
}

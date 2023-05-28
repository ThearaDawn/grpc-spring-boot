package com.rean.service;

import com.rean.Customer;
import com.rean.Product;
import com.rean.ProductServiceGrpc;
import com.rean.config.FakeDataLoader;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

@GrpcService
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServerService extends ProductServiceGrpc.ProductServiceImplBase {

    private final FakeDataLoader fakeDataLoader;


    /**
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void getProduct(Product request, StreamObserver<Product> responseObserver) {
        fakeDataLoader.products()
                .stream()
                .filter(product -> product.getProductId() == request.getProductId())
                .findFirst()
                .ifPresent(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    /**
     *
     * @param customerRequest
     * @param productStreamObserver
     * sever streaming - Asynchronous
     * client will send one request and server will send stream of response to the client.
     */
    @Override
    public void getProductsByCustomers(Customer customerRequest, StreamObserver<Product> productStreamObserver) {
        fakeDataLoader.products()
                .stream()
                .filter(product -> product.getCustomer().getCustomerId() == customerRequest.getCustomerId())
                .forEach(productStreamObserver::onNext);
        productStreamObserver.onCompleted();
    }

    /**
     *
     * @param productStreamObserver
     * @return
     * client streaming - Asynchronous
     * client will stream of request and server will respond with one response.
     */
    @Override
    public StreamObserver<Product> filterProductHighPrice(StreamObserver<Product> productStreamObserver) {
        return new StreamObserver<Product>() {

            Product productFilter = null;
            double priceTrack = 0;

            @Override
            public void onNext(Product product) {
                if (product.getPrice() > priceTrack) {
                    priceTrack = product.getPrice();
                    productFilter = product;
                }
            }

            @Override
            public void onError(Throwable throwable) {
                productStreamObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                productStreamObserver.onNext(productFilter);
                productStreamObserver.onCompleted();
            }
        };
    }

}

package com.nw.websocket.common;

import io.grpc.stub.StreamObserver;

public class NoopStreamObserver<V> implements StreamObserver<V> {
    public void onNext(V value) {
    }

    public void onError(Throwable t) {
    }

    public void onCompleted() {
    }
}

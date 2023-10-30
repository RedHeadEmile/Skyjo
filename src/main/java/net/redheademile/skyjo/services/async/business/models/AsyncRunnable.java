package net.redheademile.skyjo.services.async.business.models;

@FunctionalInterface
public interface AsyncRunnable {
    void run() throws Exception;
}

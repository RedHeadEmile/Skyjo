package net.redheademile.skyjo.services.async;

import net.redheademile.skyjo.services.async.business.models.AsyncRunnable;

public interface IAsyncService {
    void runAsync(AsyncRunnable runnable);
}

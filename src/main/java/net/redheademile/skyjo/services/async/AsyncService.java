package net.redheademile.skyjo.services.async;

import net.redheademile.skyjo.services.async.business.models.AsyncRunnable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncService implements IAsyncService {
    @Override
    @Async
    public void runAsync(AsyncRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

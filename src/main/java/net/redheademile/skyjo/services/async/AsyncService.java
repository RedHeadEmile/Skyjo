package net.redheademile.skyjo.services.async;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncService implements IAsyncService {
    @Override
    @Async
    public void runAsync(Runnable runnable) {
        runnable.run();
    }
}

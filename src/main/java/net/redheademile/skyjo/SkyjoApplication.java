package net.redheademile.skyjo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.File;

@SpringBootApplication
@EnableAsync
public class SkyjoApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(SkyjoApplication.class, args);

		Environment environment = context.getEnvironment();
		if (environment.getActiveProfiles().length > 0 && environment.getActiveProfiles()[0].equals("dev")) {
			try {
				final Logger logger = LoggerFactory.getLogger(SkyjoApplication.class);

				logger.info("Starting generating API client...");
				final long start = System.currentTimeMillis();
				ProcessBuilder processBuilder = new ProcessBuilder("nswag.cmd", "run", "nswagstudio.nswag");
				processBuilder.directory(new File("."));
				processBuilder.start().waitFor();
				logger.info("API client generated successfully in {} ms.", System.currentTimeMillis() - start);
			}
			catch (Exception e) {
				e.printStackTrace();
				context.close();
			}
		}
	}

}

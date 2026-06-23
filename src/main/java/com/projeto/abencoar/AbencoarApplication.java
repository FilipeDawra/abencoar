package com.projeto.abencoar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication(scanBasePackages = "com.projeto.abencoar")
public class AbencoarApplication {

	public static void main(String[] args) {
		SpringApplication.run(AbencoarApplication.class, args);
	}
	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			System.out.println(">>> LISTANDO TODOS OS BEANS DO SPRING:");
			String[] beanNames = ctx.getBeanDefinitionNames();
			for (String beanName : beanNames) {
				if (beanName.contains("Controller")) {
					System.out.println("ACHEI UM CONTROLLER: " + beanName);
				}
			}
		};
	}
}

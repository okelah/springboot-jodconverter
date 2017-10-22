package cn.patterncat.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"cn.patterncat"})
public class JodconverterExampleApplication{

	public static void main(String[] args) {
		SpringApplication.run(JodconverterExampleApplication.class, args);
	}
}

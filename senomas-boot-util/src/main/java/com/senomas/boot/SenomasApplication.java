package com.senomas.boot;

import org.springframework.boot.SpringApplication;

public abstract class SenomasApplication {

	public static void run(Class<?> source, String... args) {
		for (int j=0, jl=args.length; j<jl; j++) {
			if (args[j].startsWith("--spring.profiles.active=")) {
				String profiles = args[j].substring(25);
				String px[] = profiles.split(",");
				if (px.length > 0) {
					StringBuilder sb = new StringBuilder();
					for (int i = 0, il = px.length; i < il; i++) {
						if (i > 0)
							sb.append(',');
						sb.append(px[i]);
						if ("dev".equals(px[i])) {
							sb.append(",dev-").append(System.getProperty("user.name"));
						}
					}
					System.setProperty("spring.profiles.active", profiles = sb.toString());
				}
				args[j] = "--spring.profiles.active=" + profiles;
			}
		}
		SpringApplication.run(source, args);
	}
}

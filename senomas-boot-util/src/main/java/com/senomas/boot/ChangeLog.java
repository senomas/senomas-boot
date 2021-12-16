package com.senomas.boot;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
public class ChangeLog {
	private static final Logger log = LoggerFactory.getLogger(ChangeLog.class);

	@Autowired
	ResourcePatternResolver resolver;

	@PostConstruct
	public void init() {
		try {
			Resource[] res = resolver.getResources("classpath*:/META-INF/changelog.txt");
			for (Resource r : res) {
				String url = r.getURL().toExternalForm();
				int ix;
				String module;
				if ((ix = url.indexOf(".jar!/META-INF/")) > 0) {
					module = url.substring(0, ix+4);
					ix = module.lastIndexOf('/');
					if (ix > 0) module = module.substring(ix+1);
				} else {
					module = "main";
				}
				log.info("CHANGE-LOG "+module+":\n"+IOUtils.toString(r.getInputStream(), "UTF-8"));
			}
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}
	}

}

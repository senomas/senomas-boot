package com.senomas.common.logback.controller;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.senomas.common.logback.domain.LogDump;
import com.senomas.common.logback.domain.LogDumpRepository;
import com.senomas.common.rs.ResourceNotFoundException;

@RestController
@RequestMapping("${rest.log.uri:${rest.uri}}/log")
public class LogController {
	private static Logger log = LoggerFactory.getLogger(LogController.class);

	@Autowired
	protected LogDumpRepository logRepo;
	
	@PostConstruct
	public void init() {
		log.debug("initialized");
	}

	@RequestMapping(value = "", method = { RequestMethod.GET })
	public Page<LogDump> getLogs(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "pageSize", defaultValue = "100") int pageSize) {
		return logRepo.findAll(new PageRequest(page, pageSize, new Sort(new Order(Direction.DESC, "id"))));
	}

	@RequestMapping(value = "{id}", method = { RequestMethod.GET })
	public LogDump getLog(@PathVariable String id) {
		LogDump dump = logRepo.findOne(id);
		if (dump == null)
			throw new ResourceNotFoundException(LogDump.class.getName() + " with id '" + id + "' not found!");
		return dump;
	}
}

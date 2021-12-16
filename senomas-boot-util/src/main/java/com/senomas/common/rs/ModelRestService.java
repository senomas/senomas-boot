package com.senomas.common.rs;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.google.common.collect.Lists;
import com.senomas.boot.security.SenomasAuthentication;
import com.senomas.common.U;
import com.senomas.common.persistence.Filter;
import com.senomas.common.persistence.PageRepository;
import com.senomas.common.persistence.PageRequestId;
import com.senomas.common.persistence.RepositoryPageParam;

public abstract class ModelRestService<T, K extends Serializable, F extends Filter<FT>, FT> {
	protected Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected PageRepository<T, K, F, FT> repo;

	final protected TypeReference<T> modelType;

	final protected TypeReference<F> filterType;

	final protected String modelCode;

	protected ModelRestService(TypeReference<T> modelType, TypeReference<F> filterType, String modelCode) {
		this.modelType = modelType;
		this.filterType = filterType;
		this.modelCode = modelCode;
	}

	@JsonView(Views.List.class)
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@Transactional
	public List<T> getList() {
		if (modelCode != null) {
			SenomasAuthentication auth = (SenomasAuthentication) SecurityContextHolder.getContext().getAuthentication();
			if (auth == null || !auth.hasAuthority(modelCode + "-list"))
				throw new AccessDeniedException("List access to "+modelCode+" is Denied");
		}
		return Lists.newArrayList(repo.findAll());
	}

	@SuppressWarnings("unchecked")
	@JsonView(Views.List.class)
	@RequestMapping(value = "/page", method = RequestMethod.POST)
	@Transactional
	public PageResponse<T> getPage(@RequestBody String paramStr) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper om = new ObjectMapper();
		JavaType f = om.getTypeFactory().constructType(filterType);
		JavaType ft = ((SimpleType) f).containedType(0);
		RepositoryPageParam<F, FT> param = om.readValue(paramStr, om.getTypeFactory().constructParametricType(RepositoryPageParam.class, f, ft));
		if (modelCode != null) {
			SenomasAuthentication auth = (SenomasAuthentication) SecurityContextHolder.getContext().getAuthentication();
			if (auth == null || !auth.hasAuthority(modelCode + "-list"))
				throw new AccessDeniedException("List access to "+modelCode+" is Denied");
		}
		return new PageResponse<T>((PageRequestId<T>) repo.findFilter(param, (Class<T>) om.getTypeFactory().constructType(modelType).getRawClass()));
	}

	@JsonView(Views.Detail.class)
	@RequestMapping(value = "/{id}", method = { RequestMethod.GET })
	@Transactional
	public T getById(@PathVariable("id") K id) {
		if (modelCode != null) {
			SenomasAuthentication auth = (SenomasAuthentication) SecurityContextHolder.getContext().getAuthentication();
			if (auth == null || !auth.hasAuthority(modelCode + "-detail"))
				throw new AccessDeniedException("Detail access to "+modelCode+" is Denied");
		}
		T obj = repo.findOne(id);
		if (obj == null)
			throw new ResourceNotFoundException("Object "+modelCode+" '" + id + "' not found.");
		return obj;
	}

	@JsonView(Views.Detail.class)
	@RequestMapping(value = "", method = { RequestMethod.POST })
	@Transactional
	public T save(@RequestBody T obj) {
		if (modelCode != null) {
			SenomasAuthentication auth = (SenomasAuthentication) SecurityContextHolder.getContext().getAuthentication();
			if (repo.exists(obj)) {
				if (auth == null || !auth.hasAuthority(modelCode + "-save"))
					throw new AccessDeniedException("Save access to "+modelCode+" is Denied");
			} else {
				if (auth == null || !auth.hasAuthority(modelCode + "-create"))
					throw new AccessDeniedException("Create access to "+modelCode+" is Denied");
			}
		}
		if (log.isDebugEnabled()) log.debug("SAVE "+U.dump(obj));
		return repo.save(obj);
	}

	@JsonView(Views.Detail.class)
	@RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
	@Transactional
	public T delete(@PathVariable("id") K id) {
		if (modelCode != null) {
			SenomasAuthentication auth = (SenomasAuthentication) SecurityContextHolder.getContext().getAuthentication();
			if (auth == null || !auth.hasAuthority(modelCode + "-delete"))
				throw new AccessDeniedException("Delete access to "+modelCode+" is Denied");
		}
		T obj = repo.findOne(id);
		if (obj == null)
			throw new ResourceNotFoundException("Object "+modelCode+" '" + id + "' not found.");
		repo.delete(id);
		return obj;
	}
}

package com.senomas.common.rs;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.senomas.common.persistence.ModelRepository;

public abstract class DataRestService<T, K extends Serializable> {
	Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	ModelRepository<T, K> repo;

	@Autowired
	EntityManager em;

	private final String modelName;
	private final Class<T> modelType;

	public DataRestService(Class<T> modelType) {
		this.modelName = modelType.getSimpleName();
		this.modelType = modelType;
	}

	public DataRestService(String modelName, Class<T> modelType) {
		this.modelName = modelName;
		this.modelType = modelType;
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@Transactional
	public Page<?> getListGET(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "limit", defaultValue = "10") int pageSize,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "ascending", defaultValue = "true") boolean ascending) throws Exception {
		return getList(page, pageSize, sort, ascending, null);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/list", method = RequestMethod.POST, consumes = "application/json")
	@Transactional
	public Page<?> getList(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "limit", defaultValue = "10") int pageSize,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "ascending", defaultValue = "true") boolean ascending,
			@RequestBody(required = false) String param) throws Exception {
		Specifications<T> specs = null;
		ArrayNode fields = null;
		if (param != null) {
			ObjectMapper om = new ObjectMapper();
			JsonNode po = om.readTree(param);
			if (po.has("filter")) {
				for (Iterator<Entry<String, JsonNode>> itr = po.get("filter").fields(); itr.hasNext();) {
					Entry<String, JsonNode> fi = itr.next();
					String fin = fi.getKey();
					Specification<T> spec;
					try {
						spec = (Specification<T>) getClass()
								.getMethod("filter" + fin.substring(0, 1).toUpperCase() + fin.substring(1),
										JsonNode.class)
								.invoke(this, fi.getValue());
					} catch (NoSuchMethodException e) {
						Class<?> ftype = em.getMetamodel().entity(modelType).getAttribute(fin).getJavaType();
						if (String.class.isAssignableFrom(ftype)) {
							spec = filterString(fin, fi.getValue());
						} else if (Date.class.isAssignableFrom(ftype)) {
							spec = filterDate(fin, fi.getValue());
						} else if (Number.class.isAssignableFrom(ftype)) {
							spec = filterNumber(fin, fi.getValue());
						} else {
							log.warn(e.getMessage(), e);
							throw new RuntimeException("'" + modelName + "' not support field '" + fin + "'");
						}
					}
					if (specs == null) {
						specs = Specifications.where(spec);
					} else {
						specs = specs.and(spec);
					}
				}
			}
			if (po.has("fields")) {
				fields = (ArrayNode) po.get("fields");
			}
		}
		PageRequest pageReq;
		if (sort != null) {
			pageReq = new PageRequest(page, pageSize,
					new Sort(new Order(ascending ? Direction.ASC : Direction.DESC, sort)));
		} else {
			pageReq = new PageRequest(page, pageSize);
		}
		Page<T> res;
		if (specs != null) {
			res = repo.findAll(specs, pageReq);
		} else {
			res = repo.findAll(pageReq);
		}
		if (fields != null) {
			List<Json> content = new ArrayList<>(res.getSize());
			ObjectMapper om = new ObjectMapper();
			for (T e : res.getContent()) {
				JsonNode ov = filter(om, fields, om.convertValue(e, JsonNode.class));
				content.add(new Json(om.writeValueAsString(ov)));
			}
			return new PageImpl<Json>(content, pageReq, res.getTotalElements());
		}
		return res;
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@Transactional
	public Object getByIdGET(@PathVariable("id") K id)
			throws Exception {
		return getById(id, null);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.POST, consumes = "application/json")
	@Transactional
	public Object getById(@PathVariable("id") K id, @RequestBody(required = false) String fields)
			throws Exception {
		T obj = repo.findOne(id);
		if (obj == null)
			throw new ResourceNotFoundException("Object '" + modelName + "' with id '" + id + "' not found.");
		if (fields != null) {
			ObjectMapper om = new ObjectMapper();
			JsonNode of = om.readTree(fields);
			if (of.isArray()) {
				JsonNode ov = filter(om, (ArrayNode) of, om.convertValue(obj, JsonNode.class));
				return new Json(om.writeValueAsString(ov));
			}
		}
		return obj;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "", method = { RequestMethod.POST })
	@Transactional
	public K save(@RequestBody T obj) {
		obj = repo.save(obj);
		return (K) em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(obj);
	}

	@RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
	@Transactional
	public T delete(@PathVariable("id") K id) {
		T obj = repo.findOne(id);
		if (obj == null)
			throw new ResourceNotFoundException("Object '" + modelName + "' with id '" + id + "' not found.");
		repo.delete(id);
		return obj;
	}

	public static JsonNode filter(ObjectMapper om, ArrayNode fields, JsonNode object) {
		if (object.isArray()) {
			ArrayNode an = om.createArrayNode();
			for (int i = 0, il = object.size(); i < il; i++) {
				an.add(filter(om, fields, object.get(i)));
			}
			return an;
		} else if (object.isObject()) {
			ObjectNode on = om.createObjectNode();
			for (int i = 0, il = fields.size(); i < il; i++) {
				JsonNode f = fields.get(i);
				if (f.isObject()) {
					// TODO NESTED
				} else {
					String fn = f.asText();
					if (object.has(fn)) {
						on.set(fn, object.get(fn));
					}
				}
			}
			return on;
		}
		return object;
	}

	public static <T> Specification<T> filterString(String field, JsonNode param) {
		return new Specification<T>() {
			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				if (param.isObject()) {
					if (param.has("equal")) {
						return cb.equal(root.get(field), param.get("equal").asText());
					}
					if (param.has("like")) {
						return cb.like(root.get(field), param.get("like").asText());
					}
					throw new RuntimeException("Not supported " + param);
				}
				return cb.like(root.get(field), param.asText());
			}
		};
	}

	public static <T> Specification<T> filterDate(String field, JsonNode param) {
		return new Specification<T>() {
			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
				try {
					if (param.isObject()) {
						return cb.between(root.get(field).as(Date.class), sdf.parse(param.get("start").asText()),
								sdf.parse(param.get("end").asText()));
					}
					return cb.equal(root.get(field).as(Date.class), sdf.parse(param.asText()));
				} catch (ParseException e) {
					throw new RuntimeException(
							"Invalid field date format field: '" + field + "' value: '" + param.asText() + "'");
				}
			}
		};
	}

	public static <T> Specification<T> filterNumber(String field, JsonNode param) {
		return new Specification<T>() {
			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				if (param.isObject()) {
					return cb.between(root.get(field).as(BigDecimal.class), new BigDecimal(param.get("start").asText()),
							new BigDecimal(param.get("end").asText()));
				}
				return cb.equal(root.get(field).as(BigDecimal.class), new BigDecimal(param.asText()));
			}
		};
	}
}

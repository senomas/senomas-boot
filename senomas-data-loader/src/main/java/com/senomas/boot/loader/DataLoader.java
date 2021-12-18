package com.senomas.boot.loader;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.senomas.common.U;

public abstract class DataLoader {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	ResourcePatternResolver resolver;

	@Autowired
	EntityManager em;

	protected Resource[] getResources() throws IOException {
		return resolver.getResources("classpath:/data/*.yml");
	}

	public void load() throws Exception {
		ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
		ObjectMapper jsonMapper = new ObjectMapper();
		jsonMapper.registerModule(new JodaModule());
		jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);

		Resource[] resources = getResources();
		for (Resource res : resources) {
			JsonNode cfg = yamlMapper.readTree(res.getInputStream());
			loadData(cfg);
		}
	}

	public void loadData(JsonNode cfg) throws Exception {
		ObjectMapper jsonMapper = new ObjectMapper();
		jsonMapper.registerModule(new JodaModule());
		jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);

		for (Iterator<Entry<String, JsonNode>> fitr = cfg.fields(); fitr.hasNext();) {
			Entry<String, JsonNode> f = fitr.next();
			String className = f.getKey();
			int ix = className.indexOf('#');
			String code;
			if (ix > 0) {
				code = className.substring(ix + 1);
				className = className.substring(0, ix);
			} else {
				code = null;
			}
			Class<?> cl = Class.forName(className);
			ManagedType<?> type = em.getMetamodel().managedType(cl);
			for (Iterator<JsonNode> itr = f.getValue().elements(); itr.hasNext();) {
				JsonNode data = itr.next();
				if (log.isDebugEnabled())
					log.debug("JSON " + jsonMapper.writeValueAsString(data));
				fix(type, (ObjectNode) data);
				Object obj;
				if (code != null) {
					obj = findByKey(cl, code, data.get(code));
					if (obj != null) {
						if (log.isDebugEnabled())
							log.debug("SKIP " + U.dump(obj));
						continue;
					}
				}
				obj = jsonMapper.readValue(jsonMapper.writeValueAsString(data), cl);
				if (log.isDebugEnabled())
					log.debug("POJO " + U.dump(obj));
				fix(obj);
				em.persist(obj);
				if (log.isDebugEnabled())
					log.debug("PERSIST " + U.dump(obj));
			}
		}
	}

	protected void fix(ManagedType<?> type, ObjectNode obj) {
		Map<String, Object> npojo = new LinkedHashMap<>();
		for (Iterator<Entry<String, JsonNode>> itr = obj.fields(); itr.hasNext();) {
			Entry<String, JsonNode> me = itr.next();
			String key[] = me.getKey().split("#");
			if (key.length == 2) {
				itr.remove();
				Attribute<?, ?> ctype = type.getDeclaredAttribute(key[0]);
				if (ctype instanceof PluralAttribute) {
					PluralAttribute<?, ?, ?> ptype = (PluralAttribute<?, ?, ?>) ctype;
					ArrayNode arr = obj.putArray(key[0]);
					for (Iterator<JsonNode> citr = me.getValue().elements(); citr.hasNext();) {
						arr.addPOJO(findByKey(ptype.getElementType().getJavaType(), key[1], citr.next()));
					}
				} else {
					SingularAttribute<?, ?> stype = (SingularAttribute<?, ?>) ctype;
					npojo.put(key[0], findByKey(stype.getJavaType(), key[1], me.getValue()));
				}
			}
		}
		for (Entry<String, Object> me : npojo.entrySet()) {
			obj.putPOJO(me.getKey(), me.getValue());
		}
		//
		// ObjectMapper jsonMapper = new ObjectMapper();
		// jsonMapper.registerModule(new JodaModule());
		// jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
		// try {
		// log.info("FIX "+type.getJavaType().getName()+"
		// "+jsonMapper.writeValueAsString(obj));
		// } catch (JsonProcessingException e) {
		// log.warn(e.getMessage(), e);
		// }
	}

	protected Object findByKey(Class<?> type, String keyName, JsonNode key) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<?> cq = cb.createQuery(type);
		Root<?> cqr = cq.from(type);
		String kns[] = keyName.split("\\|");
		if (kns.length == 1) {
			cq.where(cb.equal(cqr.get(keyName), key.asText()));
			List<?> lst = em.createQuery(cq).getResultList();
			// log.debug("findByKey ["+type.getName()+"] ["+keyName+"]
			// ["+key.asText()+"] RESULT "+U.dump(lst));
			if (lst.isEmpty())
				return null;
			return lst.get(0);
		} else {
			Predicate px[] = new Predicate[kns.length];
			for (int i = 0, il = kns.length; i < il; i++) {
				String kn = kns[i];
				px[i] = cb.equal(cqr.get(kn), key.get(kn).asText());
			}
			cq.where(px);
			List<?> lst = em.createQuery(cq).getResultList();
			if (lst.isEmpty())
				return null;
			return lst.get(0);
		}
	}

	protected void fix(Object obj) {
	}
}

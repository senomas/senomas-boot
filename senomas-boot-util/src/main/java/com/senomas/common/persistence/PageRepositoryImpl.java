package com.senomas.common.persistence;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.senomas.boot.AuditListener;
import com.senomas.boot.Audited;
import com.senomas.common.rs.PageParam;

@NoRepositoryBean
public class PageRepositoryImpl<T, ID extends Serializable, F extends Filter<FT>, FT> extends SimpleJpaRepository<T, ID>
		implements PageRepository<T, ID, F, FT> {
	private final static Logger qlog = LoggerFactory.getLogger("com.senomas.common.persistence.Stat");

	private static Map<Class<?>, WrapperConfig<?>> configs = new HashMap<>();

	private final JpaEntityInformation<T, ?> entityInformation;
	private final EntityManager em;

	@SuppressWarnings("unused")
	private final PersistenceProvider provider;

	private Class<?> springDataRepositoryInterface;

	@Autowired
	List<AuditListener> auditListeners;

	public PageRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager,
			Class<?> springDataRepositoryInterface) {
		super(entityInformation, entityManager);
		this.entityInformation = entityInformation;
		this.em = entityManager;
		this.provider = PersistenceProvider.fromEntityManager(entityManager);
		this.springDataRepositoryInterface = springDataRepositoryInterface;
	}

	public PageRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
		super(entityInformation, entityManager);
		this.entityInformation = entityInformation;
		this.em = entityManager;
		this.provider = PersistenceProvider.fromEntityManager(entityManager);
	}

	public Class<?> getSpringDataRepositoryInterface() {
		return springDataRepositoryInterface;
	}

	@Override
	public boolean exists(T object) {
		@SuppressWarnings("unchecked")
		ID key = (ID) em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(object);
		if (key == null)
			return false;
		return exists(key);
	}

	@Override
	public T findOne(ID id) {
		long t0 = System.currentTimeMillis();
		T entity = null;
		try {
			entity = super.findOne(id);
			return entity;
		} finally {
			if (entity != null && qlog.isInfoEnabled()) {
				qlog.info("findOne {} {} {}", kv("responsetime", System.currentTimeMillis() - t0),
						kv("entity", entity.getClass().getName()), kv("method", "findOne"));
			}
		}
	}

	@Override
	@Transactional
	public <S extends T> S save(S entity) {
		if (entity.getClass().getAnnotation(Audited.class) != null) {
			try {
				ObjectMapper om = new ObjectMapper();
				om.registerModule(new JodaModule());
				om.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
				om.enable(SerializationFeature.INDENT_OUTPUT);
				om.enableDefaultTypingAsProperty(DefaultTyping.JAVA_LANG_OBJECT, "@type");

				if (entityInformation.isNew(entity)) {
					em.persist(entity);
					JsonNode event = om.readTree(om.writerWithView(Audited.Detail.class).writeValueAsString(entity));
					for (AuditListener al : auditListeners) {
						al.onCreate(entity.getClass().getName(), event);
					}
					return entity;
				} else {
					em.detach(entity);
					Object old = em.find(entity.getClass(),
							em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity));
					em.refresh(old);
					JsonNode ojn = om.readTree(om.writerWithView(Audited.Detail.class).writeValueAsString(old));
					entity = em.merge(entity);
					JsonNode ejn = om.readTree(om.writerWithView(Audited.Detail.class).writeValueAsString(entity));
					ObjectNode event = diff(om, ejn, ojn);
					for (AuditListener al : auditListeners) {
						al.onUpdate(entity.getClass().getName(), event, ejn, ojn);
					}
					return entity;
				}
			} catch (IOException | ClassNotFoundException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		long t0 = System.currentTimeMillis();
		try {
			return super.save(entity);
		} finally {
			if (entity != null && qlog.isInfoEnabled()) {
				qlog.info("save {} {}", kv("responsetime", System.currentTimeMillis() - t0),
						kv("entity", entity.getClass().getName()), kv("method", "save"));
			}
		}
	}

	@Override
	@Transactional
	public void delete(T entity) {
		long t0 = System.currentTimeMillis();
		super.delete(entity);
		if (entity != null && qlog.isInfoEnabled()) {
			qlog.info("delete {} {}", kv("responsetime", System.currentTimeMillis() - t0),
					kv("entity", entity.getClass().getName()), kv("method", "delete"));
		}
		if (entity.getClass().getAnnotation(Audited.class) != null) {
			try {
				ObjectMapper om = new ObjectMapper();
				om.registerModule(new JodaModule());
				om.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
				om.enable(SerializationFeature.INDENT_OUTPUT);
				om.enableDefaultTypingAsProperty(DefaultTyping.JAVA_LANG_OBJECT, "@type");

				JsonNode event = om.readTree(om.writerWithView(Audited.Brief.class).writeValueAsString(entity));
				for (AuditListener al : auditListeners) {
					al.onDelete(entity.getClass().getName(), event);
				}
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}

	protected ObjectNode diff(ObjectMapper om, JsonNode entity, JsonNode old)
			throws IOException, ClassNotFoundException {
		ObjectNode log = om.createObjectNode();
		List<String> names = new LinkedList<>();
		for (Iterator<String> itr = entity.fieldNames(); itr.hasNext();) {
			String k = itr.next();
			if (!names.contains(k))
				names.add(k);
		}
		for (Iterator<String> itr = old.fieldNames(); itr.hasNext();) {
			String k = itr.next();
			if (!names.contains(k))
				names.add(k);
		}
		for (String n : names) {
			if (entity.has(n)) {
				JsonNode ev = entity.get(n);
				if (old.has(n)) {
					JsonNode ov = old.get(n);
					if (!ev.equals(ov)) {
						if (ev.isArray() && ov.isArray()) {
							ArrayNode al = om.createArrayNode();
							List<JsonNode> ea = new LinkedList<>();
							for (Iterator<JsonNode> itr = ev.elements(); itr.hasNext();)
								ea.add(itr.next());
							List<JsonNode> oa = new LinkedList<>();
							for (Iterator<JsonNode> itr = ov.elements(); itr.hasNext();)
								oa.add(itr.next());
							for (Iterator<JsonNode> ei = ea.iterator(); ei.hasNext();) {
								JsonNode eiv = ei.next();
								for (Iterator<JsonNode> oi = oa.iterator(); oi.hasNext();) {
									JsonNode oiv = oi.next();
									if (eiv.equals(oiv)) {
										ei.remove();
										oi.remove();
									}
								}
							}
							for (Iterator<JsonNode> ei = ea.iterator(); ei.hasNext();) {
								JsonNode eiv = ei.next();
								for (Iterator<JsonNode> oi = oa.iterator(); oi.hasNext();) {
									JsonNode oiv = oi.next();
									if (changeView(om, eiv, Audited.Key.class)
											.equals(changeView(om, oiv, Audited.Key.class))) {
										ei.remove();
										oi.remove();
									}
								}
							}
							for (Iterator<JsonNode> ei = ea.iterator(); ei.hasNext();) {
								JsonNode eiv = ei.next();
								ObjectNode le = om.createObjectNode();
								le.set("add", changeView(om, eiv, Audited.Brief.class));
								al.add(le);
							}
							for (Iterator<JsonNode> oi = oa.iterator(); oi.hasNext();) {
								JsonNode oiv = oi.next();
								ObjectNode le = om.createObjectNode();
								le.set("remove", changeView(om, oiv, Audited.Brief.class));
								al.add(le);
							}
							log.set(n, al);
						} else {
							ObjectNode le = om.createObjectNode();
							le.set("new", ev);
							le.set("old", ov);
							log.set(n, le);
						}
					}
				} else {
					ObjectNode le = om.createObjectNode();
					le.set("new", ev);
					log.set(n, le);
				}
			} else {
				ObjectNode le = om.createObjectNode();
				le.set("old", old.get(n));
				log.set(n, le);
			}
		}
		Arrays.asList(entity.fieldNames());
		return log;
	}

	protected JsonNode changeView(ObjectMapper om, JsonNode n, Class<?> view)
			throws ClassNotFoundException, IOException {
		if (n.has("@type")) {
			Object obj = om.treeToValue(n, Class.forName(n.get("@type").asText()));
			return om.readTree(om.writerWithView(view).writeValueAsString(obj));
		}
		return n;
	}

	@Override
	public Page<T> findFilter(PageParam<FT> param, Class<T> type) {
		return findWithSpecification(param.getRequestId(), em, param.getRequest(), param.getFilter(), type);
	}

	protected static <T> WrapperConfig<T> getWrapperConfig(Class<T> type) {
		if (configs.containsKey(type)) {
			@SuppressWarnings("unchecked")
			WrapperConfig<T> cfg = (WrapperConfig<T>) configs.get(type);
			return cfg;
		}
		try {
			WrapperConfig<T> cfg = new WrapperConfig<>();
			for (Constructor<?> c : type.getConstructors()) {
				WrapperConstruct wc = c.getAnnotation(WrapperConstruct.class);
				if (wc != null) {
					cfg.roots = new Class<?>[wc.from().length];
					cfg.rootName = new String[wc.from().length];
					for (int i = 0, il = wc.from().length; i < il; i++) {
						WrapperRoot wr = wc.from()[i];
						cfg.rootName[i] = wr.name();
						cfg.roots[i] = Class.forName(wr.className());
						cfg.rootMap.put(wr.name(), i);
					}
					cfg.params = new String[c.getParameterTypes().length];
					cfg.paramRoots = new int[c.getParameterTypes().length];
					cfg.paramsPath = new String[c.getParameterTypes().length][];
					int px = 0;
					for (Annotation pta[] : c.getParameterAnnotations()) {
						for (Annotation pt : pta) {
							if (pt instanceof WrapperParam) {
								WrapperParam wp = (WrapperParam) pt;
								cfg.params[px] = wp.name();
								if (wp.path().length == 0) {
									cfg.paramRoots[px] = 0;
									cfg.paramsPath[px] = new String[] { wp.name() };
								} else {
									if (!cfg.rootMap.containsKey(wp.path()[0]))
										throw new RuntimeException(
												"Root '" + wp.path()[0] + "' not found for " + type.getName());
									cfg.paramRoots[px] = cfg.rootMap.get(wp.path()[0]);
									cfg.paramsPath[px] = new String[wp.path().length - 1];
									System.arraycopy(wp.path(), 1, cfg.paramsPath[px], 0, cfg.paramsPath[px].length);
								}
								cfg.paramMap.put(wp.name(), px);
							}
						}
						px++;
					}
					cfg.joins = new WrapperJoinConfig[wc.joins().length];
					for (int i = 0, il = cfg.joins.length; i < il; i++) {
						WrapperJoinConfig join = cfg.joins[i] = new WrapperJoinConfig();
						WrapperJoin wj = wc.joins()[i];
						if (!cfg.rootMap.containsKey(wj.left()[0]))
							throw new RuntimeException("Root '" + wj.left()[0] + "' is not found in " + type.getName());
						if (!cfg.rootMap.containsKey(wj.right()[0]))
							throw new RuntimeException(
									"Root '" + wj.right()[0] + "' is not found in " + type.getName());
						join.rootLeft = cfg.rootMap.get(wj.left()[0]);
						join.rootRight = cfg.rootMap.get(wj.right()[0]);
						join.pathLeft = new String[wj.left().length - 1];
						System.arraycopy(wj.left(), 1, join.pathLeft, 0, join.pathLeft.length);
						join.pathRight = new String[wj.right().length - 1];
						System.arraycopy(wj.right(), 1, join.pathRight, 0, join.pathRight.length);
					}
					configs.put(type, cfg);
					return cfg;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		configs.put(type, null);
		return null;
	}

	@SuppressWarnings("hiding")
	protected <T> CompoundSelection<T> createSelections(WrapperConfig<T> cfg, CriteriaBuilder builder,
			CriteriaQuery<T> cq, Class<T> type, Root<?> roots[]) {
		Selection<?> selections[] = new Selection<?>[cfg.params.length];
		for (int i = 0, il = selections.length; i < il; i++) {
			Path<Object> sel = roots[cfg.paramRoots[i]].get(cfg.paramsPath[i][0]);
			for (int j = 1, jl = cfg.paramsPath[i].length; j < jl; j++) {
				sel = sel.get(cfg.paramsPath[i][j]);
			}
			selections[i] = sel;
		}
		return builder.construct(type, selections);
	}

	@SuppressWarnings("hiding")
	protected <T> PageRequestId<T> findWithSpecification(String requestId, EntityManager entityManager,
			Pageable pageable, Filter<FT> filter, Class<T> type) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		long t0 = System.currentTimeMillis();
		long total;
		WrapperConfig<T> cfg = getWrapperConfig(type);
		if (cfg != null) {
			{
				CriteriaQuery<Long> cqTotal = builder.createQuery(Long.class);
				Root<?> roots[] = new Root<?>[cfg.roots.length];
				Map<String, Root<?>> rootMap = new HashMap<>();
				for (int i = 0, il = roots.length; i < il; i++) {
					roots[i] = cqTotal.from(cfg.roots[i]);
					rootMap.put(cfg.rootName[i], roots[i]);
				}
				if (filter != null) {
					QueryBuilder qwb = new QueryBuilder(builder, roots[0], rootMap);
					for (WrapperJoinConfig join : cfg.joins) {
						Path<Object> pl = roots[join.rootLeft].get(join.pathLeft[0]);
						for (int i = 1, il = join.pathLeft.length; i < il; i++)
							pl = pl.get(join.pathLeft[i]);
						Path<Object> pr = roots[join.rootRight].get(join.pathRight[0]);
						for (int i = 1, il = join.pathRight.length; i < il; i++)
							pr = pr.get(join.pathRight[i]);
						qwb.whereAddAnd(builder.equal(pl, pr));
					}
					filter.init(builder, qwb);
					Predicate predicate = qwb.getPredicate();
					if (predicate != null)
						cqTotal.where(predicate);
				}
				cqTotal.select(builder.count(roots[0]));
				TypedQuery<Long> qry = entityManager.createQuery(cqTotal);
				total = qry.getSingleResult();
			}

			TypedQuery<T> qry = null;
			try {
				CriteriaQuery<T> cq = builder.createQuery(type);
				Root<?> roots[] = new Root<?>[cfg.roots.length];
				Map<String, Root<?>> rootMap = new HashMap<>();
				for (int i = 0, il = roots.length; i < il; i++) {
					roots[i] = cq.from(cfg.roots[i]);
					rootMap.put(cfg.rootName[i], roots[i]);
				}
				if (filter != null) {
					QueryBuilder qwb = new QueryBuilder(builder, roots[0], rootMap);
					for (WrapperJoinConfig join : cfg.joins) {
						Path<Object> pl = roots[join.rootLeft].get(join.pathLeft[0]);
						for (int i = 1, il = join.pathLeft.length; i < il; i++)
							pl = pl.get(join.pathLeft[i]);
						Path<Object> pr = roots[join.rootRight].get(join.pathRight[0]);
						for (int i = 1, il = join.pathRight.length; i < il; i++)
							pr = pr.get(join.pathRight[i]);
						qwb.whereAddAnd(builder.equal(pl, pr));
					}
					filter.init(builder, qwb);
					Predicate predicate = qwb.getPredicate();
					if (predicate != null)
						cq.where(predicate);
				}
				cq.select(createSelections(cfg, builder, cq, type, roots));
				if (pageable.getSort() != null) {
					List<Order> orders = new LinkedList<Order>();
					for (Iterator<org.springframework.data.domain.Sort.Order> itr = pageable.getSort().iterator(); itr
							.hasNext();) {
						org.springframework.data.domain.Sort.Order order = itr.next();
						Integer ix = cfg.paramMap.get(order.getProperty());
						if (ix == null)
							throw new RuntimeException("No property '" + order.getProperty() + "'");
						String ppx[] = cfg.paramsPath[ix];
						Path<Object> p = roots[cfg.paramRoots[ix]].get(ppx[0]);
						for (int i = 1, il = cfg.paramsPath[ix].length; i < il; i++)
							p = p.get(ppx[i]);
						if (order.isAscending()) {
							orders.add(builder.asc(p));
						} else {
							orders.add(builder.desc(p));
						}
					}
					cq.orderBy(orders);
				}
				qry = entityManager.createQuery(cq);
				qry.setFirstResult(pageable.getOffset()).setMaxResults(pageable.getPageSize());
				return new PageRequestIdImpl<T>(qry.getResultList(), pageable, total, requestId);
			} finally {
				if (qry != null && qlog.isInfoEnabled()) {
					qlog.info("findWithSpecification {} {} {}", kv("responsetime", System.currentTimeMillis() - t0),
							kv("entity", type.getName()), kv("method", "findWithSpecification"), kv("query", qry.unwrap(Query.class).getQueryString()));
				}
			}
		} else {
			{
				CriteriaQuery<Long> cqTotal = builder.createQuery(Long.class);
				Root<T> root = cqTotal.from(type);
				if (filter != null) {
					QueryBuilder qwb = new QueryBuilder(builder, root);
					filter.init(builder, qwb);
					Predicate predicate = qwb.getPredicate();
					if (predicate != null)
						cqTotal.where(predicate);
				}
				cqTotal.select(builder.count(root));
				TypedQuery<Long> qry = entityManager.createQuery(cqTotal);
				total = qry.getSingleResult();
			}

			TypedQuery<T> qry = null;
			try {
				CriteriaQuery<T> cq = builder.createQuery(type);
				Root<T> root = cq.from(type);
				if (filter != null) {
					QueryBuilder qwb = new QueryBuilder(builder, root);
					filter.init(builder, qwb);
					Predicate predicate = qwb.getPredicate();
					if (predicate != null)
						cq.where(predicate);
				}
				cq.select(root);
				if (pageable.getSort() != null) {
					List<Order> orders = new LinkedList<Order>();
					for (Iterator<org.springframework.data.domain.Sort.Order> itr = pageable.getSort().iterator(); itr
							.hasNext();) {
						org.springframework.data.domain.Sort.Order order = itr.next();
						Path<Object> p = root.get(order.getProperty());
						if (order.isAscending()) {
							orders.add(builder.asc(p));
						} else {
							orders.add(builder.desc(p));
						}
					}
					cq.orderBy(orders);
				}
				qry = entityManager.createQuery(cq);
				qry.setFirstResult(pageable.getOffset()).setMaxResults(pageable.getPageSize());
				return new PageRequestIdImpl<T>(qry.getResultList(), pageable, total, requestId);
			} finally {
				if (qry != null && qlog.isInfoEnabled()) {
					qlog.info("findWithSpecification {} {} {}", kv("responsetime", System.currentTimeMillis() - t0),
							kv("entity", type.getName()), kv("method", "findWithSpecification"), kv("query", qry.unwrap(Query.class).getQueryString()));
				}
			}
		}
	}

	@SuppressWarnings("hiding")
	protected <T> List<T> findWithSpecification(EntityManager entityManager, Filter<T> filter, Class<T> type) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		WrapperConfig<T> cfg = getWrapperConfig(type);
		long t0 = System.currentTimeMillis();
		if (cfg != null) {
			TypedQuery<T> qry = null;
			try {
				CriteriaQuery<T> cq = builder.createQuery(type);
				Root<?> roots[] = new Root<?>[cfg.roots.length];
				Map<String, Root<?>> rootMap = new HashMap<>();
				for (int i = 0, il = roots.length; i < il; i++) {
					roots[i] = cq.from(cfg.roots[i]);
					rootMap.put(cfg.rootName[i], roots[i]);
				}
				if (filter != null) {
					QueryBuilder qwb = new QueryBuilder(builder, roots[0], rootMap);
					for (WrapperJoinConfig join : cfg.joins) {
						Path<Object> pl = roots[join.rootLeft].get(join.pathLeft[0]);
						for (int i = 1, il = join.pathLeft.length; i < il; i++)
							pl = pl.get(join.pathLeft[i]);
						Path<Object> pr = roots[join.rootRight].get(join.pathRight[0]);
						for (int i = 1, il = join.pathRight.length; i < il; i++)
							pr = pr.get(join.pathRight[i]);
						qwb.whereAddAnd(builder.equal(pl, pr));
					}
					filter.init(builder, qwb);
					Predicate predicate = qwb.getPredicate();
					if (predicate != null)
						cq.where(predicate);
					List<Order> orderBy = qwb.getOrderBy();
					if (orderBy != null)
						cq.orderBy(orderBy);
				}
				cq.select(createSelections(cfg, builder, cq, type, roots));
				qry = entityManager.createQuery(cq);
				return qry.getResultList();
			} finally {
				if (qlog.isInfoEnabled()) {
					qlog.info("findWithSpecification {} {} {}", kv("responsetime", System.currentTimeMillis() - t0),
							kv("entity", type.getName()), kv("method", "findWithSpecification"), kv("query", qry.unwrap(Query.class).getQueryString()));
				}
			}
		} else {
			TypedQuery<T> qry = null;
			try {
				CriteriaQuery<T> cq = builder.createQuery(type);
				Root<T> root = cq.from(type);
				if (filter != null) {
					QueryBuilder qwb = new QueryBuilder(builder, root);
					filter.init(builder, qwb);
					Predicate predicate = qwb.getPredicate();
					if (predicate != null)
						cq.where(predicate);
					List<Order> orderBy = qwb.getOrderBy();
					if (orderBy != null)
						cq.orderBy(orderBy);
				}
				cq.select(root);
				qry = entityManager.createQuery(cq);
				return qry.getResultList();
			} finally {
				if (qlog.isInfoEnabled()) {
					qlog.info("findWithSpecification {} {} {}", kv("responsetime", System.currentTimeMillis() - t0),
							kv("entity", type.getName()), kv("method", "findWithSpecification"), kv("query", qry.unwrap(Query.class).getQueryString()));
				}
			}
		}
	}

	public static class WrapperConfig<T> {
		Class<?> roots[];
		String rootName[];
		Map<String, Integer> rootMap = new HashMap<>();
		String params[];
		int paramRoots[];
		String paramsPath[][];
		Map<String, Integer> paramMap = new HashMap<>();
		WrapperJoinConfig joins[];

		@Override
		public String toString() {
			return "WrapperConfig [roots=" + Arrays.toString(roots) + ", rootName=" + Arrays.toString(rootName)
					+ ", rootMap=" + rootMap + ", params=" + Arrays.toString(params) + ", paramRoots="
					+ Arrays.toString(paramRoots) + ", paramsPath=" + Arrays.toString(paramsPath) + ", paramMap="
					+ paramMap + ", joins=" + Arrays.toString(joins) + "]";
		}
	};

	public static class WrapperJoinConfig {
		int rootLeft;
		String pathLeft[];

		int rootRight;
		String pathRight[];

		@Override
		public String toString() {
			return "WrapperJoinConfig [rootLeft=" + rootLeft + ", pathLeft=" + Arrays.toString(pathLeft)
					+ ", rootRight=" + rootRight + ", pathRight=" + Arrays.toString(pathRight) + "]";
		}
	}

}

package com.senomas.common.persistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

public abstract class AbstractCustomRepository {
	private static final Logger log = LoggerFactory.getLogger(AbstractCustomRepository.class);

	private static Map<Class<?>, WrapperConfig<?>> configs = new HashMap<>();

	public AbstractCustomRepository() {
		log.info("init");
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
						System.arraycopy(wj.left(), 1, join.pathLeft, 0, join.pathLeft.length );
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

	protected <T> PageRequestId<T> findWithSpecification(String requestId, EntityManager entityManager,
			Pageable pageable, Filter filter, Class<T> type) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
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
				total = entityManager.createQuery(cqTotal).getSingleResult();
			}

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
			TypedQuery<T> qry = entityManager.createQuery(cq);
			qry.setFirstResult(pageable.getOffset()).setMaxResults(pageable.getPageSize());
			return new PageRequestIdImpl<T>(qry.getResultList(), pageable, total, requestId);
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
				total = entityManager.createQuery(cqTotal).getSingleResult();
			}

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
			TypedQuery<T> qry = entityManager.createQuery(cq);
			qry.setFirstResult(pageable.getOffset()).setMaxResults(pageable.getPageSize());
			return new PageRequestIdImpl<T>(qry.getResultList(), pageable, total, requestId);
		}
	}

	protected <T> List<T> findWithSpecification(EntityManager entityManager, Filter filter, Class<T> type) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		WrapperConfig<T> cfg = getWrapperConfig(type);
		if (cfg != null) {
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
			TypedQuery<T> qry = entityManager.createQuery(cq);
			return qry.getResultList();
		} else {
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
			TypedQuery<T> qry = entityManager.createQuery(cq);
			return qry.getResultList();
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

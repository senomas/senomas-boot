package com.senomas.common.rs;

import java.util.Arrays;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.senomas.common.persistence.Filter;

public class PageParam<FT> {
	int page;
	int size;
	String requestId;
	String orders[];
	protected Filter<FT> filter;
	
	public int getPage() {
		return page;
	}
	
	public void setPage(int page) {
		this.page = page;
	}
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public String getRequestId() {
		return requestId;
	}
	
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String[] getOrders() {
		return orders;
	}
	
	public void setOrders(String[] orders) {
		this.orders = orders;
	}
	
	public Filter<FT> getFilter() {
		return filter;
	}
	
	public PageRequest getRequest() {
		if (size == 0) size = 100;
		if (orders == null || orders.length == 0) {
			return new PageRequest(page, size);
		} else {
			Order orders[] = new Order[this.orders.length];
			for (int i=0, il=orders.length; i<il; i++) {
				String o = this.orders[i];
				if (o.startsWith("!")) {
					orders[i] = new Order(Direction.DESC, o.substring(1));
				} else {
					orders[i] = new Order(Direction.ASC, o);
				}
			}
			return new PageRequest(page, size, new Sort(orders));
		}
	}

	@Override
	public String toString() {
		return "PageParam [page=" + page + ", size=" + size + ", requestId=" + requestId + ", orders="
				+ Arrays.toString(orders) + ", filter=" + filter + "]";
	}
}

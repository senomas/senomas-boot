package com.senomas.common.rs;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonView;
import com.senomas.common.persistence.PageRequestId;

public class PageResponse<T> {

	@JsonView(Object.class)
	String requestId;

	@JsonView(Object.class)
	private Date timestmap;

	@JsonView(Object.class)
	private List<T> content;

	@JsonView(Object.class)
	private Integer totalPages;

	@JsonView(Object.class)
	private Long totalElements;

	@JsonView(Object.class)
	private Integer page;

	@JsonView(Object.class)
	private Integer size;

	@JsonView(Object.class)
	private Boolean first;

	@JsonView(Object.class)
	private Boolean last;
	
	public PageResponse() {
	}
	
	public PageResponse(String requestId, Date timestamp, Page<T> page) {
		this.requestId = requestId;
		this.timestmap = timestamp;
		this.content = page.getContent();
		this.totalPages = page.getTotalPages();
		this.totalElements = page.getTotalElements();
		this.page = page.getNumber();
		this.size = page.getSize();
		this.first = page.isFirst();
		this.last = page.isLast();
	}
	
	public PageResponse(PageRequestId<T> page) {
		this.requestId = page.getRequestId();
		this.timestmap = page.getTimestamp();
		this.content = page.getContent();
		this.totalPages = page.getTotalPages();
		this.totalElements = page.getTotalElements();
		this.page = page.getNumber();
		this.size = page.getSize();
		this.first = page.isFirst();
		this.last = page.isLast();
	}
	
	public String getRequestId() {
		return requestId;
	}
	
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	
	public Date getTimestmap() {
		return timestmap;
	}
	
	public void setTimestmap(Date timestmap) {
		this.timestmap = timestmap;
	}
	
	public List<T> getContent() {
		return content;
	}
	
	public void setContent(List<T> content) {
		this.content = content;
	}
	
	public Integer getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(Integer totalPages) {
		this.totalPages = totalPages;
	}
	
	public Long getTotalElements() {
		return totalElements;
	}
	
	public void setTotalElements(Long totalElements) {
		this.totalElements = totalElements;
	}
	
	public Integer getPage() {
		return page;
	}
	
	public void setPage(Integer page) {
		this.page = page;
	}
	
	public Integer getSize() {
		return size;
	}
	
	public void setSize(Integer size) {
		this.size = size;
	}
	
	public Boolean isFirst() {
		return first;
	}
	
	public void setFirst(Boolean first) {
		this.first = first;
	}
	
	public Boolean isLast() {
		return last;
	}
	
	public void setLast(Boolean last) {
		this.last = last;
	}
}

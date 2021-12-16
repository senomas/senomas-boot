package com.senomas.common.rs;

public class WebResourceLink {
	int order;

	String link;
	
	public WebResourceLink(String link) {
		this.link = link;
	}

	public WebResourceLink(String link, int order) {
		this.link = link;
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	@Override
	public String toString() {
		return "WebResourceLink [order=" + order + ", link=" + link + "]";
	}
}

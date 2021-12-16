package com.senomas.common.rs;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class WebResourceUtil {

	public static List<WebResourceLink> sort(List<WebResourceLink> links) {
		Collections.sort(links, new Comparator<WebResourceLink>() {
			@Override
			public int compare(WebResourceLink o1, WebResourceLink o2) {
				int r = Integer.compare(o1.getOrder(), o2.getOrder());
				if (r == 0) {
					return o1.getLink().compareTo(o2.getLink());
				}
				return r;
			}
		});
		return links;
	}
}

package com.senomas.common.rs;

import java.util.Collection;

public interface WebResource {
	
	public Collection<WebResourceLink> getStylesheets();
	
	public Collection<WebResourceLink> getScripts();

}

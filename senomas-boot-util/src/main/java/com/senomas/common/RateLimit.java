package com.senomas.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RateLimit implements Serializable {
	private static final long serialVersionUID = 290952878851009938L;

	private long timestamp;
	
	private int delta;
	
	private int count[];
	
	private boolean overlimit;
	
	public RateLimit() {
	}
	
	public RateLimit(int delta, int length) {
		this.timestamp = (System.currentTimeMillis() / delta) * delta;
		this.delta = delta;
		int cl = (length + delta - 1) / delta;
		if (cl < 10) {
			if (delta > 100) {
				this.delta = delta /= 5;
				cl = (length + delta - 1) / delta;
			}
		} else if (cl < 50) {
			if (delta > 100) {
				this.delta = delta /= 2;
				cl = (length + delta - 1) / delta;
			}
		}
		U.LAZY_DEBUG("DELTA "+this.delta+" LEN "+cl);
		this.count = new int[cl];
	}
	
	public int add(int count) {
		long ts = System.currentTimeMillis();
		int shift = (int) ((ts - timestamp) / delta);
		if (shift > 0) {
			for (int s = this.count.length-1-shift, d = this.count.length-1; s>=0; s--, d--) {
				this.count[d] = this.count[s];
			}
			for (int i=1; i<Math.min(this.count.length, shift); i++) {
				this.count[i] = 0;
			}
			this.count[0] = count;
			timestamp = timestamp + shift * delta;
		} else {
			this.count[0] += count;
		}
		return this.count[0];
	}
	
	public int add(int count, int length) {
		long ts = System.currentTimeMillis();
		int shift = (int) ((ts - timestamp) / delta);
		if (shift > 0) {
			for (int s = this.count.length-1-shift, d = this.count.length-1; s>=0; s--, d--) {
				this.count[d] = this.count[s];
			}
			for (int i=1; i<Math.min(this.count.length, shift); i++) {
				this.count[i] = 0;
			}
			this.count[0] = count;
			timestamp = timestamp + shift * delta;
		} else {
			this.count[0] += count;
		}
		if (length == 0) {
			return this.count[0];
		}
		int w = (length + this.delta - 1) / this.delta;
		int total = 0;
		for (int i=0; i<w; i++) total += this.count[i];
		return total;
	}
	
	public int get(int length) {
		long ts = System.currentTimeMillis();
		int shift = (int) ((ts - timestamp) / delta);
		if (shift > 0) {
			for (int s = this.count.length-1-shift, d = this.count.length-1; s>=0; s--, d--) {
				this.count[d] = this.count[s];
			}
			for (int i=1; i<Math.min(this.count.length, shift); i++) {
				this.count[i] = 0;
			}
			timestamp = timestamp + shift * delta;
		}
		if (length == 0) {
			return this.count[0];
		}
		int w = (length + this.delta - 1) / this.delta;
		int total = 0;
		for (int i=0; i<w; i++) total += this.count[i];
		return total;
	}

	@JsonIgnore
	public int getTotal() {
		long ts = System.currentTimeMillis();
		int shift = (int) ((ts - timestamp) / delta);
		if (shift > 0) {
			for (int s = this.count.length-1-shift, d = this.count.length-1; s>=0; s--, d--) {
				this.count[d] = this.count[s];
			}
			for (int i=1; i<Math.min(this.count.length, shift); i++) {
				this.count[i] = 0;
			}
			timestamp = timestamp + shift * delta;
		}
		int count = 0;
		for (int i=0, il=this.count.length; i<il; i++) count += this.count[i];
		return count;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getDelta() {
		return delta;
	}

	public void setDelta(int delta) {
		this.delta = delta;
	}

	public int[] getCount() {
		return count;
	}

	public void setCount(int[] count) {
		this.count = count;
	}

	public boolean isOverlimit() {
		return overlimit;
	}

	public void setOverlimit(boolean overlimit) {
		this.overlimit = overlimit;
	}
}

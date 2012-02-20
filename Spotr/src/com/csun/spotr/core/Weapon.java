package com.csun.spotr.core;

public class Weapon {
	private int id;
	private int numUses;
	private double percent;

	public Weapon(int id, double percent, int numUses) {
		this.id = id;
		this.percent = percent;
		this.numUses = numUses;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNumUses() {
		return numUses;
	}

	public void setNumUses(int numUses) {
		this.numUses = numUses;
	}

	public double getPercent() {
		return percent;
	}

	public void setPercent(double percent) {
		this.percent = percent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + numUses;
		long temp;
		temp = Double.doubleToLongBits(percent);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Weapon other = (Weapon) obj;
		if (id != other.id)
			return false;
		if (numUses != other.numUses)
			return false;
		if (Double.doubleToLongBits(percent) != Double.doubleToLongBits(other.percent))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Weapon [id=" + id + ", numUses=" + numUses + ", percent=" + percent + "]";
	}

}

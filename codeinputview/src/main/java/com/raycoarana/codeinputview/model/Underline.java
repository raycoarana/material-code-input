package com.raycoarana.codeinputview.model;

public class Underline {

	private float mFromX;
	private float mFromY;
	private float mToX;
	private float mToY;

	public Underline() {
	}

	public Underline(float fromX, float fromY, float toX, float toY) {
		this.mFromX = fromX;
		this.mFromY = fromY;
		this.mToX = toX;
		this.mToY = toY;
	}

	public void from(float x, float y) {
		this.mFromX = x;
		this.mFromY = y;
	}

	public void to(float x, float y) {
		this.mToX = x;
		this.mToY = y;
	}

	public float getFromX() {
		return mFromX;
	}

	public void setFromX(float mFromX) {
		this.mFromX = mFromX;
	}

	public float getFromY() {
		return mFromY;
	}

	public void setFromY(float fromY) {
		this.mFromY = fromY;
	}

	public float getToX() {
		return mToX;
	}

	public void setToX(float toX) {
		this.mToX = toX;
	}

	public float getToY() {
		return mToY;
	}

	public void setToY(float toY) {
		this.mToY = toY;
	}
}

/*
	Copyright 2013 Anas H. Sulaiman (ahs.pw)
	
	This file is part of Mosaicify.

    Mosaicify is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Mosaicify is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Mosaicify.  If not, see <http://www.gnu.org/licenses/>.
*/

package pw.ahs.mosaicify;

import java.awt.color.ColorSpace;

public class Argb implements Comparable<Argb> {

	public static double epsilon = 216.0 / 24389.0;
	public static double k = 24389.0 / 27.0;

	public static int combineArgb(int a, int r, int g, int b) {
		return (a << 24) + (r << 16) + (g << 8) + b;
	}

	public int a, r, g, b;

	public Argb() {
		a = r = g = b = 0;
	}

	public Argb(int a, int r, int g, int b) {
		this.a = a;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public Argb(int argb) {
		this.a = (argb >> 24) & 0xff;
		this.r = (argb >> 16) & 0xff;
		this.g = (argb >> 8) & 0xff;
		this.b = argb & 0xff;
	}

	public int getArgb() {
		return (a << 24) + (r << 16) + (g << 8) + b;
	}

	public float[] toLab() {
		float[] xyz = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ).fromRGB(
				new float[] { r / 255, g / 255, b / 255 });
		double t = f(xyz[1]);
		double l = 116.0 * t - 16.0;
		double a = 500.0 * (f(xyz[0]) - t);
		double b = 200.0 * (t - f(xyz[2]));
		return new float[] { (float) l, (float) a, (float) b };
	}

	private double f(double x) {
		if (x > epsilon) {
			return Math.cbrt(x);
		} else {
			return (k * x + 16.0) / 116.0;
		}
	}

	@Override
	public int compareTo(Argb o) {
		if (this == o)
			return 0;
		if (this == null)
			if (o == null)
				return 0;
			else
				return -1;
		else if (o == null)
			return 1;
		else {
			int v1 = getArgb();
			int v2 = o.getArgb();
			return v1 > v2 ? 1 : v1 < v2 ? -1 : 0;
		}

	}
}

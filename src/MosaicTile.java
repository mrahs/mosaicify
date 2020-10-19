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

import javafx.scene.image.Image;

public class MosaicTile implements Comparable<MosaicTile> {
	public Image img;
	public Integer colorAvg;
	public boolean used;

	public MosaicTile(Image img) {
		this.img = img;
		this.colorAvg = img != null ? Utils.calcColorAverage(this.img) : 0;
		used = false;
	}

	@Override
	public int compareTo(MosaicTile o) {
		return new Argb(colorAvg).compareTo(new Argb(o.colorAvg));
	}
}

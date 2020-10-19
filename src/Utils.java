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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class Utils {
	public static int calcColorAverage(Image img) {
		PixelReader pr = img.getPixelReader();
		int asum = 0;
		int rsum = 0;
		int gsum = 0;
		int bsum = 0;
		int n = (int) (img.getWidth() * img.getHeight());
		for (int i = 0; i < img.getWidth(); i++) {
			for (int j = 0; j < img.getHeight(); j++) {
				Argb c = new Argb(pr.getArgb(i, j));
				asum += c.a;
				rsum += c.r;
				gsum += c.g;
				bsum += c.b;
			}
		}
		return Argb.combineArgb(asum / n, rsum / n, gsum / n, bsum / n);
	}

	public static Image overlay(Image img, int alpha, int red, int green,
			int blue, double rate) {

		alpha = alpha > 255 ? 255 : alpha < 0 ? 0 : alpha;
		red = red > 255 ? 255 : red < 0 ? 0 : red;
		green = green > 255 ? 255 : green < 0 ? 0 : green;
		blue = blue > 255 ? 255 : blue < 0 ? 0 : blue;
		rate = rate > 1.0f ? 1.0f : rate < 0.0f ? 0.0f : rate;

		int w = (int) img.getWidth();
		int h = (int) img.getHeight();
		WritableImage res = new WritableImage(w, h);
		PixelReader pr = img.getPixelReader();
		PixelWriter pw = res.getPixelWriter();
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				Argb c = new Argb(pr.getArgb(i, j));
				double upRate = rate;
				double downRate = 1 - upRate;
				int a = (int) ((c.a * downRate) + (alpha * upRate));
				a = a > 255 ? 255 : a;
				int r = (int) ((c.r * downRate) + (red * upRate));
				r = r > 255 ? 255 : r;
				int g = (int) ((c.g * downRate) + (green * upRate));
				g = g > 255 ? 255 : g;
				int b = (int) ((c.b * downRate) + (blue * upRate));
				b = b > 255 ? 255 : b;
				pw.setArgb(i, j, Argb.combineArgb(a, r, g, b));
			}
		}
		return res;
	}

	public static Image createImageFromColor(int argb, int w, int h) {
		WritableImage img = new WritableImage(w, h);
		PixelWriter pw = img.getPixelWriter();
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				pw.setArgb(i, j, argb);
			}
		}
		return img;
	}

	public static Image overlay(Image img, int argb, double rate) {
		Argb c = new Argb(argb);
		return overlay(img, c.a, c.r, c.g, c.b, rate);
	}

	public static Image createMosaic(int targetWidth, int targetHeight,
			int hcount, int vcount, double alpha, boolean dontRepeat,
			boolean useBmt, Image imgSrc, List<Image> tiles) {

		// calculate more parameters
		int tileWidth = targetWidth / hcount;
		int tileHeight = targetHeight / vcount;
		int srcTileWidth = (int) (imgSrc.getWidth() / hcount);
		int srcTileHeight = (int) (imgSrc.getHeight() / vcount);
		int size = tiles.size();

		// resize tiles and calculate average color
		final TreeSet<MosaicTile> mtilesTree = new TreeSet<>();
		final List<MosaicTile> mtilesList = new ArrayList<>(tiles.size());
		ImageView tmpView = ImageViewBuilder.create().fitHeight(tileHeight)
				.fitWidth(tileWidth).build();
		if (useBmt)
			for (Image img : tiles) {
				tmpView.setImage(img);
				mtilesTree.add(new MosaicTile(tmpView.snapshot(null, null)));
			}
		else
			for (Image img : tiles) {
				tmpView.setImage(img);
				mtilesList.add(new MosaicTile(tmpView.snapshot(null, null)));
			}

		// mosaic image
		WritableImage mosaicPhoto = new WritableImage(targetWidth, targetHeight);
		PixelWriter pw = mosaicPhoto.getPixelWriter();
		PixelReader pr = imgSrc.getPixelReader();
		WritableImage srcTile = new WritableImage(srcTileWidth, srcTileHeight);
		PixelWriter stpw = srcTile.getPixelWriter();

		MosaicTile[][] mosaicPhotoTiles = new MosaicTile[hcount][vcount];
		boolean turn = false;
		Random random2 = new Random(0);

		// for each tile of the mosaic
		for (int i = 0; i < hcount; i++) {
			for (int j = 0; j < vcount; j++) {
				// get source tile
				stpw.setPixels(0, 0, srcTileWidth, srcTileHeight, pr, i
						* srcTileWidth, j * srcTileHeight);

				// calculate average color
				MosaicTile srcTileM = new MosaicTile(srcTile);

				// find chosen tile
				MosaicTile chosenTile;
				if (useBmt) {
					// get best matching mosaic tile
					chosenTile = mtilesTree.floor(srcTileM);
					if (chosenTile == null)
						chosenTile = mtilesTree.ceiling(srcTileM);
					if (dontRepeat) {
						if (chosenTile.used == turn) {
							chosenTile.used = !turn;
						} else {
							chosenTile = getUnusedBmmt(chosenTile, mtilesTree,
									turn);
							if (chosenTile == null) {
								// all tiles were used
								turn = !turn;
								chosenTile = mtilesTree.floor(srcTileM);
								if (chosenTile == null)
									chosenTile = mtilesTree.ceiling(srcTileM);
								// List<MosaicTile> adjacents = new
								// ArrayList<>(4);
								// if (j > 0) {
								// adjacents.add(mosaicPhotoTiles[i][j - 1]);
								// }
								// if (i > 0) {
								// adjacents.add(mosaicPhotoTiles[i - 1][j]);
								// }
								// if (i > 0 && j > 0) {
								// adjacents.add(mosaicPhotoTiles[i - 1][j -
								// 1]);
								// if (j < vcount - 1)
								// adjacents
								// .add(mosaicPhotoTiles[i - 1][j + 1]);
								// }
								// bmmt = getDifferentTile(adjacents, mtiles);
							} else {
								chosenTile.used = !turn;
							}
						}
					}
				} else {
					chosenTile = mtilesList.get(random2.nextInt(size));
				}
				mosaicPhotoTiles[i][j] = chosenTile;

				// overlay
				PixelReader bmmtpr;
				if (alpha == 0) {
					bmmtpr = chosenTile.img.getPixelReader();
				} else {
					Argb c2 = new Argb(srcTileM.colorAvg);
					Image tmpImg = overlay(chosenTile.img, c2.a, c2.r, c2.g,
							c2.b, alpha);
					bmmtpr = tmpImg.getPixelReader();
				}
				// add bmmt to mosaic photo
				pw.setPixels(i * tileWidth, j * tileHeight, tileWidth,
						tileHeight, bmmtpr, 0, 0);

			} // end for j
		} // end for i
		return mosaicPhoto;
	}

	public static MosaicTile getUnusedBmmt(MosaicTile t,
			TreeSet<MosaicTile> tree, boolean turn) {
		MosaicTile firstBmmt = t;
		while (t.used != turn) {
			t = tree.lower(t);
			if (t == null)
				break;
		}
		if (t == null) {
			// no lower value, search for higher
			if ((t = tree.higher(firstBmmt)) != null) {
				while (t.used != turn) {
					t = tree.higher(t);
					if (t == null)
						break;
				}
			}
		}
		return t;
	}

	// private static MosaicTile getDifferentTile(List<MosaicTile> ts,
	// TreeSet<MosaicTile> tree) {
	//
	// MosaicTile mt = ts.get(0);
	// MosaicTile t = null;
	// int i = 1;
	// do {
	// t = getDifferentTile(i, mt, tree);
	// if (t == mt)
	// return t;
	// ++i;
	// } while (ts.contains(t));
	// return t;
	// }
	//
	// private static MosaicTile getDifferentTile(int delta, MosaicTile t,
	// TreeSet<MosaicTile> tree) {
	// MosaicTile mt = t;
	// for (int i = 0; i < delta; i++) {
	// mt = tree.lower(mt);
	// if (mt == null)
	// break;
	// }
	// if (mt == null) {
	// // no lower, search for higher
	// mt = t;
	// for (int i = 0; i < delta; i++) {
	// mt = tree.higher(mt);
	// if (mt == null)
	// break;
	// }
	// }
	// if (mt == null)
	// return t;
	// return mt;
	// }
}

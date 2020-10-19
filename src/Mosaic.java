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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Accordion;
import javafx.scene.control.AccordionBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxBuilder;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.HyperlinkBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioButtonBuilder;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPaneBuilder;
import javafx.scene.control.Slider;
import javafx.scene.control.SliderBuilder;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPaneBuilder;
import javafx.scene.control.Tab;
import javafx.scene.control.TabBuilder;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TabPaneBuilder;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TitledPaneBuilder;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.TilePaneBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.FileChooserBuilder;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

public class Mosaic extends Application
{

	private File	initialDirectory;

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		final Stage primaryStageF = primaryStage;

		// tabs
		final TabPane tabs = TabPaneBuilder
			.create()
			.maxWidth(Double.MAX_VALUE)
			.maxHeight(Double.MAX_VALUE)
			.tabClosingPolicy(TabClosingPolicy.UNAVAILABLE)
			.side(Side.TOP)
			.build();
		final Tab tabSrc = TabBuilder.create().text("Source Image").build();
		final Tab tabTiles = TabBuilder.create().text("Tiles").build();
		final Tab tabMosaic = TabBuilder.create().text("Mosaic").build();
		final Tab tabAbout = new Tab("About");
		tabs.getTabs().addAll(tabSrc, tabTiles, tabMosaic, tabAbout);

		// about tab
		Text authorText = TextBuilder
			.create()
			.text("By Anas H. Sulaiman (ahs.pw)")
			.fill(Paint.valueOf("blue"))
			.smooth(true)
			.font(Font.font("sans", 24.0))
			.build();
		Text noteText = TextBuilder
			.create()
			.text(
					"This is a rapidly developed prototype. It's far from being complete.\nI decided to drop the application before I finished implementing a progress bar!\nSeveral other enhancements were planned (such as more accurate color comparison and search improvements).\nFeel free to improve.")
			.font(Font.font("sans", 18.0))
			.build();
		authorText.wrappingWidthProperty().bind(tabs.widthProperty());
		noteText.wrappingWidthProperty().bind(tabs.widthProperty());
		Hyperlink hlFork = HyperlinkBuilder
			.create()
			.text("fork..")
			.onAction(new EventHandler<ActionEvent>()
			{

				@Override
				public void handle(ActionEvent event)
				{
					getHostServices().showDocument("https://github.com/ahspw/mosaicify/fork");
				}
			})
			.build();
		tabAbout.setContent(VBoxBuilder
			.create()
			.children(authorText, noteText, hlFork)
			.spacing(10.0)
			.build());

		// side bar
		Accordion accordionSideBar = AccordionBuilder
			.create()
			.maxWidth(Double.MAX_VALUE)
			.maxHeight(Double.MAX_VALUE)
			.build();
		TitledPane ttpSource = TitledPaneBuilder.create().text("Source").build();
		TitledPane ttpTiles = TitledPaneBuilder.create().text("Tiles").build();
		TitledPane ttpMosaic = TitledPaneBuilder.create().text("Mosaic").build();
		accordionSideBar.getPanes().addAll(ttpSource, ttpTiles, ttpMosaic);
		accordionSideBar.setExpandedPane(ttpSource);

		// source controls
		final ImageView ivSrc = ImageViewBuilder.create().preserveRatio(true).build();
		final ScrollPane spSrc = ScrollPaneBuilder
			.create()
			.content(ivSrc)
			.fitToHeight(true)
			.fitToWidth(true)
			.maxHeight(Double.MAX_VALUE)
			.maxWidth(Double.MAX_VALUE)
			.pannable(true)
			.build();
		Button btnSrc = ButtonBuilder
			.create()
			.text("Browse")
			.onAction(new EventHandler<ActionEvent>()
			{
				public void handle(ActionEvent event)
				{
					File file = FileChooserBuilder
						.create()
						.initialDirectory(initialDirectory)
						.extensionFilters(new ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"))
						.build()
						.showOpenDialog(primaryStageF);
					if (file != null)
					{
						initialDirectory = file.getParentFile();
						try
						{
							ivSrc.setImage(new Image(new FileInputStream(file)));
						} catch (FileNotFoundException e)
						{}
					}

				};
			})
			.build();
		final Slider sliderSrc = SliderBuilder
			.create()
			.orientation(Orientation.HORIZONTAL)
			.snapToTicks(true)
			.majorTickUnit(0.9)
			.minorTickCount(5)
			.min(0.1)
			.max(3.0)
			.value(1.0)
			.showTickLabels(true)
			.showTickMarks(true)
			.maxWidth(Double.MAX_VALUE)
			.build();
		ivSrc.fitWidthProperty().bind(new DoubleBinding()
		{
			{
				super.bind(sliderSrc.valueProperty());
			}

			@Override
			protected double computeValue()
			{
				Image img = ivSrc.getImage();
				if (img == null) return 0.0;
				return sliderSrc.getValue() * img.getWidth();
			}
		});
		ivSrc.fitHeightProperty().bind(new DoubleBinding()
		{
			{
				super.bind(sliderSrc.valueProperty());
			}

			@Override
			protected double computeValue()
			{
				Image img = ivSrc.getImage();
				if (img == null) return 0.0;
				return sliderSrc.getValue() * img.getHeight();
			}
		});

		// tiles controls
		final TilePane tpTiles = TilePaneBuilder
			.create()
			.vgap(10)
			.hgap(10)
			.maxHeight(Double.MAX_VALUE)
			.maxWidth(Double.MAX_VALUE)
			.build();
		ScrollPane spTiles = ScrollPaneBuilder
			.create()
			.content(tpTiles)
			.fitToHeight(true)
			.fitToWidth(true)
			.maxHeight(Double.MAX_VALUE)
			.maxWidth(Double.MAX_VALUE)
			.pannable(true)
			.build();
		Button btnTilesAdd = ButtonBuilder
			.create()
			.text("Add tiles")
			.onAction(new EventHandler<ActionEvent>()
			{
				public void handle(ActionEvent event)
				{
					List<File> files = FileChooserBuilder
						.create()
						.initialDirectory(initialDirectory)
						.extensionFilters(new ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"))
						.build()
						.showOpenMultipleDialog(primaryStageF);
					if (files != null)
					{
						initialDirectory = files.get(0).getParentFile();
						try
						{
							for (File f : files)
							{
								Image img = new Image(new FileInputStream(f));
								tpTiles.getChildren().add(
															ImageViewBuilder
																.create()
																.image(img)
																.fitHeight(100)
																.fitWidth(100)
																.build());
							}
						} catch (FileNotFoundException e)
						{}
					}
				};
			})
			.build();
		Button btnTilesClear = ButtonBuilder
			.create()
			.text("Clear")
			.onAction(new EventHandler<ActionEvent>()
			{

				@Override
				public void handle(ActionEvent event)
				{
					tpTiles.getChildren().clear();
				}
			})
			.build();

		// mosaic controls
		Label lblMosaicScale = LabelBuilder.create().text("Scale:").build();
		final DoubleField txtMosaicScale = new DoubleField(0.1, Double.MAX_VALUE, 1.0);
		txtMosaicScale.setPromptText("Scale");

		Label lblMosaicWidth = LabelBuilder.create().text("Width:").build();
		final IntegerField txtMosaicWidth = new IntegerField(100, Integer.MAX_VALUE, 3000);
		txtMosaicWidth.setPromptText("Width");

		Label lblMosaicHeight = LabelBuilder.create().text("Height:").build();
		final IntegerField txtMosaicHeight = new IntegerField(100, Integer.MAX_VALUE, 3000);
		txtMosaicHeight.setPromptText("Height");

		Label lblMosaicTileHeight = new Label("Tile Height");
		final IntegerField txtMosaicTileHeight = new IntegerField(10, Integer.MAX_VALUE, 30);
		txtMosaicTileHeight.setPromptText("Tile Height");
		Label lblMosaicTileWidth = new Label("Tile Width");
		final IntegerField txtMosaicTileWidth = new IntegerField(10, Integer.MAX_VALUE, 30);
		txtMosaicTileWidth.setPromptText("Tile Width");
		Label lblMosaicHcount = LabelBuilder.create().text("Number of horizonal tiles:").build();
		final IntegerField txtMosaicHcount = new IntegerField(10, Integer.MAX_VALUE, 100);
		txtMosaicHcount.setPromptText("Hor. Count");
		Label lblMosaicVcount = LabelBuilder.create().text("Number of vertical tiles:").build();
		final IntegerField txtMosaicVcount = new IntegerField(10, Integer.MAX_VALUE, 100);
		txtMosaicVcount.setPromptText("Ver. Count");

		// to break the loop
		final BooleanProperty block = new SimpleBooleanProperty(false);
		txtMosaicWidth.valueProperty().addListener(new ChangeListener<Number>()
		{

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue,
					Number newValue)
			{
				txtMosaicTileWidth.setValue(newValue.intValue() / txtMosaicHcount.getValue());
				if (block.get()) return;
				Image img = ivSrc.getImage();
				if (img == null) return;
				block.set(true);
				double ratio = img.getWidth() / img.getHeight();
				txtMosaicHeight.setValue((int) (newValue.intValue() / ratio));
				txtMosaicScale.setValue(newValue.intValue() / img.getWidth());
				block.set(false);
			}
		});

		txtMosaicHeight.valueProperty().addListener(new ChangeListener<Number>()
		{

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue,
					Number newValue)
			{
				txtMosaicTileHeight.setValue(newValue.intValue() / txtMosaicVcount.getValue());
				if (block.get()) return;
				Image img = ivSrc.getImage();
				if (img == null) return;
				block.set(true);
				double ratio = img.getWidth() / img.getHeight();
				txtMosaicWidth.setValue((int) (newValue.intValue() * ratio));
				txtMosaicScale.setValue(newValue.intValue() / img.getHeight());
				block.set(false);
			}
		});
		txtMosaicScale.valueProperty().addListener(new ChangeListener<Number>()
		{

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue,
					Number newValue)
			{
				if (block.get()) return;
				Image img = ivSrc.getImage();
				if (img == null) return;
				block.set(true);
				txtMosaicWidth.setValue((int) (img.getWidth() * newValue.doubleValue()));
				txtMosaicHeight.setValue((int) (img.getHeight() * newValue.doubleValue()));
				block.set(false);
			}
		});

		ivSrc.imageProperty().addListener(new ChangeListener<Image>()
		{

			@Override
			public void changed(ObservableValue<? extends Image> observable, Image oldValue,
					Image newValue)
			{
				if (newValue == null) return;
				txtMosaicWidth.setValue((int) newValue.getWidth());
				txtMosaicHeight.setValue((int) newValue.getHeight());
			}
		});

		txtMosaicTileWidth.valueProperty().addListener(new ChangeListener<Number>()
		{

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue,
					Number newValue)
			{
				txtMosaicHcount.setValue(txtMosaicWidth.getValue() / newValue.intValue());
			}
		});

		txtMosaicTileHeight.valueProperty().addListener(new ChangeListener<Number>()
		{

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue,
					Number newValue)
			{
				txtMosaicVcount.setValue(txtMosaicHeight.getValue() / newValue.intValue());
			}
		});

		txtMosaicHcount.valueProperty().addListener(new ChangeListener<Number>()
		{

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue,
					Number newValue)
			{
				txtMosaicTileWidth.setValue(txtMosaicWidth.getValue() / newValue.intValue());
			}
		});

		txtMosaicVcount.valueProperty().addListener(new ChangeListener<Number>()
		{

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue,
					Number newValue)
			{
				txtMosaicTileHeight.setValue(txtMosaicHeight.getValue() / newValue.intValue());
			}
		});

		CheckBox cbOverlay = CheckBoxBuilder.create().text("Overlay").selected(true).build();

		Label lblMosaicAlpha = LabelBuilder.create().text("Color change rate:").build();
		final DoubleField txtMosaicAlpha = new DoubleField(0.0, 1.0, 0.5);
		txtMosaicAlpha.setPromptText("Alpha");
		txtMosaicAlpha.disableProperty().bind(cbOverlay.selectedProperty().not());

		cbOverlay.selectedProperty().addListener(new ChangeListener<Boolean>()
		{

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
					Boolean newValue)
			{
				if (newValue.booleanValue() == false) txtMosaicAlpha.setValue(0.0);
			}
		});

		final CheckBox cbDontRepeat = CheckBoxBuilder
			.create()
			.text("Try not to repeat tiles")
			.selected(false)
			.build();

		final ImageView ivMosaic = ImageViewBuilder.create().preserveRatio(true).build();
		final ScrollPane spMosaic = ScrollPaneBuilder
			.create()
			.content(ivMosaic)
			.fitToHeight(true)
			.fitToWidth(true)
			.maxHeight(Double.MAX_VALUE)
			.maxWidth(Double.MAX_VALUE)
			.pannable(true)
			.build();
		final Slider sliderMosaic = SliderBuilder
			.create()
			.orientation(Orientation.HORIZONTAL)
			.snapToTicks(true)
			.majorTickUnit(0.9)
			.minorTickCount(5)
			.min(0.1)
			.max(3.0)
			.value(1.0)
			.showTickLabels(true)
			.showTickMarks(true)
			.maxWidth(Double.MAX_VALUE)
			.build();
		ivMosaic.fitWidthProperty().bind(new DoubleBinding()
		{
			{
				super.bind(sliderMosaic.valueProperty());
			}

			@Override
			protected double computeValue()
			{
				Image img = ivMosaic.getImage();
				if (img == null) return 0.0;
				return sliderMosaic.getValue() * img.getWidth();
			}
		});
		ivSrc.fitHeightProperty().bind(new DoubleBinding()
		{
			{
				super.bind(sliderMosaic.valueProperty());
			}

			@Override
			protected double computeValue()
			{
				Image img = ivMosaic.getImage();
				if (img == null) return 0.0;
				return sliderMosaic.getValue() * img.getHeight();
			}
		});

		ToggleGroup tgRb = new ToggleGroup();
		final RadioButton rbBmt = RadioButtonBuilder
			.create()
			.text("Use Best Maching Tile")
			.selected(true)
			.toggleGroup(tgRb)
			.build();
		RadioButton rbUrd = RadioButtonBuilder
			.create()
			.text("Use Uniform Random Distribution")
			.selected(false)
			.toggleGroup(tgRb)
			.build();

		cbDontRepeat.disableProperty().bind(rbBmt.selectedProperty().not());

		Button btnMosaicSave = ButtonBuilder
			.create()
			.text("Save")
			.onAction(new EventHandler<ActionEvent>()
			{

				@Override
				public void handle(ActionEvent event)
				{
					Image img = ivMosaic.getImage();
					File f = FileChooserBuilder
						.create()
						.initialDirectory(initialDirectory)
						.extensionFilters(new ExtensionFilter("Images", "*.png"))
						.build()
						.showSaveDialog(primaryStageF);
					if (f != null) try
					{
						ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", f);
					} catch (IOException e)
					{}
				}
			})
			.build();
		btnMosaicSave.disableProperty().bind(new BooleanBinding()
		{
			{
				super.bind(ivMosaic.imageProperty());
			}

			@Override
			protected boolean computeValue()
			{
				return ivMosaic.getImage() == null ? true : false;
			}
		});
		Button btnMosaicBuild = ButtonBuilder
			.create()
			.text("Generate")
			.onAction(new EventHandler<ActionEvent>()
			{

				@Override
				public void handle(ActionEvent event)
				{
					Worker<Void> w = new Task<Void>()
					{

						@Override
						protected Void call() throws Exception
						{
							updateProgress(0, txtMosaicHcount.getValue());
							// collect parameters
							Image imgSrc = ivSrc.getImage();

							// generate mosaic

							// calculate more parameters
							int srcTileWidth = (int) (imgSrc.getWidth() / txtMosaicHcount
								.getValue());
							int srcTileHeight = (int) (imgSrc.getHeight() / txtMosaicVcount
								.getValue());
							int size = tpTiles.getChildren().size();

							// resize tiles and calculate average color
							final TreeSet<MosaicTile> mtilesTree = new TreeSet<>();
							final List<MosaicTile> mtilesList = new ArrayList<>(size);
							ImageView tmpView = ImageViewBuilder
								.create()
								.fitHeight(txtMosaicTileHeight.getValue())
								.fitWidth(txtMosaicTileWidth.getValue())
								.build();
							if (rbBmt.isSelected())
								for (Node iv : tpTiles.getChildren())
								{
									Image img = ((ImageView) iv).getImage();
									tmpView.setImage(img);
									img = tmpView.snapshot(null, null);
									mtilesTree.add(new MosaicTile(img));
								}
							else
								for (Node iv : tpTiles.getChildren())
								{
									Image img = ((ImageView) iv).getImage();
									tmpView.setImage(img);
									img = tmpView.snapshot(null, null);
									mtilesList.add(new MosaicTile(img));
								}

							// mosaic image
							WritableImage mosaicPhoto = new WritableImage(
									txtMosaicWidth.getValue(), txtMosaicHeight.getValue());
							PixelWriter pw = mosaicPhoto.getPixelWriter();
							PixelReader pr = imgSrc.getPixelReader();
							WritableImage srcTile = new WritableImage(srcTileWidth, srcTileHeight);
							PixelWriter stpw = srcTile.getPixelWriter();

							MosaicTile[][] mosaicPhotoTiles = new MosaicTile[txtMosaicHcount
								.getValue()][txtMosaicVcount.getValue()];
							boolean turn = false; // to flip boolean value
													// when all tiles are
													// used
							Random random = new Random(0);

							// for each tile of the mosaic
							for (int i = 0; i < txtMosaicHcount.getValue(); i++)
							{
								for (int j = 0; j < txtMosaicVcount.getValue(); j++)
								{
									// get source tile
									stpw.setPixels(0, 0, srcTileWidth, srcTileHeight, pr, i
											* srcTileWidth, j * srcTileHeight);

									// calculate average color
									MosaicTile srcTileM = new MosaicTile(srcTile);

									// find chosen tile
									MosaicTile chosenTile;
									if (rbBmt.isSelected())
									{
										// get best matching mosaic tile
										chosenTile = mtilesTree.floor(srcTileM);
										if (chosenTile == null)
											chosenTile = mtilesTree.ceiling(srcTileM);
										if (cbDontRepeat.isSelected())
										{
											if (chosenTile.used == turn)
											{
												chosenTile.used = !turn;
											} else
											{
												chosenTile = Utils.getUnusedBmmt(
																					chosenTile,
																					mtilesTree,
																					turn);
												if (chosenTile == null)
												{
													// all tiles were used
													turn = !turn;
													chosenTile = mtilesTree.floor(srcTileM);
													if (chosenTile == null)
														chosenTile = mtilesTree.ceiling(srcTileM);
												} else
												{
													chosenTile.used = !turn;
												}
											}
										}
									} else
									{
										chosenTile = mtilesList.get(random.nextInt(size));
									}
									mosaicPhotoTiles[i][j] = chosenTile;

									// overlay
									PixelReader bmmtpr;
									if (txtMosaicAlpha.getValue() > 0.0000000000001)
									{

										Argb c2 = new Argb(srcTileM.colorAvg);
										Image tmpImg = Utils.overlay(
																		chosenTile.img,
																		c2.a,
																		c2.r,
																		c2.g,
																		c2.b,
																		txtMosaicAlpha.getValue());
										bmmtpr = tmpImg.getPixelReader();
									} else
									{
										bmmtpr = chosenTile.img.getPixelReader();
									}
									// add bmmt to mosaic photo
									pw
										.setPixels(
													i * txtMosaicTileWidth.getValue(),
													j * txtMosaicTileHeight.getValue(),
													(int) chosenTile.img.getWidth(),
													(int) chosenTile.img.getHeight(),
													bmmtpr,
													0,
													0);

								} // end for j
							} // end for i
							ivMosaic.setImage(mosaicPhoto);
							return null;
						} // end call
					}; // end task
					ProgressBar pb = new ProgressBar();
					pb.progressProperty().bind(w.progressProperty());
					// new Thread((Runnable) w).start();
					((Task<Void>) w).run();
				} // end handler
			})
			.build();
		btnMosaicBuild.disableProperty().bind(new BooleanBinding()
		{
			{
				super.bind(ivSrc.imageProperty(), tpTiles.getChildren());
			}

			@Override
			protected boolean computeValue()
			{
				return ivSrc.getImage() == null ? true : tpTiles.getChildren().isEmpty() ? true
						: false;
			}
		});

		VBox layoutSrc = VBoxBuilder.create().spacing(5.0).children(btnSrc, sliderSrc).build();

		VBox layoutTiles = VBoxBuilder
			.create()
			.spacing(5.0)
			.children(btnTilesAdd, btnTilesClear)
			.build();

		HBox layoutMScale = HBoxBuilder.create().children(lblMosaicScale, txtMosaicScale).build();
		HBox.setHgrow(txtMosaicScale, Priority.ALWAYS);

		HBox layoutMWidth = HBoxBuilder.create().children(lblMosaicWidth, txtMosaicWidth).build();
		HBox.setHgrow(txtMosaicWidth, Priority.ALWAYS);

		HBox layoutMHeight = HBoxBuilder
			.create()
			.children(lblMosaicHeight, txtMosaicHeight)
			.build();
		HBox.setHgrow(txtMosaicHeight, Priority.ALWAYS);

		HBox layoutMHCount = HBoxBuilder
			.create()
			.children(lblMosaicHcount, txtMosaicHcount)
			.build();
		HBox.setHgrow(txtMosaicHcount, Priority.ALWAYS);

		HBox layoutMVcount = HBoxBuilder
			.create()
			.children(lblMosaicVcount, txtMosaicVcount)
			.build();
		HBox.setHgrow(txtMosaicVcount, Priority.ALWAYS);

		HBox layoutMTW = HBoxBuilder
			.create()
			.children(lblMosaicTileWidth, txtMosaicTileWidth)
			.build();
		HBox.setHgrow(txtMosaicTileWidth, Priority.ALWAYS);

		HBox layoutMTH = HBoxBuilder
			.create()
			.children(lblMosaicTileHeight, txtMosaicTileHeight)
			.build();
		HBox.setHgrow(txtMosaicTileHeight, Priority.ALWAYS);

		HBox layoutAlpha = HBoxBuilder.create().children(lblMosaicAlpha, txtMosaicAlpha).build();
		HBox.setHgrow(txtMosaicAlpha, Priority.ALWAYS);

		VBox layoutMosaic = VBoxBuilder.create().spacing(5.0).children(
																		btnMosaicBuild,
																		btnMosaicSave,
																		sliderMosaic,
																		layoutMScale,
																		layoutMWidth,
																		layoutMHeight,
																		layoutMTW,
																		layoutMTH,
																		layoutMHCount,
																		layoutMVcount,
																		cbOverlay,
																		layoutAlpha,
																		cbDontRepeat,
																		rbBmt,
																		rbUrd).build();

		tabSrc.setContent(spSrc);
		tabTiles.setContent(spTiles);
		tabMosaic.setContent(spMosaic);

		ttpSource.setContent(layoutSrc);
		ttpTiles.setContent(layoutTiles);
		ttpMosaic.setContent(layoutMosaic);

		ttpSource.setOnMouseClicked(new EventHandler<MouseEvent>()
		{

			@Override
			public void handle(MouseEvent event)
			{
				tabs.getSelectionModel().select(tabSrc);
			}
		});

		ttpTiles.setOnMouseClicked(new EventHandler<MouseEvent>()
		{

			@Override
			public void handle(MouseEvent event)
			{
				tabs.getSelectionModel().select(tabTiles);
			}
		});

		ttpMosaic.setOnMouseClicked(new EventHandler<MouseEvent>()
		{

			@Override
			public void handle(MouseEvent event)
			{
				tabs.getSelectionModel().select(tabMosaic);
			}
		});

		SplitPane container = SplitPaneBuilder
			.create()
			.items(accordionSideBar, tabs)
			.dividerPositions(new double[]
			{ 0.25 })
			.build();

		primaryStage.setTitle("Mosaicify");
		primaryStage.setScene(SceneBuilder.create().root(
															ScrollPaneBuilder
																.create()
																.content(container)
																.fitToHeight(true)
																.fitToWidth(true)
																.prefHeight(500)
																.prefWidth(1000)
																.build()).build());
		primaryStage.sizeToScene();
		primaryStage.show();

	}

	public static void main(String[] args)
	{
		Application.launch(args);
	}
}

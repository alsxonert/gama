/*********************************************************************************************
 *
 * 'ImageLayer.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.outputs.layers;

import msi.gama.common.interfaces.IGraphics;
import msi.gama.metamodel.shape.Envelope3D;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.runtime.IScope;
import msi.gama.util.file.GamaGridFile;
import msi.gama.util.file.GamaImageFile;
import msi.gaml.statements.draw.FileDrawingAttributes;

/**
 * Written by drogoul Modified on 9 nov. 2009
 *
 * @todo Description
 *
 */
public class ImageLayer extends AbstractLayer {

	GamaImageFile file = null;
	GamaGridFile grid = null;
	private String imageFileName = "";
	Envelope3D env;

	public ImageLayer(final IScope scope, final ILayerStatement layer) {
		super(layer);
		buildImage(scope);
	}

	protected Envelope3D buildImage(final IScope scope) {
		final String newImage = ((ImageLayerStatement) definition).getImageFileName();
		if (imageFileName != null && imageFileName.equals(newImage)) { return env; }
		imageFileName = newImage;
		if (imageFileName == null || imageFileName.length() == 0) {
			file = null;
			grid = null;
		} else {
			file = new GamaImageFile(scope, imageFileName);
			env = file.getGeoDataFile(scope) == null ? scope.getSimulation().getEnvelope()
					: file.computeEnvelope(scope);
		}
		return env;
	}

	@Override
	public void privateDrawDisplay(final IScope scope, final IGraphics dg) {
		buildImage(scope);
		if (file == null) { return; }
		final FileDrawingAttributes attributes = new FileDrawingAttributes(null);
		if (env != null) {
			final GamaPoint loc = new GamaPoint(env.getMinX(), env.getMinY());
			attributes.setLocation(loc);
			attributes.setSize(new GamaPoint(env.getWidth(), env.getHeight()));
		}
		dg.drawFile(file, attributes);
	}

	@Override
	public String getType() {
		return "Image layer";
	}

}

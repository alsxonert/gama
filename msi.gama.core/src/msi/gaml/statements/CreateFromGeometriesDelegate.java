/**
 * Created by drogoul, 27 mai 2015
 * 
 */
package msi.gaml.statements;

import java.util.*;

import com.vividsolutions.jts.geom.Geometry;

import msi.gama.common.interfaces.*;
import msi.gama.database.sql.SqlConnection;
import msi.gama.metamodel.shape.GamaShape;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.*;
import msi.gama.util.file.*;
import msi.gama.util.matrix.IMatrix;
import msi.gaml.descriptions.IExpressionDescription;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import msi.gaml.types.Types;

/**
 * Class CreateFromDatabaseDelegate.
 *
 * @author drogoul
 * @since 27 mai 2015
 *
 */
public class CreateFromGeometriesDelegate implements ICreateDelegate {

	/**
	 * Method acceptSource()
	 * 
	 * @see msi.gama.common.interfaces.ICreateDelegate#acceptSource(java.lang.Object)
	 */
	@Override
	public boolean acceptSource(Object source) {

		return ( source instanceof IList && ((IList) source).getType().getContentType().isAssignableFrom(Types.GEOMETRY) ||
				source instanceof GamaShapeFile || source instanceof GamaOsmFile );
	}

	/**
	 * Method createFrom() Method used to read initial values and attributes
	 * from a CSV values describing a synthetic population
	 * 
	 * @author Alexis Drogoul
	 * @since 04-09-2012
	 * @see msi.gama.common.interfaces.ICreateDelegate#createFrom(msi.gama.runtime.IScope,
	 *      java.util.List, int, java.lang.Object)
	 */
	@Override
	public boolean createFrom(IScope scope, List<Map> inits, Integer max,
			Object input, Arguments init, CreateStatement statement) {
		IAddressableContainer<Integer, GamaShape, Integer, GamaShape> container = (IAddressableContainer<Integer, GamaShape, Integer, GamaShape>) input;
		final int num = max == null ? container.length(scope) : Math.min(container.length(scope), max);
		for ( int i = 0; i < num; i++ ) {
			final GamaShape g = container.get(scope, i);
			final Map map = g.getOrCreateAttributes();
			// The shape is added to the initial values
			map.put(IKeyword.SHAPE, g);
			// GIS attributes are mixed with the attributes of agents
			statement.fillWithUserInit(scope, map);
			inits.add(map);
		}
		return true;
	}


}
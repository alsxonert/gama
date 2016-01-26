package msi.gama.metamodel.topology.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import msi.gama.metamodel.population.IPopulation;
import msi.gama.metamodel.topology.grid.GamaSpatialMatrix.GridPopulation;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.matrix.IMatrix;
import msi.gaml.operators.Cast;

public class GridDiffuserWithMatrix {
	
	private class PairVarGrid {
		String m_var_name;
		String m_grid_name;
		int m_nbRows;
		int m_nbCols;
		boolean m_is_torus;
		
		public PairVarGrid(IScope scope, String var_name, GridPopulation pop) {
			m_var_name = var_name;
			m_grid_name = pop.getName();
			m_nbRows = ((IGrid) pop.getTopology().getPlaces()).getRows(scope);
			m_nbCols = ((IGrid) pop.getTopology().getPlaces()).getCols(scope);
			m_is_torus = pop.getTopology().isTorus();
		}
		
	    @Override
	    public int hashCode() {
	    	return m_var_name.hashCode()+m_grid_name.hashCode();
	    }

	    @Override
	    public boolean equals(Object obj) {
	       if (!(obj instanceof PairVarGrid))
	            return false;
	        if (obj == this)
	            return true;
	        
	        PairVarGrid otherGrid = (PairVarGrid)obj;
	        if ((otherGrid.m_var_name.equals(m_var_name)) && (otherGrid.m_grid_name.equals(m_grid_name))) {
				return true;
			}
			return false;
	    }
	}
	
	private class GridDiffusion {
		public String m_species_diffu;
		public boolean m_method_diffu = true;
		public double[][] m_mask, m_mat_diffu;
		public List<Integer> m_agents;
		public IScope m_scope;
		public IPopulation m_pop;
		
		public GridDiffusion(IScope scope, String var_diffu, String species_diffu, boolean method_diffu, double[][] mat_diffu, double[][] mask, List<Integer> agents) {
			m_scope = scope;
			m_species_diffu = species_diffu;
			m_method_diffu = method_diffu;
			m_mat_diffu = mat_diffu;
			m_mask = mask;
			m_agents = agents;
		}
	}
	
	public boolean compareArrays(double[][] array1, double[][] array2) {
        boolean b = true;
        if (array1 != null && array2 != null){
          if (array1.length != array2.length)
              b = false;
          else
              for (int i = 0; i < array2.length; i++) {
            	  if (array1[i].length != array2[i].length)
            	  {
            		  b = false;
            	  }
            	  else {
            		  for (int j = 0; j < array2[i].length; j++) {
                          if (array2[i][j] != array1[i][j]) {
                              b = false;    
                          } 
            		  }
            	  }                
            }
        }else{
          b = false;
        }
        return b;
    }
	
	protected final Map<PairVarGrid, List<GridDiffusion>> m_diffusions = new HashMap<PairVarGrid,List<GridDiffusion>>();
	
	public void addDiffusion(IScope scope, String var_diffu, GridPopulation pop, boolean method_diffu, double[][] mat_diffu, double[][] mask, List<Integer> agents) {
		GridDiffusion newGridDiff = new GridDiffusion(scope, var_diffu, pop.getName(), method_diffu, mat_diffu, mask, agents);
		PairVarGrid keyValue = new PairVarGrid(scope, var_diffu, pop);
		if (m_diffusions.containsKey(keyValue))
		{
			List<GridDiffusion> listWithSameVar = new ArrayList<GridDiffusion>();
			listWithSameVar = m_diffusions.get(keyValue);
			// try to mix diffusions if possible
			for (int i = 0; i<listWithSameVar.size(); i++) {
				GridDiffusion gridToAnalyze = listWithSameVar.get(i);
				if (gridToAnalyze != newGridDiff
						&& gridToAnalyze.m_method_diffu == newGridDiff.m_method_diffu
//						&& gridToAnalyze.m_agents.equals(newGridDiff.m_agents)
						&& compareArrays(gridToAnalyze.m_mask,newGridDiff.m_mask)) {
					// we can add the two diffusion matrix
					listWithSameVar.remove(gridToAnalyze);
					int iiLength = gridToAnalyze.m_mat_diffu.length;
					int cellNbiiToAddToGridToAnalyze = 0;
					int cellNbiiToAddToNewGrid = 0;
					if (gridToAnalyze.m_mat_diffu.length < newGridDiff.m_mat_diffu.length) {
						iiLength = newGridDiff.m_mat_diffu.length;
						cellNbiiToAddToGridToAnalyze = (newGridDiff.m_mat_diffu.length-gridToAnalyze.m_mat_diffu.length)/2;
					}
					else if (gridToAnalyze.m_mat_diffu.length > newGridDiff.m_mat_diffu.length) {
						iiLength = newGridDiff.m_mat_diffu.length;
						cellNbiiToAddToNewGrid = (gridToAnalyze.m_mat_diffu.length-newGridDiff.m_mat_diffu.length)/2;
					}
					int jjLength = gridToAnalyze.m_mat_diffu[0].length;
					int cellNbjjToAddToGridToAnalyze = 0;
					int cellNbjjToAddToNewGrid = 0;
					if (gridToAnalyze.m_mat_diffu[0].length < newGridDiff.m_mat_diffu[0].length) {
						jjLength = newGridDiff.m_mat_diffu[0].length;
						cellNbjjToAddToGridToAnalyze = (newGridDiff.m_mat_diffu[0].length-gridToAnalyze.m_mat_diffu[0].length)/2;
					}
					else if (gridToAnalyze.m_mat_diffu[0].length > newGridDiff.m_mat_diffu[0].length) {
						jjLength = gridToAnalyze.m_mat_diffu[0].length;
						cellNbjjToAddToNewGrid = (gridToAnalyze.m_mat_diffu[0].length-newGridDiff.m_mat_diffu[0].length)/2;
					}
					double[][] newMat = new double[iiLength][jjLength];
					for (int ii = 0 ; ii < iiLength ; ii++) {
						for (int jj = 0 ; jj < jjLength ; jj++) {
							double result = 0;
							int indexiForGridToAnalyze = gridToAnalyze.m_mat_diffu.length - iiLength + ii + cellNbiiToAddToGridToAnalyze;
							int indexjForGridToAnalyze = gridToAnalyze.m_mat_diffu[0].length - jjLength + jj + cellNbjjToAddToGridToAnalyze;
							if ((indexiForGridToAnalyze >= 0) 
									&& (indexiForGridToAnalyze < gridToAnalyze.m_mat_diffu.length)
									&& (indexjForGridToAnalyze >= 0)
									&& (indexjForGridToAnalyze < gridToAnalyze.m_mat_diffu[0].length)) {
								result += gridToAnalyze.m_mat_diffu[indexiForGridToAnalyze][indexjForGridToAnalyze];
							}
							int indexiForNewGridDiff = newGridDiff.m_mat_diffu.length - iiLength + ii + cellNbiiToAddToNewGrid;
							int indexjForNewGridDiff = newGridDiff.m_mat_diffu[0].length - jjLength + jj + cellNbjjToAddToNewGrid;
							if ((indexiForNewGridDiff >= 0)
									&& (indexiForNewGridDiff < newGridDiff.m_mat_diffu.length)
									&& (indexjForNewGridDiff >= 0)
									&& (indexjForNewGridDiff < newGridDiff.m_mat_diffu[0].length)) {
								result += newGridDiff.m_mat_diffu[indexiForNewGridDiff][indexjForNewGridDiff];
							}
							newMat[ii][jj] = result;
						}
					}
					newGridDiff.m_mat_diffu = newMat;
					// we add the two "agents" list also
					List<Integer> newList = gridToAnalyze.m_agents;
					for(int listidx = 0; listidx < newGridDiff.m_agents.size(); listidx++) {
						if (!newList.contains(newGridDiff.m_agents.get(listidx))) {
							newList.add(newGridDiff.m_agents.get(listidx));
						}
					}
					newGridDiff.m_agents = newList;
				}
			}
			listWithSameVar.add(newGridDiff);
			m_diffusions.put(keyValue, listWithSameVar);
		}
		else {
			List<GridDiffusion> valueToAdd = new ArrayList<GridDiffusion>();
			valueToAdd.add(newGridDiff);
			m_diffusions.put(keyValue, valueToAdd);
		}
	}
	
	private boolean is_torus;
	private String var_diffu;
	// true for convolution, false for dot_product
	private boolean method_diffu = true;
	boolean m_initialized = false;
	double[][] mask, mat_diffu;
	List<Integer> agents; // list of agents which have a non null value
//	List<Integer> list_of_agents_who_will_change;
	IScope scope;
	
	double[] input, output;
	int nbRows, nbCols;
	
	public GridDiffuserWithMatrix() {
	}
	
	public void loadGridProperties(PairVarGrid pairVarGrid) {
		nbRows = pairVarGrid.m_nbRows;
		nbCols = pairVarGrid.m_nbCols;
		is_torus = pairVarGrid.m_is_torus;
		var_diffu = pairVarGrid.m_var_name;
	}
	
	public void loadDiffProperties(GridDiffusion gridDiff) {
		mat_diffu = gridDiff.m_mat_diffu;
		mask = gridDiff.m_mask;
		method_diffu = gridDiff.m_method_diffu;
		scope = gridDiff.m_scope;
//		for (int i = 0 ; i<gridDiff.m_agents.size(); i++) {
//			if (!list_of_agents_who_will_change.contains(gridDiff.m_agents.get(i))) {
//				list_of_agents_who_will_change.add(gridDiff.m_agents.get(i));
//			}
//		}
		
		agents = gridDiff.m_agents;
//		if (agents == null) {
//			agents = new ArrayList<Integer>();
//			for (int i = 0; i < nbRows*nbCols; i++) {
//				agents.add(i);
//			}
//		}
		IPopulation pop = scope.getAgentScope().getPopulationFor(gridDiff.m_species_diffu);
		
		for ( int i = 0; i < input.length; i++ ) {
			if (agents == null || agents.contains(i))
				input[i] = Cast.asFloat(scope, pop.get(scope, i).getDirectVarValue(scope, var_diffu));
			else 
				input[i] = 0;
			
		}
		

	}

	public void doDiffusion1() {

		int kRows = mat_diffu.length;
		int kCols = mat_diffu[0].length;

		int kCenterX = kRows / 2;
		int kCenterY = kCols / 2;
		int mm = 0, nn = 0, ii = 0, jj = 0;
		
		for (int agenti = 0; agenti < agents.size(); agenti++) {
			int i = agents.get(agenti) - (int)(agents.get(agenti)/nbCols)*nbCols; // col number
			int j = (int)(agents.get(agenti)/nbCols); // row number



//		for ( int i = 0; i < nbRows; ++i ) // rows
//		{
//			for ( int j = 0; j < nbCols; ++j ) // columns
//			{
				// sum = 0; // init to 0 before sum
				for ( int m = 0; m < kRows; ++m ) // kernel rows
				{
					mm = kRows - 1 - m; // row index of flipped kernel
					for ( int n = 0; n < kCols; ++n ) // kernel columns
					{
						nn = kCols - 1 - n; // column index of flipped kernel
						// index of input signal, used for checking boundary
						ii = i + m - kCenterX;
						jj = j + n - kCenterY;
						// ignore input samples which are out of bound
						if ( is_torus ) {
							if ( ii < 0 ) {
								ii = nbRows + ii;
							} else if ( ii >= nbRows ) {
								ii = ii - nbRows;
							}

							if ( jj < 0 ) {
								jj = nbCols + jj;
							} else if ( jj >= nbCols ) {
								jj = jj - nbCols;
							}
						}
						if ( ii >= 0 && ii < nbCols && jj >= 0 && jj < nbRows) {
							double mask_current = mask != null ? mask[i][j] < -1 ? 0 : 1 : 1;
							output[j * nbCols + i] += input[jj * nbCols + ii] * mat_diffu[mm][nn] * mask_current;
//							if (!list_of_agents_who_will_change.contains(ii*nbRows+jj)) {
//								list_of_agents_who_will_change.add(ii*nbRows+jj);
//							}
						}
					}
				}
//			}
		}
	}

	public void doDiffusion2() {

		int kRows = mat_diffu.length;
		int kCols = mat_diffu[0].length;

		int xcenter = kRows / 2;
		int ycenter = kCols / 2;

		for ( int i = 0; i < nbRows; i++ ) {
			for ( int j = 0; j < nbCols; j++ ) {

				int um = 0;
				for ( int uu = i - xcenter; uu <= i + xcenter; uu++ ) {
					int vm = 0;
					for ( int vv = j - ycenter; vv <= j + ycenter; vv++ ) {
						int u = uu;
						int v = vv;
						double mask_current = mask != null ? mask[i][j] < -1 ? 0 : 1 : 1;
						if ( is_torus ) {
							if ( u < 0 ) {
								u = nbRows + u;
							} else if ( u >= nbRows ) {
								u = u - nbRows;
							}

							if ( v < 0 ) {
								v = nbCols + v;
							} else if ( v >= nbCols ) {
								v = v - nbCols;
							}
						} else if ( u >= 0 && v >= 0 & v < nbCols & u < nbRows ) {
							output[u * nbCols + v] += input[i * nbCols + j] * mat_diffu[um][vm] * mask_current;
						}

						vm++;
					}
					um++;

				}
			}
		}

	}

	public void finishDiffusion(final IScope scope, final IPopulation pop) {
		for ( int i = 0; i < output.length; i++ ) {
//			if (list_of_agents_who_will_change == null || list_of_agents_who_will_change.contains(i))
					pop.get(scope, i).setDirectVarValue(scope, var_diffu, output[i]);
		}
	}

	public double[][] translateMatrix(final IScope scope, final IMatrix mm) {
		if ( mm == null ) { return null; }
		int rows = mm.getRows(scope);
		int cols = mm.getCols(scope);
		double[][] res = new double[rows][cols];
		for ( int i = 0; i < rows; i++ ) {
			for ( int j = 0; j < cols; j++ ) {
				res[i][j] = Cast.asFloat(scope, mm.get(scope, i, j));
			}
		}
		return res;
	}

	public Object diffuse2(/*final IScope scope*/) throws GamaRuntimeException {
		
		System.out.println("EXECUTE DIFFUSE !!!");
//		list_of_agents_who_will_change = new ArrayList<Integer>();
		
		Set<PairVarGrid> keySet = m_diffusions.keySet();
		Iterator<PairVarGrid> iterator = keySet.iterator();
		
		while (iterator.hasNext()) {
			System.out.println("1");
			PairVarGrid pairVarGrid = iterator.next();
			List<GridDiffusion> listGridDiffu = m_diffusions.get(pairVarGrid);
			loadGridProperties(pairVarGrid);
			Iterator<GridDiffusion> gridDiffIterator = listGridDiffu.iterator();
			input = new double[pairVarGrid.m_nbCols*pairVarGrid.m_nbRows];
			output = new double[pairVarGrid.m_nbCols*pairVarGrid.m_nbRows];
			Arrays.fill(output, 0d);
			while (gridDiffIterator.hasNext()) {
				System.out.println("2");
				GridDiffusion gridDiffusion = gridDiffIterator.next();
				loadDiffProperties(gridDiffusion);
				if (!method_diffu) {
					doDiffusion2();
				}
				else {
					doDiffusion1();
				}
			}
			IPopulation pop = scope.getAgentScope().getPopulationFor(pairVarGrid.m_grid_name);
			finishDiffusion(scope, pop);
		}
		m_diffusions.clear();
		return null;
	}

}

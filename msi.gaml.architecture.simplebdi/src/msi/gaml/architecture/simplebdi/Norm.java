package msi.gaml.architecture.simplebdi;

import msi.gama.common.interfaces.IValue;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.var;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

@vars ({ @var (
		name = "name",
		type = IType.STRING,
		doc = @doc ("The name of this norm")),
		@var (
				name = SimpleBdiPlanStatement.INTENTION,
				type = IType.NONE,
				doc = @doc ("A string representing the current intention of this BDI plan")),
		@var (
				name = SimpleBdiArchitecture.FINISHEDWHEN,
				type = IType.STRING),
		@var (
				name = SimpleBdiArchitecture.INSTANTANEAOUS,
				type = IType.STRING)
		/*
		 * @var(name = "value", type = IType.NONE),
		 * 
		 * @var(name = "parameters", type = IType.MAP),
		 */
		// @var(name = "values", type = IType.MAP), @var(name = "priority", type
		// = IType.FLOAT),
		// @var(name = "date", type = IType.FLOAT), @var(name = "subintentions",
		// type = IType.LIST),
		// @var(name = "on_hold_until", type = IType.NONE)
})

//Classe qui permet de définir les normes comme type, contenant le norm statement, sur l'exemple des plans
public class Norm implements IValue{

	private NormStatement normStatement;
	
	@Override
	public String serialize(boolean includingBuiltIn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IType<?> getType() {
		return Types.get(NormType.id);
//		return null;
	}

	@Override
	public String stringValue(IScope scope) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IValue copy(IScope scope) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

}

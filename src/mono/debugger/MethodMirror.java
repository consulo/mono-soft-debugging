package mono.debugger;

import org.jetbrains.annotations.NotNull;
import mono.debugger.protocol.Method_GetDeclarationType;
import mono.debugger.protocol.Method_GetName;
import mono.debugger.protocol.Method_GetParamInfo;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class MethodMirror extends MirrorWithIdAndName implements MirrorWithId
{
	private TypeMirror myDeclarationType;
	private Method_GetParamInfo myParamInfo;

	public MethodMirror(@NotNull VirtualMachine aVm, long id)
	{
		super(aVm, id);
	}

	public Method_GetParamInfo paramInfo()
	{
		if(myParamInfo != null)
		{
			return myParamInfo;
		}
		try
		{
			return myParamInfo = Method_GetParamInfo.process(vm, this);
		}
		catch(JDWPException e)
		{
			throw e.toJDIException();
		}
	}

	public MethodParameterMirror[] parameters()
	{
		return paramInfo().parameters;
	}

	public int genericParameterCount()
	{
		return paramInfo().genericParameterCount;
	}

	@NotNull
	@Override
	protected String nameImpl() throws JDWPException
	{
		return Method_GetName.process(vm, this).name;
	}

	@NotNull
	public TypeMirror declaringType()
	{
		if(myDeclarationType != null)
		{
			return myDeclarationType;
		}

		try
		{
			return myDeclarationType = Method_GetDeclarationType.process(vm, this).declarationType;
		}
		catch(JDWPException e)
		{
			throw e.toJDIException();
		}
	}
}

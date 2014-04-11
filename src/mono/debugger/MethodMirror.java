package mono.debugger;

import org.jetbrains.annotations.NotNull;
import mono.debugger.protocol.Method_GetDebugInfo;
import mono.debugger.protocol.Method_GetDeclarationType;
import mono.debugger.protocol.Method_GetName;
import mono.debugger.protocol.Method_GetParamInfo;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class MethodMirror extends MirrorWithIdAndName implements MirrorWithId
{
	private static final Method_GetDebugInfo.Entry[] EMPTY_ENTRIES = new Method_GetDebugInfo.Entry[0];

	private TypeMirror myDeclarationType;
	private Method_GetParamInfo myParamInfo;

	private int myMaxCodeIndex = Integer.MIN_VALUE;
	private Method_GetDebugInfo.Entry[] myDebugEntries;

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

	public int maxCodeIndex()
	{
		debugInfo();
		return myMaxCodeIndex;
	}

	@NotNull
	public Method_GetDebugInfo.Entry[] debugInfo()
	{
		if(myMaxCodeIndex == Integer.MIN_VALUE)
		{
			try
			{
				Method_GetDebugInfo process = Method_GetDebugInfo.process(vm, this);
				myDebugEntries = process.entries;
				myMaxCodeIndex = process.maxIndex;
			}
			catch(JDWPException e)
			{
				myMaxCodeIndex = -1;
				myDebugEntries = EMPTY_ENTRIES;
			}
		}
		return myDebugEntries;
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

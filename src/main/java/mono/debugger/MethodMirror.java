package mono.debugger;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import consulo.internal.dotnet.asm.signature.MethodAttributes;
import mono.debugger.protocol.Method_GetCustomAttributes;
import mono.debugger.protocol.Method_GetDebugInfo;
import mono.debugger.protocol.Method_GetDeclarationType;
import mono.debugger.protocol.Method_GetInfo;
import mono.debugger.protocol.Method_GetLocalsInfo;
import mono.debugger.protocol.Method_GetName;
import mono.debugger.protocol.Method_GetParamInfo;
import mono.debugger.protocol.VirtualMachine_InvokeMethod;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class MethodMirror extends CustomAttributeMirrorOwner implements MirrorWithId, ModifierOwner
{
	private static final Method_GetDebugInfo.Entry[] EMPTY_ENTRIES = new Method_GetDebugInfo.Entry[0];

	private TypeMirror myDeclarationType;
	private Method_GetParamInfo myParamInfo;
	private Method_GetInfo myInfo;
	private Method_GetLocalsInfo myLocalsInfo;

	private int myMaxCodeIndex = Integer.MIN_VALUE;
	private Method_GetDebugInfo.Entry[] myDebugEntries;

	public MethodMirror(@Nonnull VirtualMachine aVm, int id)
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
			throw e.asUncheckedException();
		}
	}

	private Method_GetInfo info()
	{
		if(myInfo != null)
		{
			return myInfo;
		}
		try
		{
			return myInfo = Method_GetInfo.process(vm, this);
		}
		catch(JDWPException e)
		{
			throw e.asUncheckedException();
		}
	}

	private Method_GetLocalsInfo localsInfo()
	{
		if(myLocalsInfo != null)
		{
			return myLocalsInfo;
		}
		try
		{
			return myLocalsInfo = Method_GetLocalsInfo.process(vm, this);
		}
		catch(JDWPException e)
		{
			throw e.asUncheckedException();
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

	@Nullable
	public TypeMirror returnType()
	{
		return paramInfo().returnType;
	}

	@Nullable
	@Deprecated
	public Value<?> invoke(@Nonnull ThreadMirror threadMirror, @Nullable Value<?> thisObject, Value<?>... arguments)
	{
		return invoke(threadMirror, InvokeFlags.NONE, thisObject, arguments);
	}

	@Nullable
	@Deprecated
	public Value<?> invoke(@Nonnull ThreadMirror threadMirror, @Nonnull InvokeFlags invokeFlags, @Nullable Value<?> thisObject, Value<?>... arguments)
	{
		InvokeResult invokeResult = invokeNew(threadMirror, InvokeFlags.pack(invokeFlags), thisObject, arguments);
		return invokeResult.getValue();
	}

	@Nonnull
	public InvokeResult invokeNew(@Nonnull ThreadMirror threadMirror, @Nullable Value<?> thisObject, Value<?>... arguments)
	{
		return invokeNew(threadMirror, 0, thisObject, arguments);
	}

	@Nonnull
	public InvokeResult invokeNew(@Nonnull ThreadMirror threadMirror, int invokeFlags, @Nullable Value<?> thisObject, Value<?>... arguments)
	{
		if(arguments.length != parameters().length)
		{
			throw new IllegalArgumentException("Wrong count of arguments");
		}
		try
		{
			thisObject = thisObject == null ? new NoObjectValueMirror(vm) : thisObject;
			return VirtualMachine_InvokeMethod.process(vm, threadMirror, invokeFlags, this, thisObject, arguments).getValue();
		}
		catch(JDWPException e)
		{
			throw e.asUncheckedException();
		}
	}

	@Nonnull
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

	@Nonnull
	public LocalVariableMirror[] locals()
	{
		return localsInfo().localVariables;
	}

	@Nonnull
	public LocalVariableMirror[] locals(long index)
	{
		LocalVariableMirror[] locals = locals();
		List<LocalVariableMirror> localVariableMirrors = new ArrayList<LocalVariableMirror>(locals.length);
		for(LocalVariableMirror local : locals)
		{
			if(index >= local.liveRangeStart() && index <= local.liveRangeEnd())
			{
				localVariableMirrors.add(local);
			}
		}
		return localVariableMirrors.toArray(new LocalVariableMirror[localVariableMirrors.size()]);
	}

	@Override
	protected CustomAttributeMirror[] customAttributesImpl() throws JDWPException
	{
		return vm.isAtLeastVersion(2, 21) ? Method_GetCustomAttributes.process(vm, this).customAttributeMirrors : CustomAttributeMirror.EMPTY_ARRAY;
	}

	@Nonnull
	@Override
	protected String nameImpl() throws JDWPException
	{
		return Method_GetName.process(vm, this).name;
	}

	@Nonnull
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
			throw e.asUncheckedException();
		}
	}

	@Override
	public boolean isStatic()
	{
		return (info().attributes & MethodAttributes.Static) == MethodAttributes.Static;
	}

	@Override
	public boolean isAbstract()
	{
		return (info().attributes & MethodAttributes.Abstract) == MethodAttributes.Abstract;
	}
}

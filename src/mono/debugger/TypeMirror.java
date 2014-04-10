package mono.debugger;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import mono.debugger.protocol.Type_GetInfo;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class TypeMirror extends MirrorWithIdAndName implements TypeMirrorOld
{
	private Type_GetInfo myInfo;

	public TypeMirror(@NotNull VirtualMachine aVm, long id)
	{
		super(aVm, id);
	}

	private Type_GetInfo info()
	{
		if(myInfo == null)
		{
			try
			{
				myInfo = Type_GetInfo.process(vm, this);
			}
			catch(JDWPException e)
			{
				throw e.toJDIException();
			}
		}
		return myInfo;
	}

	@NotNull
	@Override
	protected String nameImpl() throws JDWPException
	{
		return info().name;
	}

	@NotNull
	@Override
	public String qualifiedName()
	{
		return info().fullName;
	}

	@NotNull
	@Override
	public String namespace()
	{
		return info().namespace;
	}

	@Override
	public TypeMirrorOld superclass()
	{
		return null;
	}

	@Override
	public List<TypeMirrorOld> subclasses()
	{
		return null;
	}

	@Override
	public boolean isEnum()
	{
		return false;
	}

	@Override
	public void setValue(Field field, Value value) throws InvalidTypeException, ClassNotLoadedException
	{

	}

	@Override
	public Value invokeMethod(
			ThreadMirror thread,
			MethodMirror method,
			List<? extends Value> arguments,
			int options) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException
	{
		return null;
	}

	@Override
	public ObjectReference newInstance(
			ThreadMirror thread,
			MethodMirror method,
			List<? extends Value> arguments,
			int options) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException
	{
		return null;
	}

	@Override
	public MethodMirror concreteMethodByName(String name, String signature)
	{
		return null;
	}

	@Override
	public String genericSignature()
	{
		return null;
	}

	@Override
	public String sourceName() throws AbsentInformationException
	{
		return null;
	}

	@Override
	public List<String> sourceNames() throws AbsentInformationException
	{
		return null;
	}

	@Override
	public List<String> sourcePaths() throws AbsentInformationException
	{
		return null;
	}

	@Override
	public String sourceDebugExtension() throws AbsentInformationException
	{
		return null;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public boolean isAbstract()
	{
		return false;
	}

	@Override
	public boolean isFinal()
	{
		return false;
	}

	@Override
	public boolean isPrepared()
	{
		return false;
	}

	@Override
	public boolean isVerified()
	{
		return false;
	}

	@Override
	public boolean isInitialized()
	{
		return false;
	}

	@Override
	public boolean failedToInitialize()
	{
		return false;
	}

	@Override
	public List<Field> fields()
	{
		return null;
	}

	@Override
	public List<Field> visibleFields()
	{
		return null;
	}

	@Override
	public List<Field> allFields()
	{
		return null;
	}

	@Override
	public Field fieldByName(String fieldName)
	{
		return null;
	}

	@Override
	public List<MethodMirror> methods()
	{
		return null;
	}

	@Override
	public List<MethodMirror> visibleMethods()
	{
		return null;
	}

	@Override
	public List<MethodMirror> allMethods()
	{
		return null;
	}

	@Override
	public List<MethodMirror> methodsByName(String name)
	{
		return null;
	}

	@Override
	public List<MethodMirror> methodsByName(String name, String signature)
	{
		return null;
	}

	@Override
	public List<ReferenceType> nestedTypes()
	{
		return null;
	}

	@Override
	public Value getValue(Field field)
	{
		return null;
	}

	@Override
	public Map<Field, Value> getValues(List<? extends Field> fields)
	{
		return null;
	}

	@Override
	public ClassObjectReference classObject()
	{
		return null;
	}

	@Override
	public List<Location> allLineLocations() throws AbsentInformationException
	{
		return null;
	}

	@Override
	public List<Location> allLineLocations(String sourceName) throws AbsentInformationException
	{
		return null;
	}

	@Override
	public List<Location> locationsOfLine(int lineNumber) throws AbsentInformationException
	{
		return null;
	}

	@Override
	public List<Location> locationsOfLine(String sourceName, int lineNumber) throws AbsentInformationException
	{
		return null;
	}

	@Override
	public List<String> availableStrata()
	{
		return null;
	}

	@Override
	public String defaultStratum()
	{
		return null;
	}

	@Override
	public List<ObjectReference> instances(long maxInstances)
	{
		return null;
	}

	@Override
	public int modifiers()
	{
		return 0;
	}

	@Override
	public boolean isPrivate()
	{
		return false;
	}

	@Override
	public boolean isPackagePrivate()
	{
		return false;
	}

	@Override
	public boolean isProtected()
	{
		return false;
	}

	@Override
	public boolean isPublic()
	{
		return false;
	}

	@Override
	public int compareTo(ReferenceType o)
	{
		return 0;
	}

	@Override
	public String signature()
	{
		return null;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName()).append(" {").append(" id = ").append(id()).append(", fullName = ").append(qualifiedName()).append(" }");
		return builder.toString();
	}
}

package mono.debugger;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 04.01.2016
 */
public class StructValueMirror extends ValueImpl<Object>
{
	public static final Pattern BackingFieldPattern = Pattern.compile("<([\\S\\d]+)>k__BackingField");

	private TypeMirror myTypeMirror;
	private Value[] myValues;

	public StructValueMirror(VirtualMachine aVm, @NotNull TypeMirror typeMirror, Value[] values)
	{
		super(aVm);
		myTypeMirror = typeMirror;
		myValues = values;
	}

	@NotNull
	public Map<FieldOrPropertyMirror, Value<?>> values()
	{
		TypeMirror type = type();
		FieldMirror[] fields = type.fields();
		if(fields.length != myValues.length)
		{
			return Collections.emptyMap();
		}

		PropertyMirror[] properties = type.properties();
		Map<String, PropertyMirror> propertiesByName = new HashMap<String, PropertyMirror>();
		for(PropertyMirror property : properties)
		{
			propertiesByName.put(property.name(), property);
		}

		Map<FieldOrPropertyMirror, Value<?>> values = new LinkedHashMap<FieldOrPropertyMirror, Value<?>>();
		for(int i = 0; i < fields.length; i++)
		{
			FieldMirror field = fields[i];

			FieldOrPropertyMirror fieldOrPropertyMirror = field;
			String name = field.name();
			Matcher matcher = BackingFieldPattern.matcher(name);
			if(matcher.find())
			{
				PropertyMirror propertyMirror = propertiesByName.get(matcher.group(1));
				if(propertyMirror != null)
				{
					fieldOrPropertyMirror = propertyMirror;
				}
			}

			values.put(fieldOrPropertyMirror, myValues[i]);
		}
		return values;
	}

	@NotNull
	@Override
	public TypeMirror type()
	{
		return myTypeMirror;
	}

	@Nullable
	@Override
	public Object value()
	{
		return null;
	}

	@Override
	public void accept(@NotNull ValueVisitor valueVisitor)
	{
		valueVisitor.visitStructValue(this);
	}
}

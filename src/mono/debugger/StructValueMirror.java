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
public class StructValueMirror extends ValueTypeValueMirror<Object>
{
	public static final Pattern BackingFieldPattern = Pattern.compile("<([\\S\\d]+)>k__BackingField");

	public StructValueMirror(VirtualMachine aVm, @NotNull TypeMirror typeMirror, Value[] values)
	{
		super(aVm, typeMirror, values);
	}

	@NotNull
	public Map<FieldOrPropertyMirror, Value<?>> map()
	{
		Value[] fieldValues = fieldValues();
		TypeMirror type = type();
		FieldMirror[] fields = type.fields();
		if(fields.length != fieldValues.length)
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

			values.put(fieldOrPropertyMirror, fieldValues[i]);
		}
		return values;
	}

	@Override
	public boolean isEnum()
	{
		return false;
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

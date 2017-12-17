package test;

import consulo.internal.dotnet.asm.signature.TypeAttributes;

/**
 * @author VISTALL
 * @since 06.06.14
 */
public class Test
{
	public static void main(String[] args)
	{
		System.out.println(isSet(1048578, TypeAttributes.VisibilityMask, TypeAttributes.NotPublic));
		System.out.println(isSet(1048578, TypeAttributes.VisibilityMask, TypeAttributes.Public));
		System.out.println(isSet(1048578, TypeAttributes.VisibilityMask, TypeAttributes.NestedPublic));
		System.out.println(isSet(1048578, TypeAttributes.VisibilityMask, TypeAttributes.NestedPrivate));
		System.out.println(isSet(1048578, TypeAttributes.VisibilityMask, TypeAttributes.NestedFamily));
		System.out.println(isSet(1048578, TypeAttributes.VisibilityMask, TypeAttributes.NestedAssembly));
		System.out.println(isSet(1048578, TypeAttributes.VisibilityMask, TypeAttributes.NestedFamANDAssem));
		System.out.println(isSet(1048578, TypeAttributes.VisibilityMask, TypeAttributes.NestedFamORAssem));
	}

	public static boolean isSet(int val, int mask, int to)
	{
		return (val & mask) == to;
	}
}

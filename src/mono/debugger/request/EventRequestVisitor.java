package mono.debugger.request;

/**
 * @author VISTALL
 * @since 11.05.14
 */
public class EventRequestVisitor<A, R>
{
	public R visitTypeLoad(TypeLoadRequest request, A argument)
	{
		return null;
	}

	public R visitBreakpoint(BreakpointRequest request, A argument)
	{
		return null;
	}

	public R visitAppDomainCreate(AppDomainCreateRequest request, A argument)
	{
		return null;
	}

	public R visitAppDomainCreateUnload(AppDomainUnloadRequest request, A argument)
	{
		return null;
	}

	public R visitVMDeath(VMDeathRequest request, A argument)
	{
		return null;
	}

	public R visitThreadStart(ThreadStartRequest request, A argument)
	{
		return null;
	}

	public R visitMethodEntry(MethodEntryRequest request, A argument)
	{
		return null;
	}

	public R visitThreadDeath(ThreadDeathRequest request, A argument)
	{
		return null;
	}

	public R visitMethodExit(MethodExitRequest request, A argument)
	{
		return null;
	}

	public R visitStep(StepRequest stepRequest, A argument)
	{
		return null;
	}

	public R visitException(ExceptionRequest request, A argument)
	{
		return null;
	}
}

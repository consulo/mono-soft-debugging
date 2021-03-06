package mono.debugger;

/**
 * @author VISTALL
 * @since 22.04.14
 */
public enum EventKind
{
	VM_START,
	VM_DEATH,
	THREAD_START,
	THREAD_DEATH,
	APPDOMAIN_CREATE,
	APPDOMAIN_UNLOAD,
	METHOD_ENTRY,
	METHOD_EXIT,
	ASSEMBLY_LOAD,
	ASSEMBLY_UNLOAD,
	BREAKPOINT,
	STEP,
	TYPE_LOAD,
	EXCEPTION,
	KEEPALIVE,
	USER_BREAK,
	USER_LOG,
}

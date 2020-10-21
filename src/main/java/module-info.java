/**
 * @author VISTALL
 * @since 2020-10-22
 */
module mono.soft.debugging {
    requires transitive consulo.internal.dotnet.asm;
    requires consulo.annotation;

    exports mono.debugger;
    exports mono.debugger.event;
    exports mono.debugger.request;
    exports mono.debugger.util;
}
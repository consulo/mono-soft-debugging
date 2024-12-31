/**
 * @author VISTALL
 * @since 2020-10-22
 */
module mono.soft.debugging {
    requires transitive consulo.internal.dotnet.asm;
    requires jakarta.annotation;

    exports mono.debugger;
    exports mono.debugger.connect;
    exports mono.debugger.event;
    exports mono.debugger.request;
    exports mono.debugger.protocol;
    exports mono.debugger.util;
}
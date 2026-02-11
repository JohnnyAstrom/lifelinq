package app.lifelinq.config;

public final class RequestContextHolder {
    private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

    private RequestContextHolder() {
    }

    public static RequestContext getCurrent() {
        return CONTEXT.get();
    }

    public static void set(RequestContext context) {
        CONTEXT.set(context);
    }

    public static void clear() {
        CONTEXT.remove();
    }
}

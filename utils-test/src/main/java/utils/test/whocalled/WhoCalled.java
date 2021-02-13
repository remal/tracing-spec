package utils.test.whocalled;

import lombok.val;

public abstract class WhoCalled {

    private static final CustomSecurityManager SECURITY_MANAGER = new CustomSecurityManager();

    public static Class<?> getCallerClass(int depth) {
        val context = SECURITY_MANAGER.getClassContext();
        return context[depth + 2];
    }

    public static Class<?> getCallerClass() {
        return getCallerClass(1);
    }


    private static class CustomSecurityManager extends SecurityManager {
        @Override
        public Class<?>[] getClassContext() {
            return super.getClassContext();
        }
    }


    private WhoCalled() {
    }

}

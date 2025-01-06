import com.github.fallencrystal.lavender.api.accessor.AccessorFactory;
import com.github.fallencrystal.lavender.api.accessor.annotation.Accessor;
import com.github.fallencrystal.lavender.api.accessor.annotation.RuntimeGenerated;
import com.github.fallencrystal.lavender.api.accessor.interfaces.InstanceCheck;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AccessorFactoryTest {
    @Test
    public void testInstanceChecker() throws IllegalAccessException {
        try {
            AccessorFactory.getInstanceCheck(String.class).isInstance(null);
            Assertions.fail("Should throw IllegalAccessException. Because InstanceCheck is not exist in target ClassLoader.");
        } catch (IllegalAccessException ignore) {

        }
        try {
            AccessorFactory.getInstanceCheck(Object.class);
            Assertions.fail("Should throw IllegalArgumentException. Because cannot generate InstanceCheck for Object.class");
        } catch (IllegalArgumentException ignore) {

        }
        final @NotNull InstanceCheck check = AccessorFactory.getInstanceCheck(InstanceCheck.class.getClassLoader(), String.class);
        Assertions.assertEquals(check, AccessorFactory.getInstanceCheck(InstanceCheck.class.getClassLoader(), String.class));
        Assertions.assertEquals(check.getTargetClass(), String.class);
        Assertions.assertTrue(check.isInstance("Test String"));
        Assertions.assertNotNull(check.getClass().getAnnotation(RuntimeGenerated.class));
    }

    @Test
    @SuppressWarnings("unused")
    public void testAccessor() {
        class ExampleData {
            public final @NotNull String publicField = "publicField";
            final @NotNull String protectedField = "protectedField";
            private final @NotNull String privateField = "privateField";
            public final int intField = 0;
            public final boolean booleanField = false;

            public void testMethod() {}
            public void testMethodWithParameters(final @NotNull Object object) {}
            public @Nullable String nullMethod() { return null; }
        }
        @Accessor(ExampleData.class)
        interface ExampleAccessor1 {
            @NotNull String getPublicField();
            @NotNull String getProtectedField();
            @NotNull String getPrivateField();
            int getIntField();
            boolean isBooleanField();
            void testMethod();
            void testMethodWithParameters(@NotNull Object object);
            @Nullable String nullMethod();
        }
    }
}

package loader;

import clojure.lang.DynamicClassLoader;
import org.quartz.spi.ClassLoadHelper;

public class DynamicClassLoadHelper extends DynamicClassLoader implements ClassLoadHelper {
  public void initialize() {}

  @SuppressWarnings("unchecked")
  public <T> Class<? extends T> loadClass(String name, Class<T> clazz)
      throws ClassNotFoundException {
    return (Class<? extends T>) loadClass(name);
  }

  public ClassLoader getClassLoader() {
    return this;
  }
}

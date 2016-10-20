package il.org.spartan.plugin;

import java.util.*;
import java.util.concurrent.atomic.*;

import il.org.spartan.utils.*;

/** An abstract listener taking events that may have any number of parameters of
 * any kind; default implementation is empty, override to specialize, or use
 * {@link Listener.S}
 * @author Ori Roth
 * @author Yossi Gil
 * @see #tick(Object...)
 * @see #push(Object...)
 * @see #pop(Object...)
 * @since 2.6 */
public interface Listener {
  final AtomicLong eventId = new AtomicLong();

  default Listener asListener() {
    return this;
  }

  /** Create a new id for an event
   * @param ¢ notification details
   * @return */
  static long newId() {
    return eventId.incrementAndGet();
  }

  /** Main listener function.
   * @param ¢ notification details */
  void tick(final Object... os);

  /** Begin a delimited listening session
   * @param ¢ notification details
   * @see #pop */
  default void push(final Object... ¢) {
    tick(¢);
  }

  /** Used to restore a pushed listening session
   * @param ¢ notification details */
  default void pop(final Object... ¢) {
    tick(¢);
  }

  /** A listener that records a long string of the message it got.
   * @author Yossi Gil
   * @since 2016 */
  class Tracing implements Listener {
    private final StringBuilder $ = new StringBuilder();

    public String $() {
      return $ + "";
    }

    @Override public void tick(final Object... os) {
      $.append(newId() + ": ");
      final Separator s = new Separator(", ");
      for (final Object ¢ : os)
        $.append(s + trim(¢));
      $.append('\n');
    }

    private static String trim(final Object ¢) {
      return (¢ + "").substring(1, 35);
    }
  }

  /** An aggregating kind of {@link Listener} that dispatches the event it
   * receives to the multiple {@link Listener}s it stores internally.
   * @author Yossi Gil
   * @since 2.6 */
  class S extends ArrayList<Listener> implements Listener {
    private static final long serialVersionUID = 1L;

    @Override public void tick(final Object... os) {
      asListener().tick(os);
      for (final Listener ¢ : this)
        ¢.tick(os);
    }

    /** for fluent API use, i.e., <code>
     *
     * <pre>
         <b>public final</b>  {@link Listener}  listeners =  {@link Listener.S} . {@link #empty()}
     * </pre>
     *
     * <code>
     * @return an empty new instance */
    public static Listener.S empty() {
      return new Listener.S();
    }

    /** To be used in the nano found in {@link ConfigurableObjectTemplate}
     * @return <code><b>this</b></code> */
    public Listener listeners() {
      return this;
    }
  }
}
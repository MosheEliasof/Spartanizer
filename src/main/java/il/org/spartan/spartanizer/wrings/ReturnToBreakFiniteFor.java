package il.org.spartan.spartanizer.wrings;

import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.text.edits.*;

import il.org.spartan.spartanizer.ast.*;
import il.org.spartan.spartanizer.dispatch.*;
import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.wringing.*;

/** Convert Finite loops with return statements to shorter ones : </br>
 * Convert <br/>
 * <code>
 * for (..) { <br/>
 *  does(something); <br/>
 *   return XX; <br/>
 * } <br/>
 *return XX; <br/>
 * </code> to : <br/>
 * <code>
 * for (..) { <br/>
 *  does(something); <br/>
 *   break; <br/>
 * } <br/>
 *return XX; <br/>
 * </code>
 * @author Dor Ma'ayan
 * @since 2016-09-07 */
public final class ReturnToBreakFiniteFor extends CarefulWring<ForStatement> implements Kind.Collapse {
  private static boolean compareReturnStatements(final ReturnStatement r1, final ReturnStatement r2) {
    return r1 != null && r2 != null && (r1.getExpression() + "").equals(r2.getExpression() + "");
  }

  @SuppressWarnings("unchecked") private static Statement handleBlock(final Block body, final ReturnStatement nextReturn) {
    Statement $ = null;
    final List<Statement> blockStatements = body.statements();
    for (final Statement ¢ : blockStatements) {
      if (az.ifStatement(¢) != null)
        $ = handleIf(¢, nextReturn);
      if (compareReturnStatements(nextReturn, az.returnStatement(¢))) {
        $ = ¢;
        break;
      }
    }
    return $;
  }

  private static Statement handleIf(final Statement s, final ReturnStatement nextReturn) {
    if (!iz.ifStatement(s))
      return null;
    final IfStatement ifStatement = az.ifStatement(s);
    final Statement then = ifStatement.getThenStatement();
    final Statement elze = ifStatement.getElseStatement();
    if (then != null) {
      if (compareReturnStatements(az.returnStatement(then), nextReturn))
        return then;
      if (iz.block(then)) {
        final Statement $ = handleBlock((Block) then, nextReturn);
        if ($ != null)
          return $;
      }
      if (az.ifStatement(then) != null)
        return handleIf(then, nextReturn);
      if (elze != null) {
        if (compareReturnStatements(az.returnStatement(elze), nextReturn))
          return elze;
        if (iz.block(elze)) {
          final Statement $ = handleBlock((Block) elze, nextReturn);
          if ($ != null)
            return $;
        }
        if (az.ifStatement(elze) != null)
          return handleIf(elze, nextReturn);
      }
    }
    return null;
  }

  private static boolean isInfiniteLoop(final ForStatement ¢) {
    return iz.booleanLiteral(¢) && az.booleanLiteral(¢.getExpression()).booleanValue();
  }

  @Override public String description() {
    return "Convert the return inside the loop to break";
  }

  @Override public String description(final ForStatement ¢) {
    return "Convert the return inside " + ¢ + " to break";
  }

  @Override public boolean prerequisite(final ForStatement ¢) {
    return ¢ != null && extract.nextReturn(¢) != null && !isInfiniteLoop(¢);
  }

  @Override public Suggestion suggest(final ForStatement s) {
    final ReturnStatement nextReturn = extract.nextReturn(s);
    if (nextReturn == null || isInfiniteLoop(s))
      return null;
    final Statement body = s.getBody();
    final Statement $ = iz.returnStatement(body) && compareReturnStatements(nextReturn, az.returnStatement(body)) ? body
        : iz.block(body) ? handleBlock((Block) body, nextReturn) : iz.ifStatement(body) ? handleIf(body, nextReturn) : null;
    return $ == null ? null : new Suggestion(description(), $) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        r.replace($, (ASTNode) az.block(into.s("break;")).statements().get(0), g);
      }
    };
  }
}
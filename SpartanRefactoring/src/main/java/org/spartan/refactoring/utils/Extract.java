package org.spartan.refactoring.utils;

import static org.eclipse.jdt.core.dom.ASTNode.BLOCK;
import static org.eclipse.jdt.core.dom.ASTNode.EMPTY_STATEMENT;
import static org.spartan.refactoring.utils.Funcs.asAssignment;
import static org.spartan.refactoring.utils.Funcs.asBlock;
import static org.spartan.refactoring.utils.Funcs.asExpressionStatement;
import static org.spartan.refactoring.utils.Funcs.asMethodInvocation;
import static org.spartan.refactoring.utils.Funcs.asReturnStatement;
import static org.spartan.refactoring.utils.Funcs.asThrowStatement;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;

/**
 * An empty <code><b>enum</b></code> for fluent programming. The name should say
 * it all: The name, followed by a dot, followed by a method name, should read
 * like a sentence phrase.
 *
 * @author Yossi Gil
 * @since 2015-07-28
 */
public enum Extract {
  ;
  /**
   * @param n a node to extract an expression from
   * @return null if the statement is not an expression or return statement or
   *         the expression if they are
   */
  public static Expression returnExpression(final ASTNode n) {
    final ReturnStatement $ = returnStatement(n);
    return $ == null ? null : $.getExpression();
  }
  /**
   * Extract the single {@link ReturnStatement} embedded in a node.
   * 
   * @param n JD
   * @return the single {@link ReturnStatement} embedded in the parameter, and
   *         return it; <code><b>null</b></code> if not such statements exists.
   */
  public static ReturnStatement returnStatement(final ASTNode n) {
    return asReturnStatement(Extract.singleStatement(n));
  }
  /**
   * Extract the single {@link ThrowStatement} embedded in a node.
   * 
   * @param n JD
   * @return the single {@link ThrowStatement} embedded in the parameter, and
   *         return it; <code><b>null</b></code> if not such statements exists.
   */
  public static ThrowStatement throwStatement(final ASTNode n) {
    return asThrowStatement(Extract.singleStatement(n));
  }
  /**
   * @param n a node to extract an expression from
   * @return null if the statement is not an expression or return statement or
   *         the expression if they are
   */
  public static Expression throwExpression(final ASTNode n) {
    final ThrowStatement $ = asThrowStatement(Extract.singleStatement(n));
    return $ == null ? null : $.getExpression();
  }
  /**
   * @param node a node to extract an expression from
   * @return null if the statement is not an expression or return statement or
   *         the expression if they are
   */
  public static Expression expression(final ASTNode node) {
    if (node == null)
      return null;
    switch (node.getNodeType()) {
      case ASTNode.EXPRESSION_STATEMENT:
        return ((ExpressionStatement) node).getExpression();
      case ASTNode.RETURN_STATEMENT:
        return ((ReturnStatement) node).getExpression();
      case ASTNode.THROW_STATEMENT:
        return ((ThrowStatement) node).getExpression();
      default:
        return null;
    }
  }
  /**
   * @param n JD
   * @return the method invocation if it exists or null if it doesn't or if the
   *         block contains more than one statement
   */
  public static MethodInvocation methodInvocation(final ASTNode n) {
    return asMethodInvocation(Extract.expressionStatement(n).getExpression());
  }
  /**
   * @param n a statement or block to extract the assignment from
   * @return null if the block contains more than one statement or if the
   *         statement is not an assignment or the assignment if it exists
   */
  public static Assignment assignment(final ASTNode n) {
    final ExpressionStatement e = Extract.expressionStatement(n);
    return e == null ? null : asAssignment(e.getExpression());
  }
  /**
   * Convert, is possible, an {@link ASTNode} to a {@link ExpressionStatement}
   *
   * @param n a statement or a block to extract the expression statement from
   * @return the expression statement if n is a block or an expression statement
   *         or null if it not an expression statement or if the block contains
   *         more than one statement
   */
  public static ExpressionStatement expressionStatement(final ASTNode n) {
    return n == null ? null : asExpressionStatement(Extract.singleStatement(n));
  }
  /**
   * Extract the list of non-empty statements embedded in node (nesting within
   * control structure such as <code><b>if</b></code> are not removed.)
   *
   * @param n JD
   * @return the list of such statements.
   */
  public static List<Statement> statements(final ASTNode n) {
    final List<Statement> $ = new ArrayList<>();
    return n == null || !(n instanceof Statement) ? $ : Extract.statementsInto((Statement) n, $);
  }
  private static List<Statement> statementsInto(final Statement s, final List<Statement> $) {
    final int nodeType = s.getNodeType();
    switch (nodeType) {
      case EMPTY_STATEMENT:
        return $;
      case BLOCK:
        return Extract.statementsInto((Block) s, $);
      default:
        $.add(s);
        return $;
    }
  }
  private static List<Statement> statementsInto(final Block b, final List<Statement> $) {
    for (final Object statement : b.statements())
      Extract.statementsInto((Statement) statement, $);
    return $;
  }
  /**
   * @param n JD
   * @return if b is a block with just 1 statement it returns that statement, if
   *         b is statement it returns b and if b is null it returns a null
   */
  public static Statement singleStatement(final ASTNode n) {
    final List<Statement> $ = Extract.statements(n);
    return $.size() != 1 ? null : (Statement) $.get(0);
  }
  /**
   * Find a subsequent return statement
   * 
   * @param s JD
   * @return the subsequent {@link ReturnStatement}, or <code><b>null</b> if
   *         there is not subsequent statement or if its type is not
   *         {@link ReturnStatement}
   */
  public static ReturnStatement nextReturn(Statement s) {
    return asReturnStatement(next(s));
  }
  private static Statement next(Statement s) {
    final Block b = asBlock(s.getParent());
    if (b == null)
      return null;
    return next(s, Extract.statements(b));
  }
  private static Statement next(Statement s, List<Statement> ss) {
    for (int i = 0; i < ss.size() - 1; ++i)
      if (ss.get(i) == s)
        return ss.get(i + 1);
    return null;
  }
}
